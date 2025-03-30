package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.FcmSendDto;
import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.MemberSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.TrackLikeSelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.TrackSelectQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Boolean getTrackLikeStatus(TrackRequestDto trackRequestDto) {

        TrackLikeSelectQueryBuilder trackLikeSelectQueryBuilder = new TrackLikeSelectQueryBuilder(jpaQueryFactory);

        return trackLikeSelectQueryBuilder.selectFrom(QTrackLike.trackLike)
                .findTrackLikesByTrackId(trackRequestDto.getTrackId())
                .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                .fetchTrackLikeStatus();
    }

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

    /// 멤버 서비스 연결시 순환 참조 오류
    private Member getMemberEntity(Long memberId) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return (Member) memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberId(memberId)
                .fetchOne(Member.class);

    }

    @Override
    public Map<String, String> setTrackLike(TrackRequestDto trackRequestDto) {
        Map<String, String> hashMap = new HashMap<>();

        try {
            /* 해당 곡에 좋아요 엔티티 여부 */
            TrackLike trackLike = getTrackLikeEntity(trackRequestDto);

            if(trackLike == null) {
                Member member = getMemberEntity(trackRequestDto.getLoginMemberId());

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
                updateTrackLike(trackRequestDto, trackLike);
            }

            hashMap.put("status", "200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    @Override
    public Map<String, Object> getLikeTrackList(TrackRequestDto trackRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();

        try {
            TrackLikeSelectQueryBuilder trackLikeSelectQueryBuilder = new TrackLikeSelectQueryBuilder(jpaQueryFactory);

            List<TrackDto> likeTrackList = (List<TrackDto>) trackLikeSelectQueryBuilder
                    .selectFrom(QTrackLike.trackLike)
                    .findIsTrackPrivacyFalse()
                    .findIsTrackLikeStatusTrue()
                    .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                    .orderByMemberTrackIdDesc()
                    .offset(trackRequestDto.getOffset())
                    .limit(trackRequestDto.getLimit())
                    .fetchTrackLikeListDto(TrackDto.class);

            Long totalCount = trackLikeSelectQueryBuilder
                    .resetQuery()
                    .from(QTrackLike.trackLike)
                    .findIsTrackPrivacyFalse()
                    .findIsTrackLikeStatusTrue()
                    .findTrackLikesByMemberId(trackRequestDto.getLoginMemberId())
                    .fetchCount();

            hashMap.put("likeTrackList", likeTrackList);
            hashMap.put("totalCount",totalCount);
            hashMap.put("status","200");
        } catch(Exception e) {
            hashMap.put("status","500");
        }

        return hashMap;
    }

    private Long insertTrackLike(TrackRequestDto trackRequestDto) {

        Member member = Member.builder().memberId(trackRequestDto.getLoginMemberId()).build();

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto);

        insertTrackLikeStatus(memberTrack,member);

        insertTrackLikeCount(memberTrack);

        return memberTrack.getMember().getMemberId();

    }


    private void updateTrackLike(TrackRequestDto trackRequestDto, TrackLike trackLike) {

        MemberTrack memberTrack = getMemberTrackEntity(trackRequestDto);

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
                .set(qTrackLike.trackLikeStatus, !trackLike.getTrackLikeStatus())
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

        if (trackLike.getTrackLikeStatus()) {
            track.setTrackLikeCnt(track.getTrackLikeCnt() - 1);
        } else {
            track.setTrackLikeCnt(track.getTrackLikeCnt() + 1);

        }

        entityManager.merge(track);  // track 엔티티를 병합하여 업데이트
    }

}
