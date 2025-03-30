package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAUpdateClause;

import java.util.List;

@SuppressWarnings("unchecked")
public class ComnSelectQueryBuilder<T extends ComnSelectQueryBuilder<T>> {

    protected final JPAQueryFactory jpaQueryFactory;
    protected  JPAQuery<?> query;

    // 생성자: JPAQueryFactory 주입
    public ComnSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.query = jpaQueryFactory.query();  // 초기 쿼리 객체 설정
    }

    // select() 메서드: select할 필드를 설정
    public T select(Expression<?>... fields) {
        this.query = jpaQueryFactory.select(fields);
        return (T) this;
    }

    public T selectFrom(EntityPathBase<?> entityPathBase) {
        this.query = jpaQueryFactory.selectFrom(entityPathBase);
        return (T) this;
    }

    // from() 메서드: 엔티티를 설정
    public T from(EntityPathBase<?> entityPathBase) {
        this.query.from(entityPathBase);
        return (T) this;
    }

    // where() 메서드: 조건을 추가
    public T where(Predicate... predicates) {
        this.query.where(predicates);
        return (T) this;
    }

    // limit() 메서드: 제한값을 설정
    public T limit(long limit) {
        this.query.limit(limit);
        return (T) this;
    }

    // offset() 메서드: 오프셋을 설정
    public T offset(long offset) {
        this.query.offset(offset);
        return (T) this;
    }

    // orderBy() 메서드: 정렬 조건을 추가
    public T orderBy(OrderSpecifier<?>... orderSpecifiers) {
        this.query.orderBy(orderSpecifiers);
        return (T) this;
    }

    // 결과 반환 (명확한 타입을 받도록 수정)
    public <T> List<T> fetch(Class<T> clazz) {
        return (List<T>) this.query.fetch();  // fetch()에서 반환할 타입을 명시적으로 설정
    }

    public <T> Object fetchOne(Class<T> clazz) {
        return (T) this.query.fetchOne();  // fetch()에서 반환할 타입을 명시적으로 설정
    }

    // 첫 번째 결과만 반환
    public <T> Object fetchFirst(Class<T> clazz) {
        return this.query.fetchFirst();
    }

    public ComnSelectQueryBuilder<T> resetQuery() {
        this.query = this.jpaQueryFactory.query(); // 새로운 쿼리 객체 생성
        return this;
    }

    // 쿼리 객체 반환 (이후 다른 작업을 할 때 활용 가능)
    public JPAQuery<T> getQuery() {
        return (JPAQuery<T>) this.query;
    }

    public Long fetchCount() {
        return this.query.select(Wildcard.count) // COUNT(*) 설정
                .fetchOne();            // 쿼리 실행 및 결과 반환
    }

}
