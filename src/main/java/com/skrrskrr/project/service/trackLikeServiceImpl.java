package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.MemberSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.TrackLikeSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.TrackSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.TrackLikeUpdateQueryBuilder;
import com.skrrskrr.project.queryBuilder.update.TrackUpdateQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
@SuppressWarnings("unchecked")
public class trackLikeServiceImpl implements TrackLikeService{

    @PersistenceContext
    EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;
    private final FireBaseService fireBaseService;

    @Override
    public TrackLike getTrackLikeEntity(TrackRequestDto trackRequestDto) {

        TrackLikeSelectQueryBuilder trackLikeSelectQueryBuilder = new TrackLikeSelectQueryBuilder(jpaQueryFactory);

        return trackLikeSelectQueryBuilder.selectFrom(QTrackLike.trackLike)
                .findTrackLikesByTrackId(trackRequestDto.getTrackId())
                .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                .fetchTrackLike(TrackLike.class);
    }

    private MemberTrack getMemberTrackEntity(TrackRequestDto trackRequestDto) {

        TrackSelectQueryBuilder trackSelectQueryBuilder = new TrackSelectQueryBuilder(jpaQueryFactory);

        return (MemberTrack) trackSelectQueryBuilder.selectFrom(QMemberTrack.memberTrack)
                .findTrackByTrackId(trackRequestDto.getTrackId())
                .fetchFirst(MemberTrack.class);
    }

    private Member getMemberEntity(Long memberId) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return (Member) memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberId(memberId)
                .fetchOne(Member.class);

    }

    @Override
    public void setTrackLike(TrackRequestDto trackRequestDto) {

        TrackLike trackLike = getTrackLikeEntity(trackRequestDto);

        if (trackLike == null) {
            Member member = getMemberEntity(trackRequestDto.getLoginMemberId());

            if (member != null ) {
                Long fcmRecvMemberId = insertTrackLike(trackRequestDto);

                try {
                    if (!Objects.equals(trackRequestDto.getLoginMemberId(), fcmRecvMemberId)) {
                        FcmSendDto fcmSendDto = FcmSendDto.builder()
                                .title("알림")
                                .body(member.getMemberNickName() +  "님이 회원님의 곡에 좋아요를 눌렀습니다.")
                                .notificationType(1L)
                                .notificationIsView(false)
                                .notificationTrackId(trackRequestDto.getTrackId())
                                .notificationMemberId(trackRequestDto.getLoginMemberId())
                                .memberId(fcmRecvMemberId) /// 알림 받을 멤버
                                .build();

                        fireBaseService.sendPushNotification(fcmSendDto);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            updateTrackLike(trackRequestDto, trackLike);
        }
    }

    @Override
    public List<Long> getRecommendLikeTrackMemberId(TrackRequestDto trackRequestDto) {

        TrackLikeSelectQueryBuilder trackLikeSelectQueryBuilder = new TrackLikeSelectQueryBuilder(jpaQueryFactory);

        return trackLikeSelectQueryBuilder
                .selectFrom(QTrackLike.trackLike)
                .findIsTrackPrivacyFalse()
                .findIsTrackLikeStatusTrue()
                .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                .findTrackByNotEqualMemberId(trackRequestDto.getLoginMemberId())
                .orderByTrackLikeDateDesc()
                .distinct()
                .limit(5L)
                .fetchTrackByMemberIdList();
    }


    @Override
    public TrackResponseDto getLikeTrackList(TrackRequestDto trackRequestDto) {

        TrackLikeSelectQueryBuilder trackLikeSelectQueryBuilder = new TrackLikeSelectQueryBuilder(jpaQueryFactory);
        List<TrackDto> likeTrackList = new ArrayList<>();

        Long totalCount = trackLikeSelectQueryBuilder
                .resetQuery()
                .from(QTrackLike.trackLike)
                .findIsTrackPrivacyFalseOrLoginMemberIdEqual(trackRequestDto.getLoginMemberId())
                .findIsTrackLikeStatusTrue()
                .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                .fetchCount();

        if (totalCount != 0L) {
            likeTrackList = (List<TrackDto>) trackLikeSelectQueryBuilder
                    .selectFrom(QTrackLike.trackLike)
                    .findIsTrackPrivacyFalseOrLoginMemberIdEqual(trackRequestDto.getLoginMemberId())
                    .findIsTrackLikeStatusTrue()
                    .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                    .orderByTrackLikeDateDesc()
                    .offset(trackRequestDto.getOffset())
                    .limit(trackRequestDto.getLimit())
                    .fetchTrackLikeListDto(TrackDto.class);
        }

        return TrackResponseDto.builder()
                .trackList(likeTrackList)
                .totalCount(totalCount)
                .build();
    }


    private void updateTrackLike(TrackRequestDto trackRequestDto, TrackLike trackLike) {

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto);

        updateTrackLikeStatus(trackLike, memberTrack, trackRequestDto.getLoginMemberId());

        updateTrackLikeCount(trackLike.getMemberTrack().getTrack(), trackLike.getIsTrackLikeStatus());

    }


    private void updateTrackLikeStatus(TrackLike trackLike, MemberTrack memberTrack, Long loginMemberId) {

        TrackLikeUpdateQueryBuilder trackLikeUpdateQueryBuilder = new TrackLikeUpdateQueryBuilder(entityManager);

        trackLikeUpdateQueryBuilder.setEntity(QTrackLike.trackLike)
                .set(QTrackLike.trackLike.isTrackLikeStatus, !trackLike.getIsTrackLikeStatus())
                .set(QTrackLike.trackLike.trackLikeDate, LocalDateTime.now())
                .findMemberTrackByMemberTrackId(memberTrack.getMemberTrackId())
                .findTrackLikeByMemberMemberId(loginMemberId)
                .execute();

    }


    private Long insertTrackLike(TrackRequestDto trackRequestDto) {

        Member member = Member.builder().memberId(trackRequestDto.getLoginMemberId()).build();

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto);

        insertTrackLikeStatus(memberTrack,member);

        updateTrackLikeCount(memberTrack.getTrack(), false);

        return memberTrack.getMember().getMemberId();

    }


    private void insertTrackLikeStatus(MemberTrack memberTrack, Member member) {

        TrackLike insertTrackLike = new TrackLike();
        insertTrackLike.setMemberTrack(memberTrack);
        insertTrackLike.setMember(member);
        insertTrackLike.setIsTrackLikeStatus(true);
        insertTrackLike.setTrackLikeDate(LocalDateTime.now());

        entityManager.persist(insertTrackLike);

    }


    private void updateTrackLikeCount(Track track, Boolean isTrackLikeStatus) {
        TrackUpdateQueryBuilder trackUpdateQueryBuilder = new TrackUpdateQueryBuilder(entityManager);

        trackUpdateQueryBuilder.setEntity(QTrack.track)
                .set(QTrack.track.trackLikeCnt, isTrackLikeStatus
                        ? track.getTrackLikeCnt() - 1
                        : track.getTrackLikeCnt() + 1)
                .findTrackByTrackId(track.getTrackId())
                .execute();
    }

}
