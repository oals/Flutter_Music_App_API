package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FcmSendDTO {

    private Long memberId;
    private String title;
    private String body;
    private Long notificationType;
    private Long notificationTrackId;
    private Long notificationCommentId;
    private Long notificationMemberId;


}
