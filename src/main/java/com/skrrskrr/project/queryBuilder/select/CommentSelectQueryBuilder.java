package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.QComment;


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


    public CommentSelectQueryBuilder findCommentByCommentId(Long commentId) {
        throwIfConditionNotMet(commentId != null);

        this.query.where(qComment.commentId.eq(commentId));
        return this;
    }

    public CommentSelectQueryBuilder findCommentByTrackId(Long trackId){
        throwIfConditionNotMet(trackId != null);

        this.query .where(qComment.track.trackId.eq(trackId));
        return this;
    }



    /** -------------------------join -------------------------------------------*/




    /** --------------------------ordeBy ---------------------------------------- */



}
