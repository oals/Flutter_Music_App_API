package com.skrrskrr.project.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FcmSendDto {

    private Long memberId;

    private Long notificationId;

    private String title;

    private String body;

    private Boolean notificationIsView;

    private Long notificationType;

    private Long notificationTrackId;

    private Long notificationCommentId;

    private Long notificationMemberId;

    public String toJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

}
