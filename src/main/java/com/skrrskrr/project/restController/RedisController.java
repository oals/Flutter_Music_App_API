package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.redisService.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/api/setLastListenTrackId")
    public Map<String,Object> setLastTrack(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setLastListenTrack");
        return redisService.setLastListenTrackId(trackRequestDto);
    }


    @GetMapping("/api/getLastListenTrackId")
    public Map<String,Object> getLastListenTrack(TrackRequestDto trackRequestDto){
        log.info("getLastListenTrackId");
        return redisService.getLastListenTrackId(trackRequestDto);
    }


}
