package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.persistence.EntityManager;

public class ComnUpdateQueryBuilder <T extends ComnUpdateQueryBuilder<T>> {

    protected EntityManager entityManager;
    protected JPAUpdateClause updateClause;

    public ComnUpdateQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ComnUpdateQueryBuilder<T> setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }


    // 업데이트 필드 설정
    public <V> T set(Path<V> field, V value) {
        if (this.updateClause == null) {
            throw new IllegalStateException("Entity must be set before setting values.");
        }
        if (value != null) {
            this.updateClause.set(field, value);
        }
        return (T) this;
    }

    // 공통 실행 메서드
    public Long execute() {
        return this.updateClause.execute();
    }
}