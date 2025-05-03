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

    private Long totalCount;

    private List<NotificationsDto> notificationList;

}
