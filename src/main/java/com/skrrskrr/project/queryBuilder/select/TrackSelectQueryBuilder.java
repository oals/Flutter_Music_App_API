package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

public class TrackSelectQueryBuilder extends ComnSelectQueryBuilder<TrackSelectQueryBuilder> {


    QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
    QMember qMember = QMember.member;

    // 생성자: JPAQueryFactory 주입
    public TrackSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);  // 상위 클래스의 생성자 호출
    }

    public TrackSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */

    public TrackSelectQueryBuilder findTracksByMemberId(Long memberId) {
        if (memberId != null) {
            this.query.where(qMemberTrack.member.memberId.eq(memberId));
        }
        return this;
    }

    public TrackSelectQueryBuilder findTrackByTrackId(Long trackId) {
        if (trackId != null) {
            this.query.where(qMemberTrack.track.trackId.eq(trackId));
        }
        return this;
    }

    public TrackSelectQueryBuilder findIsTrackPrivacyFalse() {
        this.query.where(qMemberTrack.track.isTrackPrivacy.isFalse());
        return this;
    }


    public TrackSelectQueryBuilder findIsTrackPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {
        this.findIsTrackPrivacyFalse();

        this.query.where(qMemberTrack.track.isTrackPrivacy.isFalse().or(qMemberTrack.member.memberId.eq(loginMemberId)));
        return this;
    }


    public TrackSelectQueryBuilder findCategoryTracks(Long trackCategoryId){
        if (trackCategoryId != null) {
            this.query.where(qMemberTrack.track.trackCategoryId.eq(trackCategoryId));
        }
        return this;

    }

    public TrackSelectQueryBuilder findTrackNotInList(List<Long> excludedTrackIds) {
        if (!excludedTrackIds.isEmpty()) {
            this.query.where(qMemberTrack.track.trackId.notIn(excludedTrackIds));
        }
        return this;
    }

    public TrackSelectQueryBuilder findFollowerTracks(Long loginMemberId) {
        if (loginMemberId != null) {
            FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);
            followSelectQueryBuilder.setQuery(this.query).findFollowerMember(loginMemberId);
        }
        return this;
    }

    public TrackSelectQueryBuilder findTrackBySearchText(String searchText) {
        if (searchText != null) {
            this.query.where(qMemberTrack.track.trackNm.contains(searchText));
        }
        return this;
    }



    /** --------------------------join -------------------------------------------*/

    public TrackSelectQueryBuilder joinMemberTrackWithMember() {
        this.query.join(qMemberTrack.member, qMember);;
        return this; // 체이닝 유지
    }
    public TrackSelectQueryBuilder leftJoinTrackCommentListWithComment() {
        this.query.leftJoin(qMemberTrack.track.commentList, QComment.comment);; // JOIN 조건 추가
        return this; // 체이닝 유지
    }

    public TrackSelectQueryBuilder joinCategoryWithTrackCategoryId() {
        this.query.join(QTrackCategory.trackCategory)
                .on(QTrackCategory.trackCategory.category.trackCategoryId.eq(QMemberTrack.memberTrack.track.trackCategoryId));
        return this; // 체이닝 유지
    }

    public TrackSelectQueryBuilder joinTrackLikeWithMemberTrack(Long loginMemberId) {

        if (loginMemberId != null) {
            this.query.leftJoin(QTrackLike.trackLike)
                    .on(QTrackLike.trackLike.member.memberId.eq(loginMemberId)
                            .and(QTrackLike.trackLike.memberTrack.eq(qMemberTrack)));
        }
        return this;
    }


    public TrackSelectQueryBuilder joinMemberFollowersAndFollow() {
        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);
        followSelectQueryBuilder.setQuery(this.query).joinMemberFollowersAndFollow();
        return this;
    }


    /** --------------------------groupBy ----------------------------------------*/

    public TrackSelectQueryBuilder groupByMemberTrackId() {
        this.query.groupBy(
                QMemberTrack.memberTrack.memberTrackId
        );
        return this;
    }



    /** --------------------------ordeBy ---------------------------------------- */

    public TrackSelectQueryBuilder orderByMemberTrackIdDesc() {
        this.query.orderBy(qMemberTrack.memberTrackId.desc());
        return this;
    }

    public TrackSelectQueryBuilder orderByTrackUploadDateDesc() {
        this.query.orderBy(qMemberTrack.track.trackUploadDate.desc());
        return this;
    }

    public TrackSelectQueryBuilder orderByTrackPlayCntDesc() {
        this.query.orderBy(qMemberTrack.track.trackPlayCnt.desc());
        return this;
    }

    public TrackSelectQueryBuilder orderByTrackLikeCntDesc() {
        this.query.orderBy(qMemberTrack.track.trackLikeCnt.desc());
        return this;
    }


    /** -------------------------fetch ------------------------------------------- */

    public Long fetchTrackId() {
        return this.query.select(qMemberTrack.track.trackId).fetchFirst();
    }

    public <T> List<?> fetTrackSearchDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberTrack.memberTrackId,
                        qMemberTrack.member.memberId,
                        qMemberTrack.member.memberNickName,
                        qMemberTrack.track.trackId,
                        qMemberTrack.track.trackNm,
                        qMemberTrack.track.trackTime,
                        qMemberTrack.track.trackPlayCnt,
                        qMemberTrack.track.trackImagePath,
                        QTrackCategory.trackCategory.category.trackCategoryId,
                        QTrackCategory.trackCategory.category.trackCategoryNm,
                        QTrackLike.trackLike.trackLikeStatus
                )
        ).fetch();
    }

    public <T> T fetchTrackDetailDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberTrack.memberTrack.track.trackId,
                        QMemberTrack.memberTrack.track.trackNm,
                        QMemberTrack.memberTrack.track.isTrackPrivacy,
                        QMemberTrack.memberTrack.track.trackImagePath,
                        QMemberTrack.memberTrack.track.trackPlayCnt,
                        QMemberTrack.memberTrack.track.trackInfo,
                        QMemberTrack.memberTrack.track.trackPath,
                        QMemberTrack.memberTrack.track.trackTime,
                        QMemberTrack.memberTrack.track.trackLikeCnt,
                        QMemberTrack.memberTrack.track.trackCategoryId,
                        QMemberTrack.memberTrack.track.trackUploadDate,
                        QMemberTrack.memberTrack.member.memberId,
                        QMemberTrack.memberTrack.member.memberNickName
                )
        ).fetchFirst();
    }

    public <T> List<?> fetchTrackPreviewDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberTrack.memberTrack.track.trackId,
                        QMemberTrack.memberTrack.track.trackNm,
                        QMemberTrack.memberTrack.track.trackImagePath,
                        QMemberTrack.memberTrack.member.memberNickName
                )
        ).fetch();
    }

    public <T> List<?> fetchTrackListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMemberTrack.memberTrack.track.trackId,
                        QMemberTrack.memberTrack.track.trackNm,
                        QMemberTrack.memberTrack.track.trackTime,
                        QMemberTrack.memberTrack.track.trackLikeCnt,
                        QMemberTrack.memberTrack.track.trackPlayCnt,
                        QMemberTrack.memberTrack.track.trackCategoryId,
                        QMemberTrack.memberTrack.track.trackPath,
                        QMemberTrack.memberTrack.track.trackUploadDate,
                        QMemberTrack.memberTrack.track.trackImagePath,
                        QMemberTrack.memberTrack.member.memberId,
                        QMemberTrack.memberTrack.member.memberNickName
                )
        ).fetch();
    }



    public TrackSelectQueryBuilder isUploadedThisWeekOrLastWeek(StringExpression trackUploadDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 날짜 기준으로 이번 주와 저번 주의 날짜 범위를 계산
        LocalDate today = LocalDate.now();

        // 이번 주 시작일과 종료일
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfThisWeek = startOfThisWeek.plusDays(6);

        // 저번 주 시작일과 종료일
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

        // 날짜를 문자열로 변환
        String startOfThisWeekStr = startOfThisWeek.format(formatter);
        String endOfThisWeekStr = endOfThisWeek.format(formatter);

        String startOfLastWeekStr = startOfLastWeek.format(formatter);
        String endOfLastWeekStr = endOfLastWeek.format(formatter);

        // QueryDSL 조건 작성
        BooleanExpression uploadThisWeek = trackUploadDate.between(startOfThisWeekStr, endOfThisWeekStr);
        BooleanExpression uploadLastWeek = trackUploadDate.between(startOfLastWeekStr, endOfLastWeekStr);

        this.query.where(uploadThisWeek.or(uploadLastWeek)); // WHERE 조건 추가

        return this; // 체이닝을 위해 반환
    }

}
