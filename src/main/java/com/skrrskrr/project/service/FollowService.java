package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.FollowRequestDto;
import com.skrrskrr.project.dto.FollowResponseDto;
import com.skrrskrr.project.dto.MemberResponseDto;

import java.util.HashMap; import java.util.Map;

public interface FollowService {


    void setFollow(FollowRequestDto followRequestDto);

    FollowResponseDto getFollow(FollowRequestDto followRequestDto);

    Boolean isFollowCheck(Long followerId, Long followingId);

}
