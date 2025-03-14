package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationsRequestDto extends BaseRequestDto{

    private Long notificationId;

    private Long loginMemberId;

}
