package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.NotificationResponseDto;
import com.skrrskrr.project.dto.NotificationsRequestDto;
import org.springframework.stereotype.Service;

import java.util.HashMap; import java.util.Map;


public interface NotificationsService {


    NotificationResponseDto getNotifications(NotificationsRequestDto notificationsRequestDto);

    NotificationResponseDto setNotificationIsView(NotificationsRequestDto NotificationsRequestDto);

    void setAllNotificationIsView(NotificationsRequestDto notificationsRequestDto);

    void setDelNotificationIsView(NotificationsRequestDto notificationsRequestDto);



}
