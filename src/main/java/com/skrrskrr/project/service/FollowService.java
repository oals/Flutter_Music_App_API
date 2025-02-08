package com.skrrskrr.project.service;

import java.util.HashMap;

public interface FollowService {


    HashMap<String,Object> setFollow(Long followerId, Long followingId);

    HashMap<String,Object> getFollow(Long memberId);

    HashMap<String,Object> isFollowCheck(Long followerId, Long followingId);

}
