package com.skrrskrr.project.service;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FcmSendDto;
import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.UploadDto;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.MemberTrackRepository;
import com.skrrskrr.project.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class TrackServiceImpl implements TrackService {

    @PersistenceContext
    EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;
    private final TrackRepository trackRepository;
    private final MemberTrackRepository memberTrackRepository;
    private final FollowService followService;
    private final FireBaseService fireBaseService;
    private final CommentService commentService;
    private final ModelMapper modelMapper;


    @Override
    public Map<String, Object> saveTrack(UploadDto uploadDto) {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            // 트랙 엔티티 저장
            Track track = createTrack(uploadDto);
            // 트랙 연관 관계 설정
            setTrackRelationships(track, uploadDto);

            // 트랙 저장 후 반환 데이터 준비
            returnMap = prepareReturnMap(track);

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

        assert member != null;
        assert category != null;

        // Track 엔티티 생성
        Track track = Track.builder()
                .trackNm(uploadDto.getTrackNm())
                .trackInfo(uploadDto.getTrackInfo())
                .trackCategoryId(uploadDto.getTrackCategoryId())
                .isTrackPrivacy(uploadDto.isTrackPrivacy())
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


    @Override
    public Map<String,Object> updateTrackImage(UploadDto uploadDto) {
        Map<String,Object> hashMap = new HashMap<>();
        try {
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.trackImagePath, uploadDto.getUploadImagePath())
                    .where(qTrack.trackId.eq(uploadDto.getTrackId()))
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
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.trackInfo, trackRequestDto.getTrackInfo())
                    .where(qTrack.trackId.eq(trackRequestDto.getTrackId()))
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

        QTrack qTrack = QTrack.track;

        Long lastTrackId = jpaQueryFactory.select(
                        qTrack.trackId
                ).from(qTrack)
                .orderBy(qTrack.trackId.desc())
                .fetchFirst();


        return lastTrackId + 1;
    }



    @Override
    public Map<String, String> setTrackLike(TrackRequestDto trackRequestDto) {
        Map<String, String> hashMap = new HashMap<>();

        try {
            /* 해당 곡에 좋아요 여부 */
            TrackLike trackLike = getTrackLikeStatus(trackRequestDto);

            if(trackLike == null) {
                QMember qMember = QMember.member;
                Member member = jpaQueryFactory.selectFrom(qMember)
                        .where(qMember.memberId.eq(trackRequestDto.getLoginMemberId()))
                        .fetchOne();


                if (member != null ) {

                    Long fcmRecvMemberId = insertTrackLike(trackRequestDto);

                    FcmSendDto fcmSendDto = FcmSendDto.builder()
                            .title("알림")
                            .body(member.getMemberNickName() +  "님이 회원님의 곡에 좋아요를 눌렀습니다.")
                            .notificationType(1L)
                            .notificationTrackId(trackRequestDto.getTrackId())
                            .memberId(fcmRecvMemberId)
                            .build();

                    fireBaseService.sendPushNotification(fcmSendDto);
                }

              } else {
                  updateTrackLike(trackRequestDto);
              }

            hashMap.put("status", "200");
        } catch (Exception e) {
            hashMap.put("status", "500");

        }


        return hashMap;
    }


    @Override
    public Map<String, Object> getLikeTrack(TrackRequestDto trackRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();

        try {
            Long totalCount = getLikeTrackListCnt(trackRequestDto);

            List<TrackDto> likeTrackList = new ArrayList<>();
            if (totalCount != 0L) {
                likeTrackList = getLikeTrackList(trackRequestDto);
            }

            hashMap.put("likeTrackList", likeTrackList);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public List<TrackDto> getLikeTrackList(TrackRequestDto trackRequestDto) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.select(
                        Projections.bean(
                                TrackDto.class,
                                qTrackLike.memberTrack.track.trackId.as("trackId"),
                                qTrackLike.memberTrack.track.trackNm.as("trackNm"),
                                qTrackLike.memberTrack.track.trackPlayCnt.as("trackPlayCnt"),
                                qTrackLike.memberTrack.track.trackImagePath.as("trackImagePath"),
                                qTrackLike.memberTrack.track.trackCategoryId.as("trackCategoryId"),
                                qTrackLike.memberTrack.member.memberNickName.as("memberNickName"),
                                qTrackLike.memberTrack.member.memberId.as("memberId"),
                                qTrackLike.memberTrack.track.trackPath.as("trackPath"),
                                qTrackLike.memberTrack.track.trackLikeCnt.as("trackLikeCnt"),
                                qTrackLike.memberTrack.track.trackInfo.as("trackInfo"),
                                qTrackLike.trackLikeStatus.as("trackLikeStatus")
                        )
                )
                .from(qTrackLike)
                .where(qTrackLike.member.memberId.eq(trackRequestDto.getLoginMemberId())
                        .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                        .and(qTrackLike.trackLikeStatus.isTrue()))
                .limit(trackRequestDto.getLimit())
                .offset(trackRequestDto.getOffset())
                .orderBy(qTrackLike.memberTrack.memberTrackId.desc())
                .fetch();


    }

    @Override
    public Long getLikeTrackListCnt(TrackRequestDto trackRequestDto) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.select(qTrackLike.trackLikeId.count()).from(qTrackLike)
                .where(qTrackLike.member.memberId.eq(trackRequestDto.getLoginMemberId())
                        .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                        .and(qTrackLike.trackLikeStatus.isTrue()))
                .fetchOne();
    }


    @Override
    public Map<String, Object> setLockTrack(TrackRequestDto trackRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.isTrackPrivacy, trackRequestDto.isTrackPrivacy())
                    .where(qTrack.trackId.eq(trackRequestDto.getTrackId()))
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String, Object> getTrackInfo(TrackRequestDto trackRequestDto) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            TrackDto trackInfoDTO = jpaQueryFactory.select(
                            Projections.bean(
                                    TrackDto.class,
                                    qMemberTrack.track.trackId.as("trackId"),
                                    qMemberTrack.track.trackNm.as("trackNm"),
                                    qMemberTrack.track.isTrackPrivacy.as("isTrackPrivacy"),
                                    qMemberTrack.track.trackImagePath.as("trackImagePath"),
                                    qMemberTrack.track.trackPlayCnt.as("trackPlayCnt"),
                                    qMemberTrack.track.trackInfo.as("trackInfo"),
                                    qMemberTrack.track.trackPath.as("trackPath"),
                                    qMemberTrack.track.trackTime.as("trackTime"),
                                    qMemberTrack.track.trackLikeCnt.as("trackLikeCnt"),
                                    qMemberTrack.track.trackCategoryId.as("trackCategoryId"),
                                    qMemberTrack.track.trackUploadDate.as("trackUploadDate"),
                                    qMemberTrack.member.memberId.as("memberId"),
                                    qMemberTrack.member.memberNickName.as("memberNickName")
                            )
                    )
                    .from(qMemberTrack)
                    .where(qMemberTrack.track.trackId.eq(trackRequestDto.getTrackId()))
                    .fetchFirst();


            /* 해당 트랙에 좋아요 여부 */
            TrackLike trackLike = getTrackLikeStatus(trackRequestDto);

            /* 트랙의 댓글 수 조회 */
            Long commentCount = commentService.getTrackCommentCnt(trackRequestDto);

            /* 해당 트랙의 뮤지션을 내가 팔로워 했는지 */
            boolean isFollow = followService.isFollowCheck(trackInfoDTO.getMemberId(), trackRequestDto.getLoginMemberId());

            trackInfoDTO.setTrackLikeStatus(trackLike != null && trackLike.isTrackLikeStatus());
            trackInfoDTO.setCommentsCnt(commentCount);  // commentCount 값을 설정
            trackInfoDTO.setFollowMember(isFollow);  // isFollow 값을 설정
            trackInfoDTO.setTrackPrivacy(Boolean.TRUE.equals(trackInfoDTO.isTrackPrivacy()));


            hashMap.put("trackInfo",trackInfoDTO);
            hashMap.put("status","200");
        } catch(Exception e) {
          e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public Map<String, Object> getRecommendTrack(TrackRequestDto trackRequestDto) {
        Map<String,Object> hashMap = new HashMap<>();

        try{
            /* 추천트랙 조회 */
            List<TrackDto> recommendTrackDtoList = getRecommendTrackList(trackRequestDto);

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
            Long uploadTrackListCnt = getUploadTrackListCnt(trackRequestDto);

            List<TrackDto> uploadTrackDtoList = new ArrayList<>();
            if (uploadTrackListCnt != 0L) {
                uploadTrackDtoList = getUploadTrackList(trackRequestDto);
            }

            hashMap.put("uploadTrackList",uploadTrackDtoList);
            hashMap.put("totalCount",uploadTrackListCnt);
            hashMap.put("status","200");

        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public List<TrackDto> getUploadTrackList(TrackRequestDto trackRequestDto) {


        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;


        List<MemberTrack> uploadTrackList = jpaQueryFactory.selectFrom(qMemberTrack)
                .where(qMemberTrack.member.memberId.eq(trackRequestDto.getLoginMemberId()))
                .limit(trackRequestDto.getLimit())
                .offset(trackRequestDto.getOffset()) /// 조회된 10개의 항목을 무시하고 다음 데이터 조회
                .orderBy(qMemberTrack.memberTrackId.desc())
                .fetch();

        List<TrackDto> uploadTrackDtoList = new ArrayList<>();

        for (MemberTrack track : uploadTrackList) {
            TrackDto trackDto = modelMapper.map(track.getTrack(), TrackDto.class);
            uploadTrackDtoList.add(trackDto);
        }

        return uploadTrackDtoList;
    }

    @Override
    public Long getUploadTrackListCnt(TrackRequestDto trackRequestDto) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.select(qMemberTrack.track.trackId.count()).from(qMemberTrack)
                .where(qMemberTrack.member.memberId.eq(trackRequestDto.getLoginMemberId()))
                .fetchOne();
    }

    @Override
    public TrackLike getTrackLikeStatus(TrackRequestDto trackRequestDto) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.selectFrom(qTrackLike)
                .where(qTrackLike.memberTrack.track.trackId.eq(trackRequestDto.getTrackId())
                        .and(qTrackLike.member.memberId.eq(trackRequestDto.getLoginMemberId())))
                .fetchFirst();
    }

    @Override
    public List<TrackDto> getFollowMemberTrackList(TrackRequestDto trackRequestDto) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QMember qMember = QMember.member;
        QFollow qFollow = QFollow.follow;

        List<MemberTrack> queryResultFollowMemberTrack = jpaQueryFactory
                .selectFrom(qMemberTrack)
                .join(qMemberTrack.member, qMember) // MemberTrack과 Member를 조인
                .join(qMember.followers, qFollow)   // Member와 Follow를 조인 (팔로우 관계)
                .where(qFollow.following.memberId.eq(trackRequestDto.getLoginMemberId())
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse()))  // 내가 팔로우한 유저들
                .orderBy(qMemberTrack.track.trackUploadDate.desc()) // 최신 날짜 순으로 정렬
                .limit(trackRequestDto.getLimit())
                .fetch();

        List<TrackDto> followMemberTrackList = new ArrayList<>();
        for (MemberTrack memberTrack : queryResultFollowMemberTrack) {
            TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
            trackDto.setMemberNickName(memberTrack.getMember().getMemberNickName());
            trackDto.setMemberId(memberTrack.getMember().getMemberId());

            followMemberTrackList.add(trackDto);
        }
        return followMemberTrackList;

    }

    @Override
    public List<TrackDto> getTrendingTrackList(TrackRequestDto trackRequestDto){

        /// 트렌딩 음원 추천
        List<MemberTrack> queryResultTrackPlayDesc = getTrendingTrackPlayDesc(trackRequestDto);

        List<MemberTrack> queryResultTrackLikeDesc = getTrendingTrackLikeDesc(trackRequestDto);

        List<MemberTrack> combinedResult = mergeDescListTrack(queryResultTrackPlayDesc,queryResultTrackLikeDesc);

        List<TrackDto> trendingTrackList = new ArrayList<>();
        for (MemberTrack memberTrack : combinedResult) {
            TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
            trendingTrackList.add(trackDto);
        }

        return trendingTrackList;
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

        assert member != null;
        assert category != null;

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


    private Map<String, Object> prepareReturnMap(Track track) {
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("trackId", track.getTrackId());
        returnMap.put("status", "200");
        return returnMap;
    }


    private List<MemberTrack> mergeDescListTrack(List<MemberTrack> queryResultTrackPlayDesc, List<MemberTrack> queryResultTrackLikeDesc){

        List<MemberTrack> combinedResult = new ArrayList<>();

        combinedResult.addAll(queryResultTrackPlayDesc);
        combinedResult.addAll(queryResultTrackLikeDesc);

        // 중복 제거 (필요한 경우)
        return combinedResult.stream()
                .distinct()
                .collect(Collectors.toList());
    }


    private List<MemberTrack> getTrendingTrackPlayDesc(TrackRequestDto trackRequestDto){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.selectFrom(qMemberTrack)
                .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                ).orderBy(qMemberTrack.track.trackPlayCnt.desc())
                .limit(trackRequestDto.getLimit())
                .fetch();
    }


    private List<MemberTrack> getTrendingTrackLikeDesc(TrackRequestDto trackRequestDto){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.selectFrom(qMemberTrack)
                .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                ).orderBy(qMemberTrack.track.trackLikeCnt.desc())
                .limit(trackRequestDto.getLimit())
                .fetch();
    }


    private List<TrackDto> getRecommendTrackList(TrackRequestDto trackRequestDto) {


        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;


       return jpaQueryFactory.select(
                            Projections.bean(TrackDto.class,
                                    qMemberTrack.track.trackId.as("trackId"),
                                    qMemberTrack.track.trackImagePath.as("trackImagePath"),
                                    qMemberTrack.track.trackNm.as("trackNm"),
                                    qMemberTrack.member.memberNickName.as("memberNickName")
                            )
                        ).from(qMemberTrack)
                .where(qMemberTrack.track.trackCategoryId.eq( trackRequestDto.getTrackCategoryId())
                        .and(qMemberTrack.track.trackId.ne(trackRequestDto.getTrackId()))
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                )
                .limit(trackRequestDto.getLimit())
                .fetch();

    }




    private Long insertTrackLike(TrackRequestDto trackRequestDto) {

        Member member = Member.builder().memberId(trackRequestDto.getLoginMemberId()).build();

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto.getTrackId());

        insertTrackLikeStatus(memberTrack,member);

        insertTrackLikeCount(memberTrack);

        return memberTrack.getMember().getMemberId();

    }


    private void updateTrackLike(TrackRequestDto trackRequestDto) {

        TrackLike trackLike = getTrackLikeStatus(trackRequestDto);

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto.getTrackId());

        updateTrackLikeStatus(trackLike, memberTrack, trackRequestDto.getLoginMemberId());

        updateTrackLikeCount(trackLike);

    }

    private void insertTrackLikeStatus(MemberTrack memberTrack, Member member) {

        TrackLike insertTrackLike = new TrackLike();
        insertTrackLike.setMemberTrack(memberTrack);
        insertTrackLike.setMember(member);
        insertTrackLike.setTrackLikeStatus(true);

        entityManager.persist(insertTrackLike);

    }

    private void updateTrackLikeStatus(TrackLike trackLike, MemberTrack memberTrack, Long memberId) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        jpaQueryFactory.update(qTrackLike)
                .set(qTrackLike.trackLikeStatus, !trackLike.isTrackLikeStatus())
                .where(qTrackLike.memberTrack.eq(memberTrack)
                        .and(qTrackLike.member.memberId.eq(memberId)))
                .execute();
    }


    private void insertTrackLikeCount(MemberTrack memberTrack){
        Track track = memberTrack.getTrack();
        track.setTrackLikeCnt(track.getTrackLikeCnt() + 1);

        entityManager.merge(track);
    }

    private void updateTrackLikeCount(TrackLike trackLike) {
        Track track = trackLike.getMemberTrack().getTrack();

        if (trackLike.isTrackLikeStatus()) {
            track.setTrackLikeCnt(track.getTrackLikeCnt() + 1);
        } else {
            track.setTrackLikeCnt(track.getTrackLikeCnt() - 1);
        }

        entityManager.merge(track);  // track 엔티티를 병합하여 업데이트
    }


    private MemberTrack getMemberTrackEntity(Long trackId) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.selectFrom(qMemberTrack)
                .where(qMemberTrack.track.trackId.eq(trackId))
                .fetchFirst();
    }



    private BooleanExpression isUploadedThisWeekOrLastWeek(StringExpression trackUploadDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 날짜 기준으로 이번 주와 저번 주의 날짜 범위를 계산합니다.
        LocalDate today = LocalDate.now();

        // 이번 주 시작일과 종료일
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfThisWeek = startOfThisWeek.plusDays(6);  // 일요일까지

        // 저번 주 시작일과 종료일
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

        // 날짜를 문자열로 변환
        String startOfThisWeekStr = startOfThisWeek.format(formatter);
        String endOfThisWeekStr = endOfThisWeek.format(formatter);

        String startOfLastWeekStr = startOfLastWeek.format(formatter);
        String endOfLastWeekStr = endOfLastWeek.format(formatter);

        // 이번 주 또는 저번 주 업로드 날짜 필터
        BooleanExpression uploadThisWeek = trackUploadDate.between(startOfThisWeekStr, endOfThisWeekStr);
        BooleanExpression uploadLastWeek = trackUploadDate.between(startOfLastWeekStr, endOfLastWeekStr);

        return uploadThisWeek.or(uploadLastWeek);
    }


}
