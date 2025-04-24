package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponseDto {

    List<FollowDto> followingList;

    List<FollowDto> followerList;

}
