package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationsDto {

    private Long notificationId;

    private String notificationMsg;

    private Long notificationType;

    private String notificationDate;

    private Long memberId;

    private String memberImagePath;

    private Long notificationTrackId;

    private Long notificationCommentId;

    private Long notificationMemberId;

    private Boolean notificationIsView;

}
