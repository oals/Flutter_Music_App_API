package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.util.List;

public class HistorySelectQueryBuilder extends ComnSelectQueryBuilder<HistorySelectQueryBuilder> {

    QComment qComment = QComment.comment;

    public HistorySelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public HistorySelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }


    /** --------------------------where ---------------------------------------- */


    public HistorySelectQueryBuilder findHistoryByMemberId(Long loginMemberId) {
        if (loginMemberId != null) {
            this.query.where(QHistory.history.member.memberId.eq(loginMemberId));
        }
        return this;
    }


    /** -------------------------join -------------------------------------------*/


//    public HistorySelectQueryBuilder joinMemberFollowersAndFollow() {
//        this.query.join(QMember.member.followers, QFollow.follow); // JOIN 조건 추가
//        return this;
//    }



    /** --------------------------ordeBy ---------------------------------------- */


    public HistorySelectQueryBuilder orderByHistoryIdDesc() {
        this.query.orderBy(QHistory.history.historyId.desc()); // JOIN 조건 추가
        return this;
    }

    public HistorySelectQueryBuilder orderByHistoryIdAsc() {
        this.query.orderBy(QHistory.history.historyId.asc()); // JOIN 조건 추가
        return this;
    }


    /**-------------------------fetch -------------------------------------------*/

    public List<Long> fetchHistoryIdList() {
        return this.query.select(
                QHistory.history.historyId
        ).fetch();
    }

    public <T> List<T> fetchHistoryListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QHistory.history.historyId.as("historyId"),
                        QHistory.history.historyText.as("historyText"),
                        QHistory.history.historyDate.as("historyDate")
                )
        ).fetch();
    }



}