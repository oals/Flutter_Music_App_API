package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.TrackSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.TrackUpdateQueryBuilder;
import com.skrrskrr.project.redisService.RedisService;
import com.skrrskrr.project.repository.MemberTrackRepository;
import com.skrrskrr.project.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
@SuppressWarnings("unchecked")
public class TrackServiceImpl implements TrackService {

    @PersistenceContext
    EntityManager entitiyManager;

    private final JPAQueryFactory jpaQueryFactory;
    private final TrackRepository trackRepository;
    private final MemberTrackRepository memberTrackRepository;
    private final RedisService redisService;
    private final TrackLikeService trackLikeService;
    private final SearchService searchService;
    private final FollowService followService;


    @Override
    public Map<String, Object> getSearchTrack(SearchRequestDto searchRequestDto) {
        Map<String,Object> hashMap = new HashMap<>();
        List<TrackDto> searchTrackList = new ArrayList<>();

        try {
            /* 검색된 트랙 수 */
            Long totalCount = getSearchTrackListCnt(searchRequestDto);

            /* 검색된 트랙 정보 */
            if (totalCount != 0L) {
                searchTrackList = getSearchTrackList(searchRequestDto);
            }

            hashMap.put("totalCount", totalCount);   // 트랙의 전체 개수
            hashMap.put("searchTrackList", searchTrackList);   // 검색된 트랙 리스트
            hashMap.put("status","200");

        }catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");

        }

        return hashMap;
    }

    @Override
    public Map<String, Object> getPlayListTrackList(PlayListRequestDto playListRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();
        try{
            List<TrackDto> trackDtoList = getPlayListTracks(playListRequestDto);

            hashMap.put("playListTrackList",trackDtoList);
            hashMap.put("status","200");

        }catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String, Object> getMemberPageTrack(MemberRequestDto memberRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();
        try{
            List<TrackDto> allTrackDtoList = new ArrayList<>();
            Long allTrackDtoListCnt = getMemberTrackListCnt(memberRequestDto);

            if (allTrackDtoListCnt != 0){
                allTrackDtoList = getAllMemberTrackList(memberRequestDto);
            }

            hashMap.put("allTrackList",allTrackDtoList);
            hashMap.put("allTrackListCnt",allTrackDtoListCnt);
            hashMap.put("status","200");
        }catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;

    }

    @Override
    public Map<String, Object> getMemberPagePopularTrack(MemberRequestDto memberRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();
        try{

            List<TrackDto> popularTrackDtoList = new ArrayList<>();
            Long allTrackDtoListCnt = getMemberTrackListCnt(memberRequestDto);

            if (allTrackDtoListCnt != 0){
                memberRequestDto.setLimit(5L);
                popularTrackDtoList = getPopularMemberTrackList(memberRequestDto);
            }

            hashMap.put("popularTrackList",popularTrackDtoList);

            hashMap.put("status","200");
        }catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;

    }

    @Override
    public void saveTrack(UploadDto uploadDto) {

        try {
            // 트랙 엔티티 저장
           Track track = createTrack(uploadDto);
            // 트랙 연관 관계 설정
           setTrackRelationships(track, uploadDto);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Track createTrack(UploadDto uploadDto) {

        QMember qMember = QMember.member;
        QCategory qCategory = QCategory.category;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(uploadDto.getLoginMemberId()))
                .fetchOne();

        Category category = jpaQueryFactory.selectFrom(qCategory)
                .where(qCategory.trackCategoryId.eq(uploadDto.getTrackCategoryId()))
                .fetchOne();

        if (member == null || category == null) {
            throw new IllegalStateException("member or category cannot be null.");
        }


        // Track 엔티티 생성
        Track track = Track.builder()
                .trackNm(uploadDto.getTrackNm())
                .trackInfo(uploadDto.getTrackInfo())
                .trackCategoryId(uploadDto.getTrackCategoryId())
                .isTrackPrivacy(uploadDto.getIsTrackPrivacy())
                .trackTime(uploadDto.getTrackTime())
                .trackPath(uploadDto.getUploadFilePath())
                .trackImagePath(uploadDto.getUploadImagePath())
                .trackLikeCnt(0L)
                .trackPlayCnt(0L)
                .trackUploadDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .memberTrackList(new ArrayList<>())
                .trackCategoryList(new ArrayList<>())
                .build();

        // 트랙 저장
        return trackRepository.save(track);
    }

    private void setTrackRelationships(Track track, UploadDto uploadDto) {

        QMember qMember = QMember.member;
        QCategory qCategory = QCategory.category;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(uploadDto.getLoginMemberId()))
                .fetchOne();

        Category category = jpaQueryFactory.selectFrom(qCategory)
                .where(qCategory.trackCategoryId.eq(uploadDto.getTrackCategoryId()))
                .fetchOne();

        if (member == null || category == null) {
            throw new IllegalStateException("member or category cannot be null.");
        }

        // MemberTrack 연관 설정
        MemberTrack memberTrack = new MemberTrack();
        memberTrack.setTrack(track);
        memberTrack.setMember(member);
        track.getMemberTrackList().add(memberTrack);
        member.getMemberTrackList().add(memberTrack);

        // TrackCategory 연관 설정
        TrackCategory trackCategory = new TrackCategory();
        trackCategory.setTrack(track);
        trackCategory.setCategory(category);
        track.getTrackCategoryList().add(trackCategory);
        category.getTrackCategoryList().add(trackCategory);

        // 연관된 엔티티 저장
        memberTrackRepository.save(memberTrack);
    }


    @Override
    public Map<String,Object> updateTrackImage(UploadDto uploadDto) {
        Map<String,Object> hashMap = new HashMap<>();
        try {

            TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

            trackUpdateQueryBuilder.setEntity(QTrack.track)
                    .set(QTrack.track.trackImagePath, uploadDto.getUploadImagePath())
                    .findTrackByTrackId(uploadDto.getTrackId())
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }

    @Override
    public Map<String, Object> setLockTrack(TrackRequestDto trackRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

            trackUpdateQueryBuilder.setEntity(QTrack.track)
                    .set(QTrack.track.isTrackPrivacy, trackRequestDto.getIsTrackPrivacy())
                    .findTrackByTrackId(trackRequestDto.getTrackId())
                    .execute();


            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String, Object> setTrackinfo(TrackRequestDto trackRequestDto) {
        Map<String,Object> hashMap = new HashMap<>();

        try {
            TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

            trackUpdateQueryBuilder.setEntity(QTrack.track)
                    .set(QTrack.track.trackInfo, trackRequestDto.getTrackInfo())
                    .findTrackByTrackId(trackRequestDto.getTrackId())
                    .execute();

            hashMap.put("status","200");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
            return hashMap;
        }
    }


    @Override
    public Long getTrackLastId() {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .orderByMemberTrackIdDesc()
                .fetchTrackId() + 1;
    }



    private List<TrackDto> getSearchTrackList(SearchRequestDto searchRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(searchRequestDto.getLoginMemberId())
                .findTrackBySearchText(searchRequestDto.getSearchText())
                .findTracksBySearchTextList(searchRequestDto.getSearchTextList())
                .findIsTrackPrivacyFalseOrEqualLoginMemberId(searchRequestDto.getLoginMemberId())
                .groupByMemberTrackId()
                .offset(searchRequestDto.getOffset())
                .limit(searchRequestDto.getLimit())
                .orderByTrackPlayCntDesc()
                .orderByTrackLikeCntDesc()
                .orderByMemberTrackIdDesc()
                .fetchTrackListDto(TrackDto.class);

    }


    private Long getSearchTrackListCnt(SearchRequestDto searchRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackBySearchText(searchRequestDto.getSearchText())
                .findIsTrackPrivacyFalseOrEqualLoginMemberId(searchRequestDto.getLoginMemberId())
                .fetchCount();
    }



    @Override
    public Map<String, Object> getLastListenTrackList(TrackRequestDto trackRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();

        try {

            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);


            List<Long> lastListenTrackIdList = redisService.getLastListenTrackIdList(trackRequestDto);

            List<TrackDto> lastListenTrackList = (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findTrackInList(lastListenTrackIdList)
                    .fetchTrackListDto(TrackDto.class);

            Map<Long, TrackDto> trackMap = lastListenTrackList.stream()
                    .collect(Collectors.toMap(TrackDto::getTrackId, Function.identity()));

            List<TrackDto> lastListenTrackListOrderBy = lastListenTrackIdList.stream()
                    .map(trackMap::get)
                    .toList();

            hashMap.put("lastListenTrackList",lastListenTrackListOrderBy);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","200");
        }

        return hashMap;


    }


    @Override
    public List<TrackDto> getAllMemberTrackList(MemberRequestDto memberRequestDto){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrEqualLoginMemberId(memberRequestDto.getLoginMemberId())
                        .joinTrackLikeWithMemberTrack(memberRequestDto.getLoginMemberId())
                        .orderByMemberTrackIdDesc()
                        .offset(memberRequestDto.getOffset())
                        .limit(memberRequestDto.getLimit())
                        .fetchTrackListDto(TrackDto.class);


    }

    @Override
    public List<TrackDto> getPopularMemberTrackList(MemberRequestDto memberRequestDto){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrEqualLoginMemberId(memberRequestDto.getLoginMemberId())
                        .joinTrackLikeWithMemberTrack(memberRequestDto.getLoginMemberId())
                        .orderByTrackPlayCntDesc()
                        .orderByTrackLikeCntDesc()
                        .offset(memberRequestDto.getOffset())
                        .limit(memberRequestDto.getLimit())
                        .fetchTrackListDto(TrackDto.class);

    }

    @Override
    public Long getMemberTrackListCnt(MemberRequestDto memberRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrEqualLoginMemberId(memberRequestDto.getLoginMemberId())
                        .fetchCount();
    }

    @Override
    public Map<String, Object> getTrackInfo(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            TrackDto trackInfoDto = trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findTrackByTrackId(trackRequestDto.getTrackId())
                    .fetchTrackDetailDto(TrackDto.class);

            /* 트랙의 댓글 수 조회 */
            Long commentCount = getTrackCommentCnt(trackRequestDto);
            trackInfoDto.setCommentsCnt(commentCount);  // commentCount 값을 설정

            /* 해당 트랙의 뮤지션을 내가 팔로워 했는지 */
            Boolean isFollow = followService.isFollowCheck(trackInfoDto.getMemberId(), trackRequestDto.getLoginMemberId());
            trackInfoDto.setIsFollowMember(isFollow);  // isFollow 값을 설정

            hashMap.put("trackInfo",trackInfoDto);
            hashMap.put("status","200");
        } catch(Exception e) {
          e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String, Object> getAudioPlayerTrackList(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        List<Long> audioPlayerTrackIdList = redisService.getAudioPlayerTrackIdList(trackRequestDto);

        try{

//            List<TrackDto> audioPlayerTrackList = (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
//                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
//                    .findIsTrackPrivacyFalse(trackRequestDto.getLoginMemberId())
//                    .findTrackInList(audioPlayerTrackIdList)
////                    .orderByMemberTrackIdDesc()
////                    .limit(4)
//                    .fetchTrackListDto(TrackDto.class);


            trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findIsTrackPrivacyFalseOrEqualLoginMemberId(trackRequestDto.getLoginMemberId())
                    .findTrackInList(audioPlayerTrackIdList);


            if (false) {
                trackSelectQueryBuilder.orderByMemberTrackIdDesc();
            }

            if (false) {
                trackSelectQueryBuilder.limit(4);
            }


            List<TrackDto> audioPlayerTrackList = (List<TrackDto>) trackSelectQueryBuilder.fetchTrackListDto(TrackDto.class);



            audioPlayerTrackList.sort((track1, track2) -> {
                int index1 = audioPlayerTrackIdList.indexOf(track1.getTrackId());
                int index2 = audioPlayerTrackIdList.indexOf(track2.getTrackId());
                return Integer.compare(index1, index2);
            });

            hashMap.put("audioPlayerTrackList",audioPlayerTrackList);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }


    private Long getTrackCommentCnt(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .joinTrackCommentListWithComment()
                .fetchCount();
    }

    @Override
    public Map<String, Object> getRecommendTrack(TrackRequestDto trackRequestDto) {
        Map<String,Object> hashMap = new HashMap<>();

        try{

            trackRequestDto.setLimit(5L);
            List<TrackDto> recommendTrackList = new ArrayList<>();

            /* 최근 좋아요 누른 트랙 5명의 사용자 인기 트랙 5곡 */
            List<Long> likeTrackMemberIdList = trackLikeService.getRecommendLikeTrackMemberId(trackRequestDto);

            MemberRequestDto memberRequestDto = new MemberRequestDto();
            memberRequestDto.setLoginMemberId(trackRequestDto.getLoginMemberId());
            memberRequestDto.setLimit(1L);

            for (Long memberId : likeTrackMemberIdList) {
                memberRequestDto.setMemberId(memberId);
                recommendTrackList.addAll(getPopularMemberTrackList(memberRequestDto));
            }

            /* 내가 팔로우 한 유저의 새로운 5곡 */
            recommendTrackList.addAll(getFollowMemberTrackList(trackRequestDto));


            /* 사용자의 검색 기록이 포함 되는 트랙 5곡 */
            SearchRequestDto searchRequestDto = new SearchRequestDto();
            searchRequestDto.setLoginMemberId(trackRequestDto.getLoginMemberId());
            searchRequestDto.setLimit(5L);

            searchRequestDto.setSearchTextList(searchService.processSearchKeywords(searchRequestDto));

            recommendTrackList.addAll(getSearchTrackList(searchRequestDto));

            recommendTrackList = recommendTrackList.stream()
                    .distinct()
                    .collect(Collectors.toList());


            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            if (recommendTrackList.isEmpty()){

                recommendTrackList.addAll(
                        (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                                .findIsTrackPrivacyFalseOrEqualLoginMemberId(trackRequestDto.getLoginMemberId())
                                .orderByTrackPlayCntDesc()
                                .orderByTrackLikeCntDesc()
                                .orderByTrackUploadDateDesc()
                                .limit(15)
                                .fetchTrackListDto(TrackDto.class)
                );

            } else if (recommendTrackList.size() < 16) {
               Long addRecommendTrackLimit = (long) (16 - recommendTrackList.size());

                Map<Long, Integer> categoryStatistics = new HashMap<>();
                for (TrackDto track : recommendTrackList) {
                    Long categoryId = track.getTrackCategoryId();
                    categoryStatistics.put(categoryId, categoryStatistics.getOrDefault(categoryId, 0) + 1);
                }

                Long mostFrequentCategoryId = categoryStatistics.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .get()
                        .getKey();


                trackRequestDto.setTrackIdList(
                        recommendTrackList.stream()
                                .map(TrackDto::getTrackId)
                                .collect(Collectors.toList())
                );

                recommendTrackList.addAll(
                        (List<TrackDto>)
                        trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                                .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                                .findTrackNotInList(trackRequestDto.getTrackIdList())
                                .findIsTrackPrivacyFalse()
                                .findTracksByNotMemberId(trackRequestDto.getLoginMemberId())
                                .findCategoryTracks(mostFrequentCategoryId)
                                .orderByTrackPlayCntDesc()
                                .orderByTrackLikeCntDesc()
                                .orderByTrackUploadDateDesc()
                                .limit(addRecommendTrackLimit)
                                .fetchTrackListDto(TrackDto.class)
                );
            }

            hashMap.put("recommendTrackList",recommendTrackList);
            hashMap.put("status","200");

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }




    @Override
    public List<TrackDto> getPlayListTracks(PlayListRequestDto playListRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder
                .selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(playListRequestDto.getLoginMemberId())
                .findMemberTrackByPlayListId(playListRequestDto.getPlayListId())
                .findIsTrackPrivacyFalseOrEqualLoginMemberId(playListRequestDto.getLoginMemberId())
                .offset(playListRequestDto.getOffset())
                .limit(playListRequestDto.getLimit())
                .fetchTrackListDto(TrackDto.class);


    }


    @Override
    public Map<String,Object> getUploadTrack(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        try {
            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            List<TrackDto> uploadTrackDtoList = (List<TrackDto>) trackSelectQueryBuilder
                    .selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findTracksByMemberId(trackRequestDto.getLoginMemberId())
                    .orderByMemberTrackIdDesc()
                    .offset(trackRequestDto.getOffset())
                    .limit(trackRequestDto.getLimit())
                    .fetchTrackListDto(TrackDto.class);

           Long totalCount = trackSelectQueryBuilder
                    .resetQuery()
                    .from(QMemberTrack.memberTrack) // 동적으로 테이블 설정
                    .findTracksByMemberId(trackRequestDto.getLoginMemberId()) // 조건 설정
                    .fetchCount(); // COUNT 쿼리 실행

            hashMap.put("uploadTrackList",uploadTrackDtoList);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");

        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private List<TrackDto> getFollowMemberTrackList(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .joinMemberTrackWithMember()
                        .joinMemberFollowersAndFollow()
                        .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                        .findIsTrackPrivacyFalse()
                        .findFollowerTracks(trackRequestDto.getLoginMemberId())
                        .orderByTrackUploadDateDesc()
                        .limit(trackRequestDto.getLimit())
                        .fetchTrackListDto(TrackDto.class);

    }


}
