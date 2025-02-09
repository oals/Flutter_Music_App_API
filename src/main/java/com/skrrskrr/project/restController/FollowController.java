package com.skrrskrr.project.restController;

import com.skrrskrr.project.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class FollowController {

    private final FollowService followService;

    @PostMapping(value = "/api/setFollow")
    public Map<String,Object> setFollow(@RequestBody Map<String,Object> hashMap){

        Long followerId = Long.valueOf(hashMap.get("followerId").toString());
        Long followingId = Long.valueOf(hashMap.get("followingId").toString());

        return followService.setFollow(followerId,followingId);
    }


    @GetMapping(value = "/api/getFollow")
    public Map<String,Object> getFollow(@RequestParam Long memberId){

        log.info("getFollow");
        return followService.getFollow(memberId);
    }




}
