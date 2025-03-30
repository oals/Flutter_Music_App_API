package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QMember;

public class MemberSelectQueryBuilder extends ComnSelectQueryBuilder<MemberSelectQueryBuilder> {


    QMember qMember = QMember.member;

    public MemberSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public MemberSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }


    /** --------------------------where ---------------------------------------- */


    public MemberSelectQueryBuilder findMemberByMemberEmail(String memberEmail) {
        if (memberEmail != null) {
            this.query.where(qMember.memberEmail.eq(memberEmail));
        }
        return this;
    }

    public MemberSelectQueryBuilder findMemberByMemberId(Long memberId) {
        if (memberId != null) {
            this.query.where(qMember.memberId.eq(memberId));
        }
        return this;
    }

    public MemberSelectQueryBuilder findMemberByMemberIdNotEqual(Long loginMemberId) {
        if (loginMemberId != null) {
            this.query.where(qMember.memberId.ne(loginMemberId));
        }
        return this;
    }

    public MemberSelectQueryBuilder findMemberBySearchText(String searchText) {
        if (searchText != null) {
            this.query.where(qMember.memberNickName.lower().contains(searchText.toLowerCase()));
        }
        return this;
    }




    public MemberSelectQueryBuilder findIsNotEmptyMemberTrackList() {
        this.query.where(qMember.memberTrackList.isNotEmpty());
        return this;
    }

    public MemberSelectQueryBuilder groupByMemberId() {
        this.query.groupBy(qMember.memberId); // 멤버 아이디별로 그룹화
        return this;
    }


    /** -------------------------join -------------------------------------------*/




    /** --------------------------ordeBy ---------------------------------------- */


    // 랜덤으로 정렬하는 메서드
    public MemberSelectQueryBuilder orderByRandom() {
        this.query.orderBy(Expressions.numberTemplate(Double.class, "function('RAND')").asc()); // 랜덤 정렬
        return this;
    }


    /** --------------------------fetch ------------------------------------------*/


    public <T> T fetchPreviewMemberDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        QMember.member.memberId,
                        QMember.member.memberNickName,
                        QMember.member.memberEmail,
                        QMember.member.memberImagePath,
                        QMember.member.memberDeviceToken
                )
        ).fetchOne();
    }



}
