package com.skrrskrr.project.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.TrackDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.*;
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
    EntityManager em;

    private final TrackRepository trackRepository;
    private final MemberTrackRepository memberTrackRepository;
    private final FollowService followService;
    private final FireBaseService fireBaseService;

    @Override
    public Map<String,Object> trackUpload(UploadDTO uploadDTO) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        Map<String,Object> returnMap = new HashMap<>();

        QMember qMember = QMember.member;
        QCategory qCategory = QCategory.category;

        MemberTrack memberTrack = new MemberTrack();
        TrackCategory trackCategory = new TrackCategory();

        Member member = jpaQueryFactory.selectFrom(qMember).where(qMember.memberId.eq(uploadDTO.getMemberId())).fetchOne();
        Category category = jpaQueryFactory.selectFrom(qCategory).where(qCategory.trackCategoryId.eq(uploadDTO.getTrackCategoryId())).fetchOne();


        assert member != null;
        assert category != null;
        try {
            ///반복문으로 변경

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

            Long trackId = trackRepository.save(track).getTrackId();

            memberTrack.setTrack(track);
            memberTrack.setMember(member);

            track.getMemberTrackList().add(memberTrack);
            member.getMemberTrackList().add(memberTrack);

            trackCategory.setTrack(track);
            trackCategory.setCategory(category);

            track.getTrackCategoryList().add(trackCategory);
            category.getTrackCategoryList().add(trackCategory);

            memberTrackRepository.save(memberTrack);

            returnMap.put("trackId",trackId);
            returnMap.put("status","200");
            return returnMap;
        } catch (Exception e) {
            returnMap.put("isStatus","500");
            return returnMap;
        }


    }

    @Override
    public Map<String,Object> setTrackImage(UploadDTO uploadDTO) {
        Map<String,Object> hashMap = new HashMap<>();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            QTrack qTrack = QTrack.track;


            queryFactory.update(qTrack)
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
        Map<String,Object> resultMap = new HashMap<>();

        try {

            JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            QTrack qTrack = QTrack.track;

            queryFactory.update(qTrack)
                    .set(qTrack.trackInfo, trackDTO.getTrackInfo())
                    .where(qTrack.trackId.eq(trackDTO.getTrackId()))
                    .execute();

            resultMap.put("status","200");
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("status","500");
            return resultMap;
        }
    }

    @Override
    public Map<String, String> setTrackLike(Long memberId, Long trackId) {
        Map<String, String> hashMap = new HashMap<>();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QTrackLike qTrackLike = QTrackLike.trackLike;
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QMember qMember = QMember.member;

        try {

            /// 해당 곡에 좋아요 기록이 있는지 조회
            TrackLike trackLike = jpaQueryFactory.selectFrom(qTrackLike)
                    .where(qTrackLike.memberTrack.track.trackId.eq(trackId)
                            .and(qTrackLike.member.memberId.eq(memberId)))
                    .fetchFirst();


            MemberTrack memberTrack = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(qMemberTrack.track.trackId.eq(trackId))
                    .fetchFirst();

            log.info(memberTrack);

            /// 없므면 insert 추가'
            if (trackLike == null) {

                Member member = Member.builder()
                        .memberId(memberId)
                        .build();

                TrackLike insertTrackLike = new TrackLike();
                insertTrackLike.setMemberTrack(memberTrack);
                insertTrackLike.setMember(member);
                insertTrackLike.setTrackLikeStatus(true);

                em.persist(insertTrackLike);

                Track track = memberTrack.getTrack();
                track.setTrackLikeCnt(track.getTrackLikeCnt() + 1);

                em.merge(track);

                try{
                    fireBaseService.sendPushNotification(
                            memberTrack.getMember().getMemberId(),
                            "알림",
                            "다른 사용자가 회원님의 곡에 좋아요를 눌렀습니다.",
                            1L,
                                trackId,
                            null,
                            null
                            );
                } catch(Exception e) {
                    e.printStackTrace(); 
                }


            } else {
                boolean trackLikeStatus = trackLike.isTrackLikeStatus();

                jpaQueryFactory.update(qTrackLike)
                        .set(qTrackLike.trackLikeStatus, !trackLikeStatus)
                        .where(qTrackLike.memberTrack.eq(memberTrack)
                                .and(qTrackLike.member.memberId.eq(memberId)))
                        .execute();


                Track track = trackLike.getMemberTrack().getTrack();
                if (trackLikeStatus){
                    track.setTrackLikeCnt(track.getTrackLikeCnt() - 1);
                } else {
                    track.setTrackLikeCnt(track.getTrackLikeCnt() + 1);
                }
                em.merge(track);

            }

            hashMap.put("status", "200");
        } catch (Exception e) {
            hashMap.put("status", "500");

        }


        return hashMap;
    }

    @Override
    public Map<String, Object> getLikeTrack(Long memberId, Long listIndex) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QTrackLike qTrackLike = QTrackLike.trackLike;

        Map<String, Object> result = new HashMap<>();

        try {
            List<TrackLike> trackLike = jpaQueryFactory.selectFrom(qTrackLike)
                    .where(qTrackLike.member.memberId.eq(memberId)
                            .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                            .and(qTrackLike.trackLikeStatus.isTrue()))
                    .limit(20)
                    .offset(listIndex)
                    .orderBy(qTrackLike.memberTrack.memberTrackId.desc())
                    .fetch();

            Long trackLikeCnt =  jpaQueryFactory.select(qTrackLike.trackLikeId.count()).from(qTrackLike)
                    .where(qTrackLike.member.memberId.eq(memberId)
                            .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                            .and(qTrackLike.trackLikeStatus.isTrue()))
                    .fetchOne();


            List<TrackDTO> likeTrackList = new ArrayList<>();

            for (TrackLike track : trackLike) {
                TrackDTO trackDTO = TrackDTO.builder()
                        .trackId(track.getMemberTrack().getTrack().getTrackId())
                        .trackNm(track.getMemberTrack().getTrack().getTrackNm())
                        .trackPlayCnt(track.getMemberTrack().getTrack().getTrackPlayCnt())
                        .trackImagePath(track.getMemberTrack().getTrack().getTrackImagePath())
                        .trackCategoryId(track.getMemberTrack().getTrack().getTrackCategoryId())
                        .memberNickName(track.getMemberTrack().getMember().getMemberNickName())
                        .memberId(track.getMemberTrack().getMember().getMemberId())
                        .trackPath(track.getMemberTrack().getTrack().getTrackPath())
                        .trackLikeCnt(track.getMemberTrack().getTrack().getTrackLikeCnt())
                        .trackInfo(track.getMemberTrack().getTrack().getTrackInfo())
                        .trackLikeStatus(true)
                        .build();

                likeTrackList.add(trackDTO);
            }
            result.put("likeTrackList", likeTrackList);
            result.put("totalCount",trackLikeCnt);
            result.put("status","200");
        } catch(Exception e) {
            result.put("status","500");
        }

        return result;
    }


    @Override
    public Map<String, Object> getTrackInfo(Long trackId,Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QTrack qTrack = QTrack.track;
        QComment qComment = QComment.comment;
        QTrackLike qTrackLike = QTrackLike.trackLike;

        Map<String,Object> hashMap = new HashMap<>();

        try {
            Tuple trackInfo = jpaQueryFactory.select(
                            qMemberTrack.track.trackId,
                            qMemberTrack.track.trackNm,
                            qMemberTrack.track.isTrackPrivacy,
                            qMemberTrack.track.trackImagePath,
                            qMemberTrack.track.trackPlayCnt,
                            qMemberTrack.track.trackInfo,
                            qMemberTrack.track.trackPath,
                            qMemberTrack.track.trackTime,
                            qMemberTrack.track.trackLikeCnt,
                            qMemberTrack.track.trackCategoryId,
                            qMemberTrack.track.trackUploadDate,
                            qMemberTrack.member.memberId,
                            qMemberTrack.member.memberNickName
                    )
                    .from(qMemberTrack)
                    .where(qMemberTrack.track.trackId.eq(trackId))
                    .fetchFirst();

            boolean trackLikeStatus = Boolean.TRUE.equals(jpaQueryFactory.select(qTrackLike.trackLikeStatus).from(qTrackLike)
                    .where(qTrackLike.memberTrack.track.trackId.eq(trackInfo.get(qMemberTrack.track.trackId))
                            .and(qTrackLike.member.memberId.eq(memberId)))
                    .fetchOne());


            List<Tuple> recommendTrack = jpaQueryFactory.select(
                            qMemberTrack.track.trackId,
                            qMemberTrack.track.trackImagePath,
                            qMemberTrack.track.trackNm,
                            qMemberTrack.member.memberNickName
                    )
                    .from(qMemberTrack)
                    .where(qMemberTrack.track.trackCategoryId.eq( trackInfo.get(qMemberTrack.track.trackCategoryId))
                            .and(qMemberTrack.track.trackId.ne( trackInfo.get(qMemberTrack.track.trackId)))
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                    )
                    .limit(5)
                    .fetch();

            List<TrackDTO> recommendTrackDTOList = new ArrayList<>();

            for (Tuple track : recommendTrack) {

                TrackDTO recommendTrackDTO = TrackDTO.builder()
                        .trackId(track.get(qMemberTrack.track.trackId))
                        .trackNm(track.get(qMemberTrack.track.trackNm))
                        .trackImagePath(track.get(qMemberTrack.track.trackImagePath))
                        .memberNickName(track.get(qMemberTrack.member.memberNickName))
                        .build();

                recommendTrackDTOList.add(recommendTrackDTO);
            }


            Long commentCount = jpaQueryFactory
                    .select(qComment.count()) // 댓글의 개수를 계산
                    .from(qMemberTrack)
                    .leftJoin(qMemberTrack.track.commentList, qComment) // Track과 commentList를 조인
                    .where(qMemberTrack.track.trackId.eq(trackId))
                    .fetchOne(); // 결과를 하나의 값으로 가져옵니다.

            TrackDTO trackInfoDTO = TrackDTO.builder()
                    .trackId(trackInfo.get(qMemberTrack.track.trackId))
                    .trackNm(trackInfo.get(qMemberTrack.track.trackNm))
                    .isTrackPrivacy(Boolean.TRUE.equals(trackInfo.get(qMemberTrack.track.isTrackPrivacy)))
                    .trackImagePath(trackInfo.get(qMemberTrack.track.trackImagePath))
                    .trackPlayCnt(trackInfo.get(qMemberTrack.track.trackPlayCnt))
                    .trackInfo(trackInfo.get(qMemberTrack.track.trackInfo))
                    .trackPath(trackInfo.get(qMemberTrack.track.trackPath))
                    .trackTime(trackInfo.get(qMemberTrack.track.trackTime))
                    .trackUploadDate(trackInfo.get(qMemberTrack.track.trackUploadDate))
                    .trackLikeCnt(trackInfo.get(qMemberTrack.track.trackLikeCnt))
                    .trackCategoryId(trackInfo.get(qMemberTrack.track.trackCategoryId))
                    .memberId(trackInfo.get(qMemberTrack.member.memberId))
                    .memberNickName(trackInfo.get(qMemberTrack.member.memberNickName))
                    .trackLikeStatus(trackLikeStatus)
                    .commentsCnt(commentCount)
                    .build();

            Map<String,Object> isFollowMap = followService.isFollowCheck(trackInfoDTO.getMemberId(),memberId);

            trackInfoDTO.setFollowMember((Boolean) isFollowMap.get("followStatus"));

            hashMap.put("trackInfo",trackInfoDTO);
            hashMap.put("recommendTrack",recommendTrackDTOList);
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
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        try {
            List<MemberTrack> uploadTrackList = jpaQueryFactory.selectFrom(qMemberTrack)
                    .where(qMemberTrack.member.memberId.eq(memberId))
                    .limit(20)
                    .offset(listIndex) /// 조회된 10개의 항목을 무시하고 다음 데이터 조회
                    .orderBy(qMemberTrack.memberTrackId.desc())
                    .fetch();

            Long uploadTrackCnt = jpaQueryFactory.select(qMemberTrack.track.trackId.count()).from(qMemberTrack)
                    .where(qMemberTrack.member.memberId.eq(memberId))
                    .fetchOne();

            List<TrackDTO> uploadTrackDTOList = new ArrayList<>();

            for (MemberTrack track : uploadTrackList) {
                TrackDTO trackDTO = TrackDTO.builder()
                        .trackId(track.getTrack().getTrackId())
                        .trackTime(track.getTrack().getTrackTime())
                        .trackNm(track.getTrack().getTrackNm())
                        .isTrackPrivacy(track.getTrack().isTrackPrivacy())
                        .trackCategoryId(track.getTrack().getTrackCategoryId())
                        .trackLikeCnt(track.getTrack().getTrackLikeCnt())
                        .trackImagePath(track.getTrack().getTrackImagePath())
                        .trackPlayCnt(track.getTrack().getTrackPlayCnt())
                        .build();


                uploadTrackDTOList.add(trackDTO);
            }

            hashMap.put("uploadTrackList",uploadTrackDTOList);
            hashMap.put("totalCount",uploadTrackCnt);
            hashMap.put("status","200");

        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }


}
