package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowRequestDto extends BaseRequestDto{

    private Long followerId;

    private Long followingId;

}
