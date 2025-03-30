package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.persistence.EntityManager;

public abstract class ComnUpdateQueryBuilder<T> {

    protected final EntityManager entityManager;
    protected final JPAUpdateClause updateClause;

    public ComnUpdateQueryBuilder(EntityManager entityManager, T entity) {
        this.entityManager = entityManager;
        this.updateClause = new JPAUpdateClause(entityManager, (EntityPath<?>) entity);
    }

    // 공통 실행 메서드
    public Long execute() {
        return this.updateClause.execute();
    }

    // 조건 추가 메서드는 서브 클래스에서 구현하도록 추상화
    public abstract ComnUpdateQueryBuilder<T> where(com.querydsl.core.types.dsl.BooleanExpression condition);

    // 업데이트 값 설정 메서드는 서브 클래스에서 구현하도록 추상화
    public abstract <V> ComnUpdateQueryBuilder<T> set(com.querydsl.core.types.Path<V> field, V value);
}