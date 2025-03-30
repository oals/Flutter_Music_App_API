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
    private final FollowService followService;
    private final TrackLikeService trackLikeService;


    @Override
    public Map<String, Object> saveTrack(UploadDto uploadDto) {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            // 트랙 엔티티 저장
            Track track = createTrack(uploadDto);
            // 트랙 연관 관계 설정
            setTrackRelationships(track, uploadDto);

            returnMap.put("trackId", track.getTrackId());
            returnMap.put("status", "200");
            return returnMap;

        } catch (Exception e) {
            returnMap.put("isStatus", "500");
        }

        return returnMap;
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
                .isTrackPrivacy(uploadDto.getTrackPrivacy())
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
                    .set(QTrack.track.isTrackPrivacy, trackRequestDto.getTrackPrivacy())
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


    @Override
    public List<SearchDto> getSearchTrackList(SearchRequestDto searchRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<SearchDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(searchRequestDto.getLoginMemberId())
                .joinCategoryWithTrackCategoryId()
                .findTrackBySearchText(searchRequestDto.getSearchText())
                .findIsTrackPrivacyFalse()
                .groupByMemberTrackId()
                .offset(searchRequestDto.getOffset())
                .limit(searchRequestDto.getLimit())
                .orderByMemberTrackIdDesc()
                .fetTrackSearchDto(SearchDto.class);

    }

    @Override
    public Long getSearchTrackListCnt(SearchRequestDto searchRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackBySearchText(searchRequestDto.getSearchText())
                .findIsTrackPrivacyFalse()
                .fetchCount();
    }



    @Override
    public Map<String, Object> getLastListenTrackList(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            Object lastListenTrackIdObject = redisService.getLastListenTrackIdList(trackRequestDto).get("lastListenTrackList");

            if (lastListenTrackIdObject instanceof List<?>) {

                List<String> lastListenTrackIdList = (List<String>) lastListenTrackIdObject;









            } else {
                // 타입이 맞지 않으면 예외를 던짐
                throw new IllegalArgumentException();
            }

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }


    @Override
    public List<TrackDto> getAllMemberTrackList(MemberRequestDto memberRequestDto){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrLoginMemberIdEqual(memberRequestDto.getLoginMemberId())
                        .orderByMemberTrackIdDesc()
                        .offset(memberRequestDto.getOffset())
                        .limit(memberRequestDto.getLimit())
                        .fetchTrackPreviewDto(TrackDto.class);


    }

    @Override
    public List<TrackDto> getPopularMemberTrackList(MemberRequestDto memberRequestDto){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrLoginMemberIdEqual(memberRequestDto.getLoginMemberId())
                        .orderByTrackPlayCntDesc()
                        .offset(memberRequestDto.getOffset())
                        .limit(memberRequestDto.getLimit())
                        .fetchTrackPreviewDto(TrackDto.class);

    }

    @Override
    public Long getMemberTrackListCnt(MemberRequestDto memberRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrLoginMemberIdEqual(memberRequestDto.getLoginMemberId())
                        .fetchCount();
    }

    @Override
    public Map<String, Object> getTrackInfo(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            TrackDto trackInfoDto = trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                            .findTrackByTrackId(trackRequestDto.getTrackId())
                            .fetchTrackDetailDto(TrackDto.class);

            /* 해당 트랙에 좋아요 여부 */
            Boolean trackLikeStatus = trackLikeService.getTrackLikeStatus(trackRequestDto);
            trackInfoDto.setTrackLikeStatus(trackLikeStatus);

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



    private Long getTrackCommentCnt(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .leftJoinTrackCommentListWithComment()
                .fetchCount();
    }

    @Override
    public Map<String, Object> getRecommendTrack(TrackRequestDto trackRequestDto) {
        Map<String,Object> hashMap = new HashMap<>();

        try{

            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            List<TrackDto> recommendTrackDtoList = (List<TrackDto>)
                    trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                            .findTrackNotInList(Collections.singletonList(trackRequestDto.getTrackId()))
                            .findIsTrackPrivacyFalse()
                            .findCategoryTracks(trackRequestDto.getTrackCategoryId())
                            .orderByTrackUploadDateDesc()
                            .offset(trackRequestDto.getOffset())
                            .limit(trackRequestDto.getLimit())
                            .fetchTrackPreviewDto(TrackDto.class);

            hashMap.put("recommendTrackList",recommendTrackDtoList);
            hashMap.put("status","200");

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String,Object> getUploadTrack(TrackRequestDto trackRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        try {
            TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

            List<TrackDto> uploadTrackDtoList = (List<TrackDto>)
                    trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
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

    @Override
    public List<TrackDto> getFollowMemberTrackList(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .joinMemberTrackWithMember()
                        .joinMemberFollowersAndFollow()
                        .findIsTrackPrivacyFalse()
                        .findFollowerTracks(trackRequestDto.getLoginMemberId())
                        .orderByTrackUploadDateDesc()
                        .offset(trackRequestDto.getOffset())
                        .limit(trackRequestDto.getLimit())
                        .fetchTrackListDto(TrackDto.class);

    }


    @Override
    public List<TrackDto> getTrendingTrackList(TrackRequestDto trackRequestDto){

        List<TrackDto> trendingTrackList = new ArrayList<>();

        trendingTrackList.addAll(getTrendingTrackPlayDesc(trackRequestDto));

        trendingTrackList.addAll(getTrendingTrackLikeDesc(trackRequestDto, trendingTrackList));

        return trendingTrackList;
    }


    private List<TrackDto> getTrendingTrackPlayDesc(TrackRequestDto trackRequestDto){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .isUploadedThisWeekOrLastWeek(QMemberTrack.memberTrack.track.trackUploadDate)
                        .findIsTrackPrivacyFalse()
                        .orderByTrackPlayCntDesc()
                        .offset(trackRequestDto.getOffset())
                        .limit(trackRequestDto.getLimit())
                        .fetchTrackListDto(TrackDto.class);

    }

    private List<TrackDto> getTrendingTrackLikeDesc(TrackRequestDto trackRequestDto, List<TrackDto> trackLikeDescList){

        List<Long> excludedTrackIds = trackLikeDescList.stream()
                .map(TrackDto::getTrackId)  // TrackDto에서 trackId를 추출
                .collect(Collectors.toList());

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .isUploadedThisWeekOrLastWeek(QMemberTrack.memberTrack.track.trackUploadDate)
                .findIsTrackPrivacyFalse()
                .findTrackNotInList(excludedTrackIds)
                .orderByTrackLikeCntDesc()
                .offset(trackRequestDto.getOffset())
                .limit(trackRequestDto.getLimit())
                .fetchTrackListDto(TrackDto.class);
    }

}
