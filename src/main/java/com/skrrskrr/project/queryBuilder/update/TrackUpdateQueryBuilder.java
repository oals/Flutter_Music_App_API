package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;
import com.skrrskrr.project.entity.QTrack;
import org.apache.poi.ss.formula.functions.T;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

public class TrackUpdateQueryBuilder {

    private JPAUpdateClause updateClause;
    private final EntityManager entityManager;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public TrackUpdateQueryBuilder(JPAQueryFactory jpaQueryFactory, EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // 동적으로 엔티티 설정
    public TrackUpdateQueryBuilder setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }


    /** -------------------------- WHERE 조건 추가 ------------------------------ */

    public TrackUpdateQueryBuilder findTrackByTrackId(Long trackId) {
        if (this.updateClause == null) {
            throw new IllegalStateException("Entity must be set before adding conditions.");
        }
        this.updateClause.where(QTrack.track.trackId.eq(trackId));
        return this;
    }


    /** -------------------------- SET 조건 추가 ------------------------------ */

    // 업데이트 필드 설정
    public <V> TrackUpdateQueryBuilder set(Path<V> field, V value) {
        if (this.updateClause == null) {
            throw new IllegalStateException("Entity must be set before setting values.");
        }
        this.updateClause.set(field, value);
        return this;
    }


    /** -------------------------- 실행 ----------------------------------------- */
    public long execute() {
        return this.updateClause.execute();
    }
}
