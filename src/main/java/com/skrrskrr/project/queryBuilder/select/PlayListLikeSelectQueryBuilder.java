package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QComment;
import com.skrrskrr.project.entity.QHistory;
import com.skrrskrr.project.entity.QPlayListLike;

import java.util.List;

public class PlayListLikeSelectQueryBuilder  extends ComnSelectQueryBuilder<PlayListLikeSelectQueryBuilder> {

    QPlayListLike qPlayListLike = QPlayListLike.playListLike;

    public PlayListLikeSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public PlayListLikeSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }

    /** --------------------------where ---------------------------------------- */

    public PlayListLikeSelectQueryBuilder findPlayListByPlayListId(Long playListId) {
        throwIfConditionNotMet(playListId != null);

        this.query.where(qPlayListLike.memberPlayList.playList.playListId.eq(playListId));
        return this;
    }

    public PlayListLikeSelectQueryBuilder findPlayListLikeByMemberId(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qPlayListLike.member.memberId.eq(loginMemberId));
        return this;
    }

    public PlayListLikeSelectQueryBuilder findIsAlbum (Boolean isAlbum) {
        throwIfConditionNotMet(isAlbum != null);
        this.query.where(qPlayListLike.memberPlayList.playList.isAlbum.eq(isAlbum));
        return this;
    }

    public PlayListLikeSelectQueryBuilder findPlayListsByNotMemberId(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);
        this.query.where(qPlayListLike.memberPlayList.member.memberId.ne(loginMemberId));
        return this;
    }

    public PlayListLikeSelectQueryBuilder findIsPlayListPrivacyFalse() {
        this.query.where(qPlayListLike.memberPlayList.playList.isPlayListPrivacy.isFalse());
        return this;
    }

    public PlayListLikeSelectQueryBuilder findIsPlayListPrivacyFalseOrLoginMemberIdEqual(Long loginMemberId) {

        this.query.where(
                qPlayListLike.memberPlayList.playList.isPlayListPrivacy.isFalse()
                        .or(qPlayListLike.memberPlayList.member.memberId.eq(loginMemberId))
        );
        return this;
    }

    public PlayListLikeSelectQueryBuilder findIsPlayListLikeStatusTrue() {
        this.query.where(qPlayListLike.playListLikeStatus.isTrue());
        return this;
    }

    /** -------------------------join -------------------------------------------*/


    /** --------------------------ordeBy ---------------------------------------- */

    public PlayListLikeSelectQueryBuilder orderByPlayListLikeDateDesc() {
        this.query.orderBy(qPlayListLike.playListLikeDate.desc());
        return this;
    }

    /**-------------------------fetch -------------------------------------------*/

    public List<Long> fetchPlayListByMemberIdList() {
        return this.query.select(
                qPlayListLike.memberPlayList.member.memberId
        ).fetch();
    }

    public <T> List<?> fetchLikePlayListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qPlayListLike.memberPlayList.playList.playListId,
                        qPlayListLike.memberPlayList.playList.playListNm,
                        qPlayListLike.memberPlayList.playList.playListLikeCnt,
                        qPlayListLike.memberPlayList.playList.isPlayListPrivacy,
                        qPlayListLike.memberPlayList.playList.playListImagePath,
                        qPlayListLike.memberPlayList.playList.totalPlayTime,
                        qPlayListLike.memberPlayList.playList.trackCnt,
                        qPlayListLike.memberPlayList.playList.isAlbum,
                        qPlayListLike.memberPlayList.playList.trackCnt,
                        qPlayListLike.memberPlayList.playList.albumDate,
                        qPlayListLike.memberPlayList.member.memberId,
                        qPlayListLike.memberPlayList.member.memberNickName,
                        qPlayListLike.memberPlayList.member.memberImagePath,
                        ExpressionUtils.as(
                                new CaseBuilder()
                                        .when(qPlayListLike.playListLikeStatus.isNull()).then(false)
                                        .otherwise(qPlayListLike.playListLikeStatus), "isPlayListLike"
                        )
                )
        ).fetch();
    }
}