package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.NotificationsRequestDto;
import org.springframework.stereotype.Service;

import java.util.HashMap; import java.util.Map;


public interface NotificationsService {


    Map<String,Object> getNotifications(NotificationsRequestDto notificationsRequestDto);
    Map<String,Object> setNotificationIsView(NotificationsRequestDto NotificationsRequestDto);
    Map<String,Object> setAllNotificationisView(NotificationsRequestDto notificationsRequestDto);
    Map<String,Object> setDelNotificationIsView(NotificationsRequestDto notificationsRequestDto);



}
