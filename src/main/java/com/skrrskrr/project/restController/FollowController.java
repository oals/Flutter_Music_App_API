package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class FollowController {

    private final FollowService followService;

    @PostMapping(value = "/api/setFollow")
    public ResponseEntity<Void> setFollow(@RequestBody FollowRequestDto followRequestDto){
        followService.setFollow(followRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping(value = "/api/getFollow")
    public ResponseEntity<FollowResponseDto> getFollow(FollowRequestDto followRequestDto){
        log.info("getFollow");
        FollowResponseDto followResponseDto = followService.getFollow(followRequestDto);
        return ResponseEntity.ok(followResponseDto);
    }
}
