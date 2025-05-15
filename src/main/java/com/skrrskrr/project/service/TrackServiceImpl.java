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
    public TrackResponseDto getSearchTrack(SearchRequestDto searchRequestDto) {

        List<TrackDto> searchTrackList = new ArrayList<>();

        /* 검색된 트랙 수 */
        Long totalCount = getSearchTrackListCnt(searchRequestDto);

        /* 검색된 트랙 정보 */
        if (totalCount != 0L) {
            searchTrackList = getSearchTrackList(searchRequestDto);
        }

        return TrackResponseDto.builder()
                .trackList(searchTrackList)
                .totalCount(totalCount)
                .build();
    }

    @Override
    public TrackResponseDto getPlayListTrackList(PlayListRequestDto playListRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        List<TrackDto> playListTrackList = (List<TrackDto>) trackSelectQueryBuilder
                .selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(playListRequestDto.getLoginMemberId())
                .findMemberTrackByPlayListId(playListRequestDto.getPlayListId())
                .findIsTrackPrivacyFalseOrEqualLoginMemberId(playListRequestDto.getLoginMemberId())
                .offset(playListRequestDto.getOffset())
                .limit(playListRequestDto.getLimit())
                .fetchTrackListDto(TrackDto.class);

        return TrackResponseDto.builder()
                .trackList(playListTrackList)
                .build();
    }

    @Override
    public TrackResponseDto getMemberPageTrack(MemberRequestDto memberRequestDto) {

        List<TrackDto> memberPageAllTrack = new ArrayList<>();
        Long totalCount = getMemberTrackListCnt(memberRequestDto);

        if (totalCount != 0) {
            memberPageAllTrack = getAllMemberTrackList(memberRequestDto);
        }

        return TrackResponseDto.builder()
                .trackList(memberPageAllTrack)
                .totalCount(totalCount)
                .build();

    }

    @Override
    public TrackResponseDto getMemberPagePopularTrack(MemberRequestDto memberRequestDto) {

        List<TrackDto> popularTrackDtoList = new ArrayList<>();
        Long totalCount = getMemberTrackListCnt(memberRequestDto);

        if (totalCount != 0) {
            memberRequestDto.setLimit(5L);
            popularTrackDtoList = getMemberPopularTrackList(memberRequestDto);
        }

        return TrackResponseDto.builder()
                .trackList(popularTrackDtoList)
                .totalCount(totalCount)
                .build();
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
    public Boolean updateTrackImage(UploadDto uploadDto) {

        try {
            TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

            trackUpdateQueryBuilder.setEntity(QTrack.track)
                    .set(QTrack.track.trackImagePath, uploadDto.getUploadImagePath())
                    .findTrackByTrackId(uploadDto.getTrackId())
                    .execute();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void setLockTrack(TrackRequestDto trackRequestDto) {

        TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

        trackUpdateQueryBuilder.setEntity(QTrack.track)
                .set(QTrack.track.isTrackPrivacy, trackRequestDto.getIsTrackPrivacy())
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .execute();
    }


    @Override
    public void setTrackInfo(TrackRequestDto trackRequestDto) {

        TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);

        trackUpdateQueryBuilder.setEntity(QTrack.track)
                .set(QTrack.track.trackInfo, trackRequestDto.getTrackInfo())
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .execute();
    }


    @Override
    public Long getTrackLastId() {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .orderByMemberTrackIdDesc()
                .fetchTrackId() + 1;
    }

    @Override
    public void setTrackPlayCnt(TrackRequestDto trackRequestDto) {

        TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entitiyManager);
        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        Long trackPlayCnt = trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                            .findTrackByTrackId(trackRequestDto.getTrackId())
                            .fetchTrackPlayCnt();

        trackUpdateQueryBuilder.setEntity(QTrack.track)
                .set(QTrack.track.trackPlayCnt, trackPlayCnt + 1)
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .execute();

    }

    private List<TrackDto> getSearchRecommendTrackList(Long loginMemberId) {

        SearchRequestDto searchRequestDto = new SearchRequestDto();
        searchRequestDto.setLoginMemberId(loginMemberId);
        searchRequestDto.setLimit(5L);
        searchRequestDto.setSearchKeywordList(searchService.processSearchKeywords(searchRequestDto));

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(searchRequestDto.getLoginMemberId())
                .findTracksBySearchTextList(searchRequestDto.getSearchKeywordList())
                .findTracksByNotMemberId(searchRequestDto.getLoginMemberId())
                .findIsTrackPrivacyFalse()
                .groupByMemberTrackId()
                .offset(searchRequestDto.getOffset())
                .limit(searchRequestDto.getLimit())
                .orderByTrackPlayCntDesc()
                .orderByTrackLikeCntDesc()
                .orderByMemberTrackIdDesc()
                .fetchTrackListDto(TrackDto.class);
    }


    private List<TrackDto> getSearchTrackList(SearchRequestDto searchRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(searchRequestDto.getLoginMemberId())
                .findTrackBySearchText(searchRequestDto.getSearchText())
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
    public List<TrackDto> getLastListenTrackList(Long loginMemberId) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);
        TrackRequestDto trackRequestDto = new TrackRequestDto();
        trackRequestDto.setLoginMemberId(loginMemberId);

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

        return lastListenTrackListOrderBy;
    }

    private List<TrackDto> getAllMemberTrackList(MemberRequestDto memberRequestDto) {

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

    private List<TrackDto> getMemberPopularTrackList(MemberRequestDto memberRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .joinTrackLikeWithMemberTrack(memberRequestDto.getLoginMemberId())
                        .findTracksByMemberId(memberRequestDto.getMemberId())
                        .findIsTrackPrivacyFalseOrEqualLoginMemberId(memberRequestDto.getLoginMemberId())
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
    public TrackResponseDto getTrackInfo(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        TrackDto trackInfoDto = trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .fetchTrackDetailDto(TrackDto.class);

        /* 트랙의 댓글 수 조회 */
        Long commentCount = getTrackCommentCnt(trackRequestDto);
        trackInfoDto.setCommentsCnt(commentCount);  // commentCount 값을 설정

        /* 해당 트랙의 뮤지션을 내가 팔로워 했는지 */
        Boolean isFollow = followService.isFollowCheck(trackRequestDto.getLoginMemberId(),trackInfoDto.getMemberId());
        trackInfoDto.setIsFollowMember(isFollow);  // isFollow 값을 설정

        return TrackResponseDto.builder()
                .track(trackInfoDto)
                .build();
    }


    @Override
    public TrackResponseDto getAudioPlayerTrackList(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);
        List<TrackDto> audioPlayerTrackList = new ArrayList<>();

        List<Long> audioPlayerTrackIdList = redisService.getAudioPlayerTrackIdList(trackRequestDto);

        if (!audioPlayerTrackIdList.isEmpty()) {
            audioPlayerTrackList = (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findIsTrackPrivacyFalseOrEqualLoginMemberId(trackRequestDto.getLoginMemberId())
                    .findTrackInList(audioPlayerTrackIdList)
                    .fetchTrackListDto(TrackDto.class);


            audioPlayerTrackList.sort((track1, track2) -> {
                int index1 = audioPlayerTrackIdList.indexOf(track1.getTrackId());
                int index2 = audioPlayerTrackIdList.indexOf(track2.getTrackId());
                return Integer.compare(index1, index2);
            });

        }

        return TrackResponseDto.builder()
                .trackList(audioPlayerTrackList)
                .build();
    }


    private Long getTrackCommentCnt(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .joinTrackCommentListWithComment()
                .fetchCount();
    }

    private List<TrackDto> getRecommendLikeMemberPopularTrack(TrackRequestDto trackRequestDto) {

        MemberRequestDto memberRequestDto = new MemberRequestDto();
        List<TrackDto> recommendTrackList = new ArrayList<>();
        memberRequestDto.setLoginMemberId(trackRequestDto.getLoginMemberId());
        memberRequestDto.setLimit(1L);

        /* 최근 좋아요 누른 트랙 사용자 조회 */
        List<Long> likeTrackMemberIdList = trackLikeService.getRecommendLikeTrackMemberId(trackRequestDto);


        for (Long memberId : likeTrackMemberIdList) {
            memberRequestDto.setMemberId(memberId);
            recommendTrackList.addAll(getMemberPopularTrackList(memberRequestDto));
        }

        return recommendTrackList;
    }

    private List<TrackDto> getRecommendPopularTrack(Long loginMemberId){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return  (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinTrackLikeWithMemberTrack(loginMemberId)
                .findIsTrackPrivacyFalse()
                .findTracksByNotMemberId(loginMemberId)
                .orderByTrackPlayCntDesc()
                .orderByTrackLikeCntDesc()
                .orderByTrackUploadDateDesc()
                .limit(15)
                .fetchTrackListDto(TrackDto.class);
    }

    private Long getRecommendCategoryId(List<TrackDto> recommendTrackList){

        Map<Long, Integer> categoryStatistics = new HashMap<>();

        for (TrackDto track : recommendTrackList) {
            Long categoryId = track.getTrackCategoryId();
            categoryStatistics.put(categoryId, categoryStatistics.getOrDefault(categoryId, 0) + 1);
        }

        return categoryStatistics.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();

    }

    private List<Long> getRecommentTrackIdList(List<TrackDto> recommendTrackList) {
        return recommendTrackList.stream()
                .map(TrackDto::getTrackId)
                .collect(Collectors.toList());
    }

    private List<TrackDto> getMemberRecommendTrack(TrackRequestDto trackRequestDto ,Long categoryId, Long limit ){

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>)
                trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                        .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                        .findTrackNotInList(trackRequestDto.getTrackIdList())
                        .findIsTrackPrivacyFalse()
                        .findTracksByNotMemberId(trackRequestDto.getLoginMemberId())
                        .findCategoryTracks(categoryId)
                        .orderByTrackPlayCntDesc()
                        .orderByTrackLikeCntDesc()
                        .orderByTrackUploadDateDesc()
                        .limit(limit)
                        .fetchTrackListDto(TrackDto.class);
    }

    @Override
    public List<TrackDto> getRecommendTrack(Long loginMemberId) {

        List<TrackDto> recommendTrackList = new ArrayList<>();
        TrackRequestDto trackRequestDto = new TrackRequestDto();
        trackRequestDto.setLoginMemberId(loginMemberId);

        /* 좋아요 누른 트랙 사용자의 인기 트랙 조회 */
        recommendTrackList.addAll(getRecommendLikeMemberPopularTrack(trackRequestDto));

        /* 내가 팔로우 한 유저의 새로운 곡 조회 */
        recommendTrackList.addAll(getRecommendFollowMemberTrackList(trackRequestDto));

        /* 사용자의 검색 키워드가 포함 되는 트랙 조회 */
        recommendTrackList.addAll(getSearchRecommendTrackList(trackRequestDto.getLoginMemberId()));

        /* 중복 트랙 제거*/
        recommendTrackList = new ArrayList<>(recommendTrackList.stream()
                .collect(Collectors.toMap(
                        TrackDto::getTrackId,
                        item -> item,
                        (existing, replacement) -> existing
                ))
                .values());

        if (recommendTrackList.isEmpty()) {

            recommendTrackList.addAll(getRecommendPopularTrack(trackRequestDto.getLoginMemberId()));

        } else if (recommendTrackList.size() < 15) {

            long addRecommendTrackLimit = (15 - recommendTrackList.size());

            Long mostFrequentCategoryId = getRecommendCategoryId(recommendTrackList);

            trackRequestDto.setTrackIdList(getRecommentTrackIdList(recommendTrackList));

            recommendTrackList.addAll(getMemberRecommendTrack(trackRequestDto,mostFrequentCategoryId,addRecommendTrackLimit));
        }

        return recommendTrackList;
    }

    @Override
    public TrackResponseDto getUploadTrack(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);
        List<TrackDto> uploadTrackDtoList = new ArrayList<>();

        Long totalCount = trackSelectQueryBuilder
                .resetQuery()
                .from(QMemberTrack.memberTrack) // 동적으로 테이블 설정
                .findTracksByMemberId(trackRequestDto.getLoginMemberId()) // 조건 설정
                .fetchCount(); // COUNT 쿼리 실행

        if (totalCount != 0L) {
            uploadTrackDtoList = (List<TrackDto>) trackSelectQueryBuilder
                    .selectFrom(QMemberTrack.memberTrack)
                    .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                    .findTracksByMemberId(trackRequestDto.getLoginMemberId())
                    .orderByMemberTrackIdDesc()
                    .offset(trackRequestDto.getOffset())
                    .limit(trackRequestDto.getLimit())
                    .fetchTrackListDto(TrackDto.class);
        }

        return TrackResponseDto.builder()
                .trackList(uploadTrackDtoList)
                .totalCount(totalCount)
                .build();
    }


    private List<TrackDto> getRecommendFollowMemberTrackList(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (List<TrackDto>) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .joinMemberTrackWithMember()
                .joinTrackLikeWithMemberTrack(trackRequestDto.getLoginMemberId())
                .joinMemberFollowersAndFollow()
                .findIsTrackPrivacyFalse()
                .findFollowerTracks(trackRequestDto.getLoginMemberId())
                .orderByTrackUploadDateDesc()
                .limit(5L)
                .fetchTrackListDto(TrackDto.class);

    }


}
