package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.NotificationResponseDto;
import com.skrrskrr.project.dto.NotificationsDto;
import com.skrrskrr.project.dto.NotificationsRequestDto;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.entity.QNotifications;
import com.skrrskrr.project.queryBuilder.select.NotificationSelectQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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

        List<NotificationsDto> queryResult = fetchNotifications(notificationsRequestDto);

        // 알림 목록 날짜별 분류
        return classifyNotificationsByDate(queryResult);

    }

    private List<NotificationsDto> fetchNotifications(NotificationsRequestDto notificationsRequestDto) {

        NotificationSelectQueryBuilder notificationSelectQueryBuilder = new NotificationSelectQueryBuilder(jpaQueryFactory);

        return notificationSelectQueryBuilder
                .selectFrom(QNotifications.notifications)
                .findNotificationByMemberId(notificationsRequestDto.getLoginMemberId())
                .limit(notificationsRequestDto.getLimit())
                .orderByNotificationIdDesc()
                .offset(notificationsRequestDto.getOffset())
                .fetchNotificationDetailDto(NotificationsDto.class);
    }

    private NotificationResponseDto classifyNotificationsByDate(List<NotificationsDto> NotificationsDtoList) {

        List<NotificationsDto> todayNotificationList = new ArrayList<>();
        List<NotificationsDto> monthNotificationList = new ArrayList<>();
        List<NotificationsDto> yearNotificationList = new ArrayList<>();

        // 현재 날짜
        LocalDate today = LocalDate.now();
        // 한 달 전과 1년 전 날짜 계산
        LocalDate oneMonthAgo = today.minusMonths(1);
        LocalDate oneYearAgo = today.minusYears(1);

        // 날짜 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (NotificationsDto notificationsDto : NotificationsDtoList) {
            LocalDate notificationDate = LocalDate.parse(notificationsDto.getNotificationDate(), formatter);

            // 날짜에 따라 알림을 분류
            if (notificationDate.isEqual(today)) {
                todayNotificationList.add(notificationsDto);
            } else if (!notificationDate.isBefore(oneMonthAgo) && !notificationDate.isAfter(today)) {
                monthNotificationList.add(notificationsDto);
            } else if (!notificationDate.isBefore(oneYearAgo) && !notificationDate.isAfter(today)) {
                yearNotificationList.add(notificationsDto);
            }
        }

        return NotificationResponseDto.builder()
                .todayNotificationList(todayNotificationList)
                .monthNotificationList(monthNotificationList)
                .yearNotificationList(yearNotificationList)
                .build();
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

        Boolean notificationIsView = Boolean.FALSE.equals(
                notificationSelectQueryBuilder
                        .selectFrom(QNotifications.notifications)
                        .findNotificationByMemberId(notificationsRequestDto.getLoginMemberId())
                        .findIsNotificationViewFalse()
                        .fetchNotificationListViewStatus()
        );

        return NotificationResponseDto.builder()
                .notificationIsView(notificationIsView)
                .build();
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
