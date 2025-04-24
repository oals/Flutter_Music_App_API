package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {

    List<MemberDto> memberList;

    List<FollowDto> followMemberList;

    MemberDto member;

    String memberImagePath;

    Long totalCount;

}
