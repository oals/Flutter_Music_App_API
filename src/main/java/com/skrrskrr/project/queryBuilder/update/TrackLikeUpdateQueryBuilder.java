package com.skrrskrr.project.queryBuilder.update;


import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.skrrskrr.project.entity.QTrackLike;

import javax.persistence.EntityManager;

public class TrackLikeUpdateQueryBuilder extends ComnUpdateQueryBuilder<TrackLikeUpdateQueryBuilder>{

    QTrackLike qTrackLike = QTrackLike.trackLike;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public TrackLikeUpdateQueryBuilder(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    // 동적으로 엔티티 설정
    public TrackLikeUpdateQueryBuilder setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }


    /** -------------------------- WHERE 조건 추가 ------------------------------ */


    public TrackLikeUpdateQueryBuilder findMemberTrackByMemberTrackId(Long memberTrackId) {
        if (memberTrackId == null) {
            throw new IllegalStateException("findMemberTrackByMemberTrackId Error");
        }
        this.updateClause.where(qTrackLike.memberTrack.memberTrackId.eq(memberTrackId));
        return this;
    }

    public TrackLikeUpdateQueryBuilder findTrackLikeByMemberMemberId(Long loginMemberId) {
        if (loginMemberId == null) {
            throw new IllegalStateException("findTrackLikeByMemberMemberId Error");
        }
        this.updateClause.where(qTrackLike.member.memberId.eq(loginMemberId));
        return this;
    }

}
