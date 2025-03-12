package com.skrrskrr.project.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FcmSendDTO;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.UploadDTO;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


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
    public Map<String, Object> saveTrack(UploadDTO uploadDTO) {
        Map<String, Object> returnMap = new HashMap<>();

        try {
            // 트랙 엔티티 저장
            Track track = createTrack(uploadDTO);

            // 트랙 연관 관계 설정
            setTrackRelationships(track, uploadDTO);

            // 트랙 저장 후 반환 데이터 준비
            returnMap = prepareReturnMap(track);

        } catch (Exception e) {
            returnMap.put("isStatus", "500");
        }

        return returnMap;
    }


    private Track createTrack(UploadDTO uploadDTO) {

        QMember qMember = QMember.member;
        QCategory qCategory = QCategory.category;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(uploadDTO.getMemberId()))
                .fetchOne();

        Category category = jpaQueryFactory.selectFrom(qCategory)
                .where(qCategory.trackCategoryId.eq(uploadDTO.getTrackCategoryId()))
                .fetchOne();

        assert member != null;
        assert category != null;

        // Track 엔티티 생성
        Track track = Track.builder()
                .trackNm(uploadDTO.getTrackNm())
                .trackInfo(uploadDTO.getTrackInfo())
                .trackCategoryId(uploadDTO.getTrackCategoryId())
                .isTrackPrivacy(uploadDTO.isTrackPrivacy())
                .trackTime(uploadDTO.getTrackTime())
                .trackPath(uploadDTO.getUploadFilePath())
                .trackImagePath(uploadDTO.getUploadImagePath())
                .trackLikeCnt(0L)
                .trackPlayCnt(0L)
                .trackUploadDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .memberTrackList(new ArrayList<>())
                .trackCategoryList(new ArrayList<>())
                .build();

        // 트랙 저장
        return trackRepository.save(track);
    }


    private void setTrackRelationships(Track track, UploadDTO uploadDTO) {

        QMember qMember = QMember.member;
        QCategory qCategory = QCategory.category;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(uploadDTO.getMemberId()))
                .fetchOne();

        Category category = jpaQueryFactory.selectFrom(qCategory)
                .where(qCategory.trackCategoryId.eq(uploadDTO.getTrackCategoryId()))
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


    @Override
    public Map<String,Object> updateTrackImage(UploadDTO uploadDTO) {
        Map<String,Object> hashMap = new HashMap<>();
        try {
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.trackImagePath, uploadDTO.getUploadImagePath())
                    .where(qTrack.trackId.eq(uploadDTO.getTrackId()))
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


    @Override
    public Map<String, Object> setTrackinfo(TrackDTO trackDTO) {
        Map<String,Object> hashMap = new HashMap<>();

        try {
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.trackInfo, trackDTO.getTrackInfo())
                    .where(qTrack.trackId.eq(trackDTO.getTrackId()))
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
    public Map<String, String> setTrackLike(Long memberId, Long trackId) {
        Map<String, String> hashMap = new HashMap<>();

        try {
            /* 해당 곡에 좋아요 여부 */
            TrackLike trackLike = getTrackLikeStatus(memberId,trackId);

            if(trackLike == null) {

                Long fcmRecvMemberId = insertTrackLike(memberId,trackId);

                FcmSendDTO fcmSendDTO = FcmSendDTO.builder()
                        .title("알림")
                        .body("다른 사용자가 회원님의 곡에 좋아요를 눌렀습니다.")
                        .notificationType(1L)
                        .notificationTrackId(trackId)
                        .memberId(fcmRecvMemberId)
                        .build();

                fireBaseService.sendPushNotification(fcmSendDTO);

              } else {
                  updateTrackLike(memberId,trackId);
              }

            hashMap.put("status", "200");
        } catch (Exception e) {
            hashMap.put("status", "500");

        }


        return hashMap;
    }




    private Long insertTrackLike(Long memberId, Long trackId) {

        Member member = Member.builder().memberId(memberId).build();

        MemberTrack memberTrack = getMemberTrackEntity(trackId);

        insertTrackLikeStatus(memberTrack,member);

        insertTrackLikeCount(memberTrack);

        return memberTrack.getMember().getMemberId();

    }


    private void updateTrackLike(Long memberId, Long trackId) {

        TrackLike trackLike = getTrackLikeStatus(memberId, trackId);

        MemberTrack memberTrack = getMemberTrackEntity(trackId);

        updateTrackLikeStatus(trackLike, memberTrack, memberId);

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


    @Override
    public Map<String, Object> getLikeTrack(Long memberId, Long listIndex) {

        Map<String, Object> hashMap = new HashMap<>();

        try {
            Long totalCount = getLikeTrackListCnt(memberId);

            List<TrackDTO> likeTrackList = new ArrayList<>();
            if (totalCount != 0L) {
                likeTrackList = getLikeTrackList(memberId,listIndex);
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
    public List<TrackDTO> getLikeTrackList(Long memberId, Long listIndex) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.select(
                        Projections.bean(
                                TrackDTO.class,
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
                .where(qTrackLike.member.memberId.eq(memberId)
                        .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                        .and(qTrackLike.trackLikeStatus.isTrue()))
                .limit(20)
                .offset(listIndex)
                .orderBy(qTrackLike.memberTrack.memberTrackId.desc())
                .fetch();


    }

    @Override
    public Long getLikeTrackListCnt(Long memberId) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.select(qTrackLike.trackLikeId.count()).from(qTrackLike)
                .where(qTrackLike.member.memberId.eq(memberId)
                        .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                        .and(qTrackLike.trackLikeStatus.isTrue()))
                .fetchOne();
    }


    @Override
    public Map<String, Object> setLockTrack(TrackDTO trackDTO) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            QTrack qTrack = QTrack.track;

            jpaQueryFactory.update(qTrack)
                    .set(qTrack.isTrackPrivacy, trackDTO.isTrackPrivacy())
                    .where(qTrack.trackId.eq(trackDTO.getTrackId()))
                    .execute();

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String, Object> getTrackInfo(Long trackId,Long memberId) {


        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        Map<String,Object> hashMap = new HashMap<>();

        try {

            TrackDTO trackInfoDTO = jpaQueryFactory.select(
                            Projections.bean(
                                    TrackDTO.class,
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
                    .where(qMemberTrack.track.trackId.eq(trackId))
                    .fetchFirst();


            /* 해당 트랙에 좋아요 여부 */
            TrackLike trackLike = getTrackLikeStatus(memberId,trackId);

            /* 추천트랙 조회 */
            List<TrackDTO> recommendTrackDtoList = getRecommendTrackList(memberId,trackInfoDTO.getTrackId(), trackInfoDTO.getTrackCategoryId());

            /* 트랙의 댓글 수 조회 */
            Long commentCount = commentService.getTrackCommentCnt(memberId,trackId);

            /* 해당 트랙의 뮤지션을 내가 팔로워 했는지 */
            boolean isFollow = followService.isFollowCheck(trackInfoDTO.getMemberId(), memberId);

            trackInfoDTO.setTrackLikeStatus(trackLike != null && trackLike.isTrackLikeStatus());
            trackInfoDTO.setCommentsCnt(commentCount);  // commentCount 값을 설정
            trackInfoDTO.setFollowMember(isFollow);  // isFollow 값을 설정
            trackInfoDTO.setTrackPrivacy(Boolean.TRUE.equals(trackInfoDTO.isTrackPrivacy()));


            hashMap.put("trackInfo",trackInfoDTO);
            hashMap.put("recommendTrackList",recommendTrackDtoList);
            hashMap.put("status","200");
        } catch(Exception e) {
          e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    @Override
    public Map<String,Object> getUploadTrack(Long memberId, Long listIndex) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            Long uploadTrackListCnt = getUploadTrackListCnt(memberId);

            List<TrackDTO> uploadTrackDtoList = new ArrayList<>();
            if (uploadTrackListCnt != 0L) {
                uploadTrackDtoList = getUploadTrackList(memberId,listIndex);
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
    public List<TrackDTO> getUploadTrackList(Long memberId, Long listIndex) {


        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;


        List<MemberTrack> uploadTrackList = jpaQueryFactory.selectFrom(qMemberTrack)
                .where(qMemberTrack.member.memberId.eq(memberId))
                .limit(20)
                .offset(listIndex) /// 조회된 10개의 항목을 무시하고 다음 데이터 조회
                .orderBy(qMemberTrack.memberTrackId.desc())
                .fetch();

        List<TrackDTO> uploadTrackDtoList = new ArrayList<>();

        for (MemberTrack track : uploadTrackList) {
            TrackDTO trackDTO = modelMapper.map(track.getTrack(), TrackDTO.class);
            uploadTrackDtoList.add(trackDTO);
        }

        return uploadTrackDtoList;
    }

    @Override
    public Long getUploadTrackListCnt(Long memberId) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.select(qMemberTrack.track.trackId.count()).from(qMemberTrack)
                .where(qMemberTrack.member.memberId.eq(memberId))
                .fetchOne();
    }

    @Override
    public TrackLike getTrackLikeStatus(Long memberId, Long trackId) {

        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory.selectFrom(qTrackLike)
                .where(qTrackLike.memberTrack.track.trackId.eq(trackId)
                        .and(qTrackLike.member.memberId.eq(memberId)))
                .fetchFirst();
    }


    @Override
    public List<TrackDTO> getRecommendTrackList(Long memberId, Long trackId, Long trackCategoryId) {


        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;


       return jpaQueryFactory.select(
                            Projections.bean(TrackDTO.class,
                                    qMemberTrack.track.trackId.as("trackId"),
                                    qMemberTrack.track.trackImagePath.as("trackImagePath"),
                                    qMemberTrack.track.trackNm.as("trackNm"),
                                    qMemberTrack.member.memberNickName.as("memberNickName")
                            )
                        ).from(qMemberTrack)
                .where(qMemberTrack.track.trackCategoryId.eq( trackCategoryId)
                        .and(qMemberTrack.track.trackId.ne(trackId))
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                )
                .limit(5)
                .fetch();

    }

}
