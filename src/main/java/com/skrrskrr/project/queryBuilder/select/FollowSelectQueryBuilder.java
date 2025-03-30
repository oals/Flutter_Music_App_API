package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;

public class FollowSelectQueryBuilder extends ComnSelectQueryBuilder<FollowSelectQueryBuilder> {

    QFollow qFollow = QFollow.follow;
    QMember qMember = QMember.member;

    public FollowSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public FollowSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }


    /** --------------------------where ---------------------------------------- */


    public FollowSelectQueryBuilder findFollowerMember(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qFollow.following.memberId.eq(loginMemberId));
        return this;
    }

    public FollowSelectQueryBuilder findFollowingMember(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qFollow.follower.memberId.eq(loginMemberId));
        return this;
    }

    /** -------------------------join -------------------------------------------*/


    public FollowSelectQueryBuilder joinMemberFollowersAndFollow() {
        this.query.join(qMember.followers, QFollow.follow); // JOIN 조건 추가
        return this;
    }



    /** --------------------------ordeBy ---------------------------------------- */



}

