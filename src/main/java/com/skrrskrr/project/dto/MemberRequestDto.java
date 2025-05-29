package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberRequestDto extends BaseRequestDto{

    private String memberEmail;

    private String deviceToken;

    private String memberNickName;

    private String memberInfo;

}
