package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.NotificationResponseDto;
import com.skrrskrr.project.dto.NotificationsRequestDto;

public interface NotificationsService {

    NotificationResponseDto getNotifications(NotificationsRequestDto notificationsRequestDto);

    NotificationResponseDto setNotificationIsView(NotificationsRequestDto NotificationsRequestDto);

    Boolean selectNotificationIsNotView(Long loginMemberId);

    void setAllNotificationIsView(NotificationsRequestDto notificationsRequestDto);

    void setDelNotificationIsView(NotificationsRequestDto notificationsRequestDto);



}
