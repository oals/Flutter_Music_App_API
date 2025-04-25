package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/api/firstLoad")
    public ResponseEntity<HomeResponseDto> firstLoad(HomeRequestDto homeRequestDto) {
        log.info("firstLoad");
        HomeResponseDto homeResponseDto = homeService.firstLoad(homeRequestDto);
        return ResponseEntity.ok(homeResponseDto);
    }


}
