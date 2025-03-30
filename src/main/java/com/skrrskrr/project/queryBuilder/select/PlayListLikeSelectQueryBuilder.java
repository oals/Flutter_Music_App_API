package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
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


    /** -------------------------join -------------------------------------------*/



    /** --------------------------ordeBy ---------------------------------------- */



    /**-------------------------fetch -------------------------------------------*/


}