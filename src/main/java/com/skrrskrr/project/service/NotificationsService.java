package com.skrrskrr.project.service;

import org.springframework.stereotype.Service;

import java.util.HashMap; import java.util.Map;


public interface NotificationsService {


    Map<String,Object> getNotifications(Long memberId,Long listIndex);
    Map<String,Object> setNotificationIsView(Long notificationId,Long memberId);
    Map<String,Object> setAllNotificationisView(Long memberId);
    Map<String,Object> setDelNotificationIsView(Long memberId);



}
