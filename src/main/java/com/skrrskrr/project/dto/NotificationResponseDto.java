package com.skrrskrr.project.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    private Boolean notificationIsView;

    private List<NotificationsDto> todayNotificationList;

    private List<NotificationsDto> monthNotificationList;

    private List<NotificationsDto> yearNotificationList;

}
