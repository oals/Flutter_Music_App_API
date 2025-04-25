package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeRequestDto {

    private String memberEmail;

    private Long loginMemberId;

    private String deviceToken;
}
