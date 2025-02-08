package com.skrrskrr.project.dto;

import com.skrrskrr.project.entity.Member;
import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationsDTO {

    private Long notificationId;

    private String notificationMsg;

    private Long notificationType;

    private String notificationDate;

    private Long memberId;

    private Long notificationTrackId;

    private Long notificationCommentId;

    private Long notificationMemberId;

    private boolean notificationIsView;

}
