package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QComment;
import com.skrrskrr.project.entity.QTrack;
import com.skrrskrr.project.entity.QTrackLike;

import java.util.List;

public class TrackLikeSelectQueryBuilder extends ComnSelectQueryBuilder<TrackLikeSelectQueryBuilder> {


    QTrackLike qTrackLike = QTrackLike.trackLike;


    // 생성자: JPAQueryFactory 주입
    public TrackLikeSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);  // 상위 클래스의 생성자 호출
    }

    public TrackLikeSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */


    public TrackLikeSelectQueryBuilder findIsTrackPrivacyFalse() {

        this.query.where(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse());

        return this;
    }

    public TrackLikeSelectQueryBuilder findIsTrackPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {

        this.query.where(
                qTrackLike.memberTrack.track.isTrackPrivacy.isFalse()
                        .or(qTrackLike.memberTrack.member.memberId.eq(loginMemberId))
        );
        return this;
    }

    public TrackLikeSelectQueryBuilder findIsTrackLikeStatusTrue() {
        this.query.where(qTrackLike.trackLikeStatus.isTrue());
        return this;
    }


    public TrackLikeSelectQueryBuilder findTrackLikesByTrackId(Long trackId) {
        throwIfConditionNotMet(trackId != null);

        this.query.where(qTrackLike.memberTrack.track.trackId.eq(trackId));
        return this;
    }

    public TrackLikeSelectQueryBuilder findTrackLikesByMemberId(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qTrackLike.member.memberId.eq(loginMemberId));
        return this;
    }



    /** --------------------------join -------------------------------------------*/



    /** --------------------------ordeBy ---------------------------------------- */



    public TrackLikeSelectQueryBuilder orderByTrackLikeDateDesc() {
        this.query.orderBy(qTrackLike.trackLikeDate.desc());
        return this;
    }

    /** -------------------------fetch ------------------------------------------- */



    public List<Long> fetchTrackByMemberIdList() {
        return this.query.select(
                qTrackLike.memberTrack.member.memberId
        ).fetch();
    }


    public <T> List<?> fetchTrackLikeListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qTrackLike.memberTrack.track.trackId,
                        qTrackLike.memberTrack.track.trackNm,
                        qTrackLike.memberTrack.track.trackPlayCnt,
                        qTrackLike.memberTrack.track.trackTime,
                        qTrackLike.memberTrack.track.trackImagePath,
                        qTrackLike.memberTrack.track.isTrackPrivacy,
                        qTrackLike.memberTrack.track.trackCategoryId,
                        qTrackLike.memberTrack.member.memberNickName,
                        qTrackLike.memberTrack.member.memberImagePath,
                        qTrackLike.memberTrack.member.memberId,
                        qTrackLike.memberTrack.track.trackPath,
                        qTrackLike.memberTrack.track.trackLikeCnt,
                        qTrackLike.memberTrack.track.trackInfo,
                        qTrackLike.trackLikeStatus
                )
        ).fetch();
    }


    public <T> T fetchTrackLike(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qTrackLike.trackLikeStatus,
                        qTrackLike.memberTrack
                )
        ).fetchFirst();
    }

}
