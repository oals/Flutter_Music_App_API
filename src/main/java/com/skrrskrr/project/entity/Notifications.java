package com.skrrskrr.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String notificationMsg;

    private Long notificationType;

    private String notificationDate;

    private Long notificationTrackId;

    private Long notificationCommentId;

    private Long notificationMemberId;

    private Boolean notificationIsView;

}
