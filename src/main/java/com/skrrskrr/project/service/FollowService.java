package com.skrrskrr.project.service;

import java.util.HashMap; import java.util.Map;

public interface FollowService {


    Map<String,Object> setFollow(Long followerId, Long followingId);

    Map<String,Object> getFollow(Long memberId);

    boolean isFollowCheck(Long followerId, Long followingId);

}
