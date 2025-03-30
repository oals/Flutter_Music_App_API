package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QComment;
import com.skrrskrr.project.entity.QFollow;
import com.skrrskrr.project.entity.QMember;

public class CommentSelectQueryBuilder extends ComnSelectQueryBuilder<CommentSelectQueryBuilder> {

    QComment qComment = QComment.comment;

    public CommentSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public CommentSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }


    /** --------------------------where ---------------------------------------- */


    public CommentSelectQueryBuilder findChildCommentByParentCommentId(Long commentId) {
        if (commentId != null) {
            this.query.where(qComment.commentId.eq(commentId));
        }
        return this;
    }

    public CommentSelectQueryBuilder findCommentByTrackId(Long trackId){
        if (trackId != null ){
            this.query .where(qComment.track.trackId.eq(trackId));
        }
        return this;
    }



    /** -------------------------join -------------------------------------------*/


    public CommentSelectQueryBuilder joinMemberFollowersAndFollow() {
        this.query.join(QMember.member.followers, QFollow.follow); // JOIN 조건 추가
        return this;
    }



    /** --------------------------ordeBy ---------------------------------------- */



}
