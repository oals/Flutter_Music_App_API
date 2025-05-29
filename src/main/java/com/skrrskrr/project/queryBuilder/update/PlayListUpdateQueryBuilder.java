package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.skrrskrr.project.entity.QPlayList;
import jakarta.persistence.EntityManager;

public class PlayListUpdateQueryBuilder extends ComnUpdateQueryBuilder<PlayListUpdateQueryBuilder>{

    QPlayList qPlayList = QPlayList.playList;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public PlayListUpdateQueryBuilder(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    // 동적으로 엔티티 설정
    public PlayListUpdateQueryBuilder setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }

    /** -------------------------- WHERE 조건 추가 ------------------------------ */

    public PlayListUpdateQueryBuilder findPlayListByPlayListId(Long playListId) {
        if (playListId == null) {
            throw new IllegalStateException("findPlayListByPlayListId Error");
        }
        this.updateClause.where(qPlayList.playListId.eq(playListId));
        return this;
    }
}