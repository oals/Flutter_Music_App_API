package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.skrrskrr.project.entity.QPlayListLike;
import jakarta.persistence.EntityManager;


public class PlayListLikeUpdateQueryBuilder extends ComnUpdateQueryBuilder<PlayListLikeUpdateQueryBuilder>{

    QPlayListLike qPlayListLike = QPlayListLike.playListLike;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public PlayListLikeUpdateQueryBuilder(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    // 동적으로 엔티티 설정
    public PlayListLikeUpdateQueryBuilder setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }

    /** -------------------------- WHERE 조건 추가 ------------------------------ */

    public PlayListLikeUpdateQueryBuilder findPlayListLikeByMemberPlayListId(Long memberPlayListId) {
        if (memberPlayListId == null) {
            throw new IllegalStateException("findPlayListLikeByMemberPlayListId Error");
        }
        this.updateClause.where(qPlayListLike.memberPlayList.memberPlayListId.eq(memberPlayListId));
        return this;
    }

    public PlayListLikeUpdateQueryBuilder findPlayListLikeByMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException("findPlayListLikeByMemberId Error");
        }
        this.updateClause.where(qPlayListLike.member.memberId.eq(memberId));
        return this;
    }
}