package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.NotificationsDTO;
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
    public Map<String, Object> getNotifications(Long memberId, Long listIndex) {
        Map<String, Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = fetchNotifications(memberId, listIndex);

            // 알림 목록 날짜별 분류
            Map<String, List<NotificationsDTO>> classifiedNotifications = classifyNotificationsByDate(queryResult);

            // 결과 맵에 알림 목록 추가
            hashMap.putAll(classifiedNotifications);
            hashMap.put("status", "200");

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status", "500");
        }

        return hashMap;
    }

    private List<Notifications> fetchNotifications(Long memberId, Long listIndex) {
        
        QNotifications qNotifications = QNotifications.notifications;

        return jpaQueryFactory.selectFrom(qNotifications)
                .where(qNotifications.member.memberId.eq(memberId))
                .orderBy(qNotifications.notificationId.desc())
                .offset(listIndex)
                .limit(20)
                .fetch();
    }

    private Map<String, List<NotificationsDTO>> classifyNotificationsByDate(List<Notifications> queryResult) {
        List<NotificationsDTO> todayNotificationList = new ArrayList<>();
        List<NotificationsDTO> monthNotificationList = new ArrayList<>();
        List<NotificationsDTO> yearNotificationList = new ArrayList<>();

        // 현재 날짜
        LocalDate today = LocalDate.now();
        // 한 달 전과 1년 전 날짜 계산
        LocalDate oneMonthAgo = today.minusMonths(1);
        LocalDate oneYearAgo = today.minusYears(1);

        // 날짜 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Notifications notification : queryResult) {
            NotificationsDTO notificationsDTO = modelMapper.map(notification, NotificationsDTO.class);
            LocalDate notificationDate = LocalDate.parse(notificationsDTO.getNotificationDate(), formatter);

            // 날짜에 따라 알림을 분류
            if (notificationDate.isEqual(today)) {
                todayNotificationList.add(notificationsDTO);
            } else if (!notificationDate.isBefore(oneMonthAgo) && !notificationDate.isAfter(today)) {
                monthNotificationList.add(notificationsDTO);
            } else if (!notificationDate.isBefore(oneYearAgo) && !notificationDate.isAfter(today)) {
                yearNotificationList.add(notificationsDTO);
            }
        }

        Map<String, List<NotificationsDTO>> result = new HashMap<>();
        result.put("todayNotificationsList", todayNotificationList);
        result.put("monthNotificationsList", monthNotificationList);
        result.put("yearNotificationsList", yearNotificationList);

        return result;
    }



    @Override
    public Map<String,Object> setNotificationIsView(Long notificationId,Long memberId) {

        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            Notifications notifications = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.notificationId.eq(notificationId))
                    .fetchOne();

            assert notifications != null;

            updateIsNotificationViewStatus(notifications);

            boolean notificationIsView = Boolean.FALSE.equals(
                    jpaQueryFactory.select(
                                    qNotifications.notificationIsView
                            ).from(qNotifications)
                            .where(qNotifications.member.memberId.eq(memberId)
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
    public Map<String,Object> setAllNotificationisView(Long memberId) {

        
        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.member.memberId.eq(memberId)
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
    public Map<String,Object> setDelNotificationIsView(Long memberId) {
        
        QNotifications qNotifications = QNotifications.notifications;
        Map<String,Object> hashMap = new HashMap<>();

        try {

            jpaQueryFactory.delete(qNotifications)
                    .where(qNotifications.member.memberId.eq(memberId))
                    .execute();

            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }
        return hashMap;
    }


}
