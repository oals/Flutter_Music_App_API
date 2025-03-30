package com.skrrskrr.project.queryBuilder.select;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.entity.*;

import java.util.List;

public class NotificationSelectQueryBuilder extends ComnSelectQueryBuilder<NotificationSelectQueryBuilder> {


    QNotifications qNotifications = QNotifications.notifications;

    public NotificationSelectQueryBuilder(JPAQueryFactory jpaQueryFactory) {
        super(jpaQueryFactory);
        this.query = jpaQueryFactory.query(); // 외부에서 전달받은 JPAQuery를 사용
    }

    public NotificationSelectQueryBuilder setQuery(JPAQuery<?> query) {
        this.query = query != null ? query : this.query; // 외부 값이 없으면 기본값 유지
        return this;
    }


    /** --------------------------where ---------------------------------------- */


    public NotificationSelectQueryBuilder findNotificationByMemberId(Long loginMemberId) {
        throwIfConditionNotMet(loginMemberId != null);

        this.query.where(qNotifications.member.memberId.eq(loginMemberId));
        return this;
    }

    public NotificationSelectQueryBuilder findNotificationByNotificationId(Long notificationId) {
        throwIfConditionNotMet(notificationId != null);

        this.query.where(qNotifications.notificationId.eq(notificationId));
        return this;
    }

    public NotificationSelectQueryBuilder findIsNotificationViewFalse() {
        this.query.where(qNotifications.notificationIsView.isFalse());
        return this;
    }

    /** -------------------------join -------------------------------------------*/




    /** --------------------------ordeBy ---------------------------------------- */


    public NotificationSelectQueryBuilder orderByNotificationIdDesc() {
        this.query.orderBy(qNotifications.notificationId.desc());
        return this;
    }


    /** --------------------------fetch ------------------------------------------*/


    public <T> List<T> fetchNotificationDetailDto(Class<T> clazz) {
        return this.query.select(
                Projections.bean(
                        clazz,
                        qNotifications.notificationId,
                        qNotifications.notificationMsg,
                        qNotifications.notificationType,
                        qNotifications.notificationDate,
                        qNotifications.member.memberId,
                        qNotifications.notificationTrackId,
                        qNotifications.notificationCommentId,
                        qNotifications.notificationMemberId,
                        qNotifications.notificationIsView
                )
        ).fetch();
    }


    public Boolean fetchNotificationListViewStatus() {
        Boolean notificationIsView = this.query.select(QNotifications.notifications.notificationIsView)
                .fetchFirst();

        return notificationIsView != null && notificationIsView;
    }

}
