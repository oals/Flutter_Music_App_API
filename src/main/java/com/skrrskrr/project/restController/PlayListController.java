package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.service.PlayListLikeService;
import com.skrrskrr.project.service.PlayListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class PlayListController {

    final private PlayListService playListService;
    final private PlayListLikeService playListLikeService;


    @PostMapping("/api/setPlayListLike")
    public Map<String,Object> setPlayListLike(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListLike");
        return playListLikeService.setPlayListLike(playListRequestDto);
    }

    @PostMapping("/api/newPlayList")
    public Map<String, Object> newPlayList(@RequestBody PlayListRequestDto playListRequestDto){

        log.info("newPlayList");
        return playListService.newPlayList(playListRequestDto);
    }



    @PostMapping("/api/setPlayListTrack")
    public Map<String,Object> setPlayListTrack(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListTrack");
        return playListService.setPlayListTrack(playListRequestDto);
    }


    @PostMapping("/api/setPlayListInfo")
    public Map<String,Object> setPlayListInfo(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListInfo");
        return playListService.setPlayListInfo(playListRequestDto);
    }


    @GetMapping("/api/getPlayList")
    public Map<String,Object> getPlayList(PlayListRequestDto playListRequestDto) {

        log.info("getPlayList");
        return playListService.getPlayList(playListRequestDto);
    }

    @GetMapping("/api/getPlayListInfo")
    public Map<String,Object> getPlayListInfo(PlayListRequestDto playListRequestDto) {
        log.info("getPlayListInfo");

        return playListService.getPlayListInfo(playListRequestDto);
    }

}
