package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.FollowRequestDto;
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
    public Map<String,Object> setFollow(@RequestBody FollowRequestDto followRequestDto){
        return followService.setFollow(followRequestDto);
    }


    @GetMapping(value = "/api/getFollow")
    public Map<String,Object> getFollow(FollowRequestDto followRequestDto){

        log.info("getFollow");
        return followService.getFollow(followRequestDto);
    }




}
