package com.skrrskrr.project.restController;

import com.skrrskrr.project.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Log4j2
public class MainController {

    private final MainService mainService;


    @GetMapping("/api/firstLoad")
    public HashMap<String, Object> firstLoad(@RequestParam("memberId") Long memberId ){
        log.info("firstLoad");
        return mainService.firstLoad(memberId);
    }





}
