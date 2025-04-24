package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.MemberRequestDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.TrackResponseDto;
import com.skrrskrr.project.redisService.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/api/setLastListenTrackId")
    public ResponseEntity<Void> setLastTrack(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setLastListenTrack");
        redisService.setLastListenTrackId(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/setAudioPlayerTrackIdList")
    public ResponseEntity<Void> setAudioPlayerTrackIdList(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setAudioPlayerTrackIdList");
        redisService.setAudioPlayerTrackIdList(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/getLastListenTrackId")
    public ResponseEntity<TrackResponseDto> getLastListenTrack(TrackRequestDto trackRequestDto){
        log.info("getLastListenTrackId");
        TrackResponseDto trackResponseDto = redisService.getLastListenTrackId(trackRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

}
