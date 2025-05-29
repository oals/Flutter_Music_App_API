package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowDto {

    private Long followMemberId;

    private String followNickName;

    private String followImagePath;

    private Long isFollowedCd;

    private Boolean isMutualFollow;

}
