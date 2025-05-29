package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.FollowRequestDto;
import com.skrrskrr.project.dto.FollowResponseDto;

public interface FollowService {

    void setFollow(FollowRequestDto followRequestDto);

    FollowResponseDto getFollow(FollowRequestDto followRequestDto);

    Boolean isFollowCheck(Long followerId, Long followingId);

}
