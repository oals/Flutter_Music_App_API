package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class TrackSelectQueryBuilder extends ComnSelectQueryBuilder<TrackSelectQueryBuilder> {

    QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
    QMember qMember = QMember.member;
    QTrackLike qTrackLike = QTrackLike.trackLike;

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
        throwIfConditionNotMet(memberId != null);
        this.query.where(qMemberTrack.member.memberId.eq(memberId));
        return this;
    }

    public TrackSelectQueryBuilder findTracksByNotMemberId(Long memberId) {
        throwIfConditionNotMet(memberId != null);
        this.query.where(qMemberTrack.member.memberId.ne(memberId));
        return this;
    }
    public TrackSelectQueryBuilder findTrackByTrackId(Long trackId) {
        throwIfConditionNotMet(trackId != null);
        this.query.where(qMemberTrack.track.trackId.eq(trackId));
        return this;
    }

    public TrackSelectQueryBuilder findIsTrackPrivacyFalse() {
        this.query.where(qMemberTrack.track.isTrackPrivacy.isFalse());
        return this;
    }

    public TrackSelectQueryBuilder findIsTrackPrivacyFalseOrEqualLoginMemberId(Long loginMemberId) {
        this.query.where(qMemberTrack.track.isTrackPrivacy.isFalse().or(qMemberTrack.member.memberId.eq(loginMemberId)));
        return this;
    }

    public TrackSelectQueryBuilder findCategoryTracks(Long trackCategoryId){
        throwIfConditionNotMet(trackCategoryId != null);
        this.query.where(qMemberTrack.track.trackCategoryId.eq(trackCategoryId));

        return this;
    }

    public TrackSelectQueryBuilder findTrackNotInList(List<Long> excludedTrackIds) {
        if (excludedTrackIds != null) {
            this.query.where(qMemberTrack.track.trackId.notIn(excludedTrackIds));
        }
        return this;
    }

    public TrackSelectQueryBuilder findTrackInList(List<Long> excludedTrackIds) {
        if (!excludedTrackIds.isEmpty()) {
            this.query.where(qMemberTrack.track.trackId.in(excludedTrackIds));
        }
        return this;
    }

    public TrackSelectQueryBuilder findMemberTrackByPlayListId(Long playListId) {
        throwIfConditionNotMet(playListId != null);

        this.query.where(qMemberTrack.playlistTrack.any().playListId.eq(playListId));

        return this;
    }

    public TrackSelectQueryBuilder findFollowerTracks(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);
        followSelectQueryBuilder.setQuery(this.query).findFollowerMember(loginMemberId);

        return this;
    }

    public TrackSelectQueryBuilder findTrackBySearchText(String searchText) {
        if (searchText != null) {
            this.query.where(qMemberTrack.track.trackNm.contains(searchText));
        }
        return this;
    }

    public TrackSelectQueryBuilder findTracksBySearchTextList(List<String> searchTextList) {
        if (searchTextList != null) {
            BooleanExpression containsCondition = searchTextList.stream()
                    .map(searchText -> qMemberTrack.track.trackNm.contains(searchText))
                    .reduce(BooleanExpression::or)
                    .orElse(null);

            if (containsCondition != null) {
                this.query.where(containsCondition);
            }
        }
        return this;
    }

    /** --------------------------join -------------------------------------------*/

    public TrackSelectQueryBuilder joinMemberTrackWithMember() {
        this.query.join(qMemberTrack.member, qMember);;
        return this; // 체이닝 유지
    }
    public TrackSelectQueryBuilder joinTrackCommentListWithComment() {
        this.query.join(qMemberTrack.track.commentList, QComment.comment);; // JOIN 조건 추가
        return this; // 체이닝 유지
    }

    public TrackSelectQueryBuilder joinTrackLikeWithMemberTrack(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.leftJoin(QTrackLike.trackLike)
                .on(QTrackLike.trackLike.member.memberId.eq(loginMemberId)
                        .and(QTrackLike.trackLike.memberTrack.eq(qMemberTrack)));
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
                qMemberTrack.memberTrackId
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

    public <T> T fetchTrackDetailDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
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
                        qMemberTrack.member.memberNickName,
                        qMemberTrack.member.memberImagePath,
                        ExpressionUtils.as(
                                new CaseBuilder()
                                        .when(qTrackLike.trackLikeStatus.isNull()).then(false)
                                        .otherwise(qTrackLike.trackLikeStatus), "trackLikeStatus"
                        )
                )
        ).fetchFirst();
    }

    public <T> List<?> fetchTrackListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberTrack.track.trackId,
                        qMemberTrack.track.trackNm,
                        qMemberTrack.track.trackTime,
                        qMemberTrack.track.trackLikeCnt,
                        qMemberTrack.track.isTrackPrivacy,
                        qMemberTrack.track.trackPlayCnt,
                        qMemberTrack.track.trackCategoryId,
                        qMemberTrack.track.trackPath,
                        qMemberTrack.track.trackUploadDate,
                        qMemberTrack.track.trackImagePath,
                        qMemberTrack.member.memberId,
                        qMemberTrack.member.memberNickName,
                        qMemberTrack.member.memberImagePath,
                        ExpressionUtils.as(
                                new CaseBuilder()
                                        .when(qTrackLike.trackLikeStatus.isNull()).then(false)
                                        .otherwise(qTrackLike.trackLikeStatus), "trackLikeStatus"
                        )

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
