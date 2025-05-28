package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.NotificationResponseDto;
import com.skrrskrr.project.dto.NotificationsDto;
import com.skrrskrr.project.dto.NotificationsRequestDto;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.entity.QNotifications;
import com.skrrskrr.project.queryBuilder.select.NotificationSelectQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;




import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class NotificationsServiceImpl implements NotificationsService{

    @PersistenceContext
    EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public NotificationResponseDto getNotifications(NotificationsRequestDto notificationsRequestDto) {


        Long totalCount = fetchNotificationsCount(notificationsRequestDto);
        NotificationResponseDto notificationResponseDto = new NotificationResponseDto();

        if (totalCount != 0L) {
            List<NotificationsDto> notifications = fetchNotifications(notificationsRequestDto);
            notificationResponseDto.setNotificationList(notifications);
        }

        notificationResponseDto.setTotalCount(totalCount);

        return notificationResponseDto;
    }

    private List<NotificationsDto> fetchNotifications(NotificationsRequestDto notificationsRequestDto) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        return notificationSelectQueryBuilder
                .selectFrom(QNotifications.notifications)
                .joinNotificationWithMember()
                .findNotificationByMemberId(notificationsRequestDto.getLoginMemberId())
                .limit(notificationsRequestDto.getLimit())
                .orderByNotificationIdDesc()
                .offset(notificationsRequestDto.getOffset())
                .fetchNotificationDetailDto(NotificationsDto.class);
    }

    private Long fetchNotificationsCount(NotificationsRequestDto notificationsRequestDto) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        return notificationSelectQueryBuilder
                .selectFrom(QNotifications.notifications)
                .findNotificationByMemberId(notificationsRequestDto.getLoginMemberId())
                .fetchCount();
    }


    @Override
    public NotificationResponseDto setNotificationIsView(NotificationsRequestDto notificationsRequestDto) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        Notifications notifications = (Notifications) notificationSelectQueryBuilder
                .selectFrom(QNotifications.notifications)
                .findNotificationByNotificationId(notificationsRequestDto.getNotificationId())
                .fetchFirst(Notifications.class);

        if (notifications == null) {
            throw new IllegalStateException("notifications cannot be null.");
        }

        updateIsNotificationViewStatus(notifications);

        Boolean notificationIsView = selectNotificationIsNotView(notificationsRequestDto.getLoginMemberId());

        return NotificationResponseDto.builder()
                .notificationIsView(notificationIsView)
                .build();
    }

    @Override
    public Boolean selectNotificationIsNotView(Long loginMemberId) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        return Boolean.FALSE.equals(
                notificationSelectQueryBuilder
                        .selectFrom(QNotifications.notifications)
                        .findNotificationByMemberId(loginMemberId)
                        .findIsNotificationViewFalse()
                        .fetchNotificationListViewStatus()
        );
    }

    @Override
    public void setAllNotificationIsView(NotificationsRequestDto notificationsRequestDto) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        List<Notifications> notificationsList = notificationSelectQueryBuilder
                .selectFrom(QNotifications.notifications)
                .findNotificationByMemberId(notificationsRequestDto.getLoginMemberId())
                .findIsNotificationViewFalse()
                .fetch(Notifications.class);

        for (Notifications notifications : notificationsList) {
            updateIsNotificationViewStatus(notifications);
        }
    }

    private void updateIsNotificationViewStatus(Notifications notifications){
        notifications.setNotificationIsView(true);
        entityManager.merge(notifications);
    }

    @Override
    public void setDelNotificationIsView(NotificationsRequestDto notificationsRequestDto) {
        
        QNotifications qNotifications = QNotifications.notifications;

        jpaQueryFactory.delete(qNotifications)
                .where(qNotifications.member.memberId.eq(notificationsRequestDto.getLoginMemberId()))
                .execute();
    }
}
