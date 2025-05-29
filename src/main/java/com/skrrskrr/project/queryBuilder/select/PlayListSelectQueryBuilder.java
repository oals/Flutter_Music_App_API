package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;
import java.util.List;

public class PlayListSelectQueryBuilder extends ComnSelectQueryBuilder<PlayListSelectQueryBuilder> {

    QMemberPlayList qMemberPlayList = QMemberPlayList.memberPlayList;
    QPlayListLike qPlayListLike = QPlayListLike.playListLike;

    public PlayListSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);  // 상위 클래스의 생성자 호출
    }

    public PlayListSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */

    public PlayListSelectQueryBuilder findPlayListsByMemberId (Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.query.where(qMemberPlayList.playList.member.memberId.eq(loginMemberId));
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListsByNotMemberId (Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.query.where(qMemberPlayList.playList.member.memberId.ne(loginMemberId));
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListsByPlayListId(Long playListId) {
        throwIfConditionNotMet(playListId != null);
        this.query.where(qMemberPlayList.playList.playListId.eq(playListId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsAlbum (Boolean isAlbum) {
        throwIfConditionNotMet(isAlbum != null);
        this.query.where(qMemberPlayList.playList.isAlbum.eq(isAlbum));
        return this;
    }

    public PlayListSelectQueryBuilder findIsInPlayListTrack (Long trackId) {
        throwIfConditionNotMet(trackId != null);
        this.query.where(qMemberPlayList.playList.playListTrackList.any().track.trackId.eq(trackId));
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListNotEmpty () {
        this.query.where(qMemberPlayList.playList.playListTrackList.isNotEmpty());
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListNotEmptyOrEqualLoginMemberId (Long loginMemberId) {
        this.query.where(qMemberPlayList.playList.playListTrackList.isNotEmpty()
                .or(qMemberPlayList.member.memberId.eq(loginMemberId))
        );
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListBySearchText (String searchText) {

        if (searchText != null) {
            this.query.where(qMemberPlayList.playList.playListNm.contains(searchText));
        }
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListBySearchTextList(List<String> searchTextList) {
        if (searchTextList != null) {
            BooleanExpression containsCondition = searchTextList.stream()
                    .map(searchText -> qMemberPlayList.playList.playListNm.contains(searchText))
                    .reduce(BooleanExpression::or)
                    .orElse(null);

            if (containsCondition != null) {
                this.query.where(containsCondition);
            }
        }
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListByNotPlayListIdList(List<Long> playListId) {
        if (playListId != null) {
            BooleanExpression containsCondition = playListId.stream()
                    .map(id -> qMemberPlayList.playList.playListId.ne(id))
                    .reduce(BooleanExpression::and)
                    .orElse(null);

            if (containsCondition != null) {
                this.query.where(containsCondition);
            }
        }
        return this;
    }

    public PlayListSelectQueryBuilder findPlayListByMemberIdList(List<Long> memberId) {
        if (memberId != null) {
            BooleanExpression containsCondition = memberId.stream()
                    .map(id -> qMemberPlayList.playList.member.memberId.eq(id))
                    .reduce(BooleanExpression::or)
                    .orElse(null);

            if (containsCondition != null) {
                this.query.where(containsCondition);
            }
        }
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalse () {
        this.query.where(qMemberPlayList.playList.isPlayListPrivacy.isFalse());
        return this;
    }

    public PlayListSelectQueryBuilder findIsPlayListPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.query.where(qMemberPlayList.playList.isPlayListPrivacy.isFalse()
                .or(qMemberPlayList.playList.member.memberId.eq(loginMemberId)));
        return this;
    }

    /** --------------------------join -------------------------------------------*/

    public PlayListSelectQueryBuilder joinMemberPlayListWithMember() {
        this.query.join(qMemberPlayList.member, QMember.member);;
        return this; // 체이닝 유지
    }


    public PlayListSelectQueryBuilder findFollowerPlayLists(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);
        followSelectQueryBuilder.setQuery(this.query).findFollowerMember(loginMemberId);

        return this;
    }

    public PlayListSelectQueryBuilder joinMemberFollowersAndFollow() {
        FollowSelectQueryBuilder followSelectQueryBuilder = new FollowSelectQueryBuilder(jpaQueryFactory);
        followSelectQueryBuilder.setQuery(this.query).joinMemberFollowersAndFollow();
        return this;
    }

    public PlayListSelectQueryBuilder joinPlayListLikeWithMemberPlayList(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.leftJoin(qPlayListLike)
                .on(qPlayListLike.member.memberId.eq(loginMemberId)
                        .and(qPlayListLike.memberPlayList.eq(qMemberPlayList)));

        return this;
    }

    /** --------------------------ordeBy ---------------------------------------- */

    public PlayListSelectQueryBuilder orderByPlayListIdDesc() {
        this.query.orderBy(qMemberPlayList.playList.playListId.desc());
        return this;
    }

    public PlayListSelectQueryBuilder orderByPlayListLikeCntDesc() {
        this.query.orderBy(qMemberPlayList.playList.playListLikeCnt.desc());
        return this;
    }

    /** -------------------------fetch ------------------------------------------- */

    public <T> T fetchPlayListDetailDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList.playListId,
                        qMemberPlayList.playList.playListNm,
                        qMemberPlayList.playList.playListLikeCnt,
                        qMemberPlayList.playList.isPlayListPrivacy,
                        qMemberPlayList.playList.playListImagePath,
                        qMemberPlayList.playList.totalPlayTime,
                        qMemberPlayList.playList.isAlbum,
                        qMemberPlayList.playList.trackCnt,
                        qMemberPlayList.playList.albumDate,
                        qMemberPlayList.playList.member.memberId,
                        qMemberPlayList.playList.member.memberNickName,
                        qMemberPlayList.playList.member.memberImagePath,
                        ExpressionUtils.as(
                                new CaseBuilder()
                                        .when(qPlayListLike.playListLikeStatus.isNull()).then(false)
                                        .otherwise(qPlayListLike.playListLikeStatus), "isPlayListLike"
                        )
                )
        ).fetchFirst();
    }

    public <T> List<?> fetchPlayListPreviewDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList.playListId,
                        qMemberPlayList.playList.playListNm,
                        qMemberPlayList.playList.playListImagePath,
                        qMemberPlayList.member.memberId
                )
        ).fetch();
    }

    public <T> List<?> fetchPlayListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qMemberPlayList.playList.playListId,
                        qMemberPlayList.playList.playListNm,
                        qMemberPlayList.playList.playListLikeCnt,
                        qMemberPlayList.playList.isPlayListPrivacy,
                        qMemberPlayList.playList.playListImagePath,
                        qMemberPlayList.playList.totalPlayTime,
                        qMemberPlayList.playList.trackCnt,
                        qMemberPlayList.playList.isAlbum,
                        qMemberPlayList.playList.trackCnt,
                        qMemberPlayList.playList.albumDate,
                        qMemberPlayList.playList.member.memberId,
                        qMemberPlayList.playList.member.memberNickName,
                        qMemberPlayList.playList.member.memberImagePath,
                        ExpressionUtils.as(
                                new CaseBuilder()
                                        .when(qPlayListLike.playListLikeStatus.isNull()).then(false)
                                        .otherwise(qPlayListLike.playListLikeStatus), "isPlayListLike"
                        )
                )
        ).fetch();
    }
}
