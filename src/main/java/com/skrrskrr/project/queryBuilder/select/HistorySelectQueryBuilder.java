package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.util.List;

public class HistorySelectQueryBuilder extends ComnSelectQueryBuilder<HistorySelectQueryBuilder> {

    QHistory qHistory = QHistory.history;

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
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qHistory.member.memberId.eq(loginMemberId));
        return this;
    }


    /** -------------------------join -------------------------------------------*/




    /** --------------------------ordeBy ---------------------------------------- */


    public HistorySelectQueryBuilder orderByHistoryIdDesc() {
        this.query.orderBy(qHistory.historyId.desc()); // JOIN 조건 추가
        return this;
    }

    public HistorySelectQueryBuilder orderByHistoryIdAsc() {
        this.query.orderBy(qHistory.historyId.asc()); // JOIN 조건 추가
        return this;
    }


    /**-------------------------fetch -------------------------------------------*/

    public List<Long> fetchHistoryIdList() {
        return this.query.select(
                qHistory.historyId
        ).fetch();
    }

    public <T> List<T> fetchHistoryListDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qHistory.historyId.as("historyId"),
                        qHistory.historyText.as("historyText"),
                        qHistory.historyDate.as("historyDate")
                )
        ).fetch();
    }



}