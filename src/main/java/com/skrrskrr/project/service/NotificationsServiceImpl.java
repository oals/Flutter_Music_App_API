package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.NotificationsDTO;
import com.skrrskrr.project.entity.Notifications;
import com.skrrskrr.project.entity.QNotifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class NotificationsServiceImpl implements NotificationsService{

    @PersistenceContext
    EntityManager em;



    @Override
    public HashMap<String, Object> getNotifications(Long memberId, Long listIndex) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QNotifications qNotifications = QNotifications.notifications;
        HashMap<String, Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.member.memberId.eq(memberId)
//                        .and(qNotifications.notificationIsView.isFalse())
                    )
                    .orderBy(qNotifications.notificationId.desc())
                    .offset(listIndex)
                    .limit(20)
                    .fetch();


            List<NotificationsDTO> todayNotificationList = new ArrayList<>();
            List<NotificationsDTO> monthNotificationList = new ArrayList<>();
            List<NotificationsDTO> yearNotificationList = new ArrayList<>();

            // 현재 날짜
            LocalDate today = LocalDate.now();

            // 한 달 전과 1년 전 날짜 계산
            LocalDate oneMonthAgo = today.minusMonths(1);
            LocalDate oneYearAgo = today.minusYears(1);

            // 날짜 포맷터 (날짜 형식이 맞다면 이 부분을 사용하여 날짜를 파싱)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 알림 목록을 날짜별로 분류
            for (int i = 0; i < queryResult.size(); i++) {
                NotificationsDTO notificationsDTO = NotificationsDTO.builder()
                        .notificationId(queryResult.get(i).getNotificationId())
                        .notificationType(queryResult.get(i).getNotificationType())
                        .notificationMsg(queryResult.get(i).getNotificationMsg())
                        .notificationDate(queryResult.get(i).getNotificationDate())
                        .notificationIsView(queryResult.get(i).isNotificationIsView())
                        .notificationTrackId(queryResult.get(i).getNotificationTrackId())
                        .notificationCommentId(queryResult.get(i).getNotificationCommentId())
                        .notificationMemberId(queryResult.get(i).getNotificationMemberId())
                        .build();

                // notificationDate를 LocalDate로 변환 (형식이 "yyyy-MM-dd"라고 가정)
                LocalDate notificationDate = LocalDate.parse(notificationsDTO.getNotificationDate(), formatter);

                // 오늘 날짜와 비교
                if (notificationDate.isEqual(today)) {
                    todayNotificationList.add(notificationsDTO);
                }
                // 한 달 전과 오늘 사이에 포함되는 날짜
                else if (!notificationDate.isBefore(oneMonthAgo) && !notificationDate.isAfter(today)) {
                    monthNotificationList.add(notificationsDTO);
                }
                // 1년 전과 오늘 사이에 포함되는 날짜
                else if (!notificationDate.isBefore(oneYearAgo) && !notificationDate.isAfter(today)) {
                    yearNotificationList.add(notificationsDTO);
                }
            }

            // 결과 맵에 알림 목록 추가
            hashMap.put("todayNotificationsList", todayNotificationList);
            hashMap.put("monthNotificationsList", monthNotificationList);
            hashMap.put("yearNotificationsList", yearNotificationList);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }





    @Override
    public HashMap<String,Object> setNotificationIsView(Long notificationId,Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QNotifications qNotifications = QNotifications.notifications;
        HashMap<String,Object> hashMap = new HashMap<>();

        try {
            Notifications notifications = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.notificationId.eq(notificationId))
                    .fetchOne();

            assert notifications != null;
            notifications.setNotificationIsView(true);

            em.merge(notifications);


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
    public HashMap<String,Object> setAllNotificationisView(Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QNotifications qNotifications = QNotifications.notifications;
        HashMap<String,Object> hashMap = new HashMap<>();

        try {
            List<Notifications> queryResult = jpaQueryFactory.selectFrom(qNotifications)
                    .where(qNotifications.member.memberId.eq(memberId)
                            .and(qNotifications.notificationIsView.isFalse()))
                    .fetch();

            for (Notifications notifications : queryResult) {
                notifications.setNotificationIsView(true);
                em.merge(notifications);
            }
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public HashMap<String,Object> setDelNotificationIsView(Long memberId) {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QNotifications qNotifications = QNotifications.notifications;
        HashMap<String,Object> hashMap = new HashMap<>();

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
