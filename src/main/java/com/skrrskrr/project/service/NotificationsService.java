package com.skrrskrr.project.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;


public interface NotificationsService {


    HashMap<String,Object> getNotifications(Long memberId,Long listIndex);
    HashMap<String,Object> setNotificationIsView(Long notificationId,Long memberId);
    HashMap<String,Object> setAllNotificationisView(Long memberId);
    HashMap<String,Object> setDelNotificationIsView(Long memberId);



}
