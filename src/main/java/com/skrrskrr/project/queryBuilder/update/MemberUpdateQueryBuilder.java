package com.skrrskrr.project.queryBuilder.update;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.skrrskrr.project.entity.QMember;

import javax.persistence.EntityManager;

public class MemberUpdateQueryBuilder extends ComnUpdateQueryBuilder<MemberUpdateQueryBuilder>{

    QMember qMember = QMember.member;

    // JPAQueryFactory에서 EntityManager를 추출하여 생성자에 전달
    public MemberUpdateQueryBuilder(EntityManager entityManager) {
        super(entityManager);
        this.entityManager = entityManager;
    }

    // 동적으로 엔티티 설정
    public MemberUpdateQueryBuilder setEntity(EntityPathBase<?> entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        }
        this.updateClause = new JPAUpdateClause(entityManager, entity); // 동적 엔티티 설정
        return this;
    }


    /** -------------------------- WHERE 조건 추가 ------------------------------ */


    public MemberUpdateQueryBuilder findMemberByMemberId(Long loginMemberId) {
        if (loginMemberId == null) {
            throw new IllegalStateException("findMemberByMemberId Error");
        }
        this.updateClause.where(qMember.memberId.eq(loginMemberId));
        return this;
    }


}