package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.FollowRequestDto;

import java.util.HashMap; import java.util.Map;

public interface FollowService {


    Map<String,Object> setFollow(FollowRequestDto followRequestDto);

    Map<String,Object> getFollow(FollowRequestDto followRequestDto);

    boolean isFollowCheck(Long followerId, Long followingId);

}
