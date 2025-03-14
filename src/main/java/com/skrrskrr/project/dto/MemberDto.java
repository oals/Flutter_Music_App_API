package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDto {

    private Long memberId;
    private Long loginMemberId;

    private String memberNickName;

    private String memberEmail;

    private String memberInfo;

    private String memberPswd;

    private String memberBirth;

    private String memberAddr;

    private String memberDate;

    private Long memberFollowCnt;

    private Long memberFollowerCnt;

    private String memberImagePath;

    private String memberDeviceToken;

    private String deviceToken;

    private int isFollowedCd;
}
