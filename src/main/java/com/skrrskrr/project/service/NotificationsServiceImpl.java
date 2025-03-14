package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.NotificationsDto;
import com.skrrskrr.project.dto.NotificationsRequestDto;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.entity.QNotifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;


    @Override
    public Map<String, Object> getNotifications(NotificationsRequestDto notificationsRequestDto) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = fetchNotifications(notificationsRequestDto);

            // 알림 목록 날짜별 분류
            Map<String, List<NotificationsDto>> classifiedNotifications = classifyNotificationsByDate(queryResult);

            // 결과 맵에 알림 목록 추가
            hashMap.putAll(classifiedNotifications);
            hashMap.put("status", "200");

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }

    private List<Notifications> fetchNotifications(NotificationsRequestDto notificationsRequestDto) {
        
        QNotifications qNotifications = QNotifications.notifications;

        return jpaQueryFactory.selectFrom(qNotifications)
                .where(qNotifications.member.memberId.eq(notificationsRequestDto.getLoginMemberId()))
                .orderBy(qNotifications.notificationId.desc())
                .offset(notificationsRequestDto.getListIndex())
                .limit(20)
                .fetch();
    }

    private Map<String, List<NotificationsDto>> classifyNotificationsByDate(List<Notifications> queryResult) {
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

        for (Notifications notification : queryResult) {
            NotificationsDto notificationsDto = modelMapper.map(notification, NotificationsDto.class);
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

        Map<String, List<NotificationsDto>> result = new HashMap<>();
        result.put("todayNotificationsList", todayNotificationList);
        result.put("monthNotificationsList", monthNotificationList);
        result.put("yearNotificationsList", yearNotificationList);

        return result;
    }



    @Override
    public Map<String,Object> setNotificationIsView(NotificationsRequestDto notificationsRequestDto) {

        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            Notifications notifications = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.notificationId.eq(notificationsRequestDto.getNotificationId()))
                    .fetchOne();

            assert notifications != null;

            updateIsNotificationViewStatus(notifications);

            boolean notificationIsView = Boolean.FALSE.equals(
                    jpaQueryFactory.select(
                                    qNotifications.notificationIsView
                            ).from(qNotifications)
                            .where(qNotifications.member.memberId.eq(notificationsRequestDto.getLoginMemberId())
                                    .and(qNotifications.notificationIsView.isFalse()))
                            .fetchFirst()
            );

            hashMap.put("notificationIsView",notificationIsView);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


    @Override
    public Map<String,Object> setAllNotificationisView(NotificationsRequestDto notificationsRequestDto) {

        
        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.member.memberId.eq(notificationsRequestDto.getLoginMemberId())
                            .and(qNotifications.notificationIsView.isFalse()))
                    .fetch();

            for (Notifications notifications : queryResult) {
                updateIsNotificationViewStatus(notifications);
            }

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }



    private void updateIsNotificationViewStatus(Notifications notifications){
        notifications.setNotificationIsView(true);
        entityManager.merge(notifications);
    }



    @Override
    public Map<String,Object> setDelNotificationIsView(NotificationsRequestDto notificationsRequestDto) {
        
        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {

            jpaQueryFactory.delete(qNotifications)
                    .where(qNotifications.member.memberId.eq(notificationsRequestDto.getLoginMemberId()))
                    .execute();

            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


}
