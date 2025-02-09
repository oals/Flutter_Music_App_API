package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.PlayListDTO;
import com.skrrskrr.project.service.PlayListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map; import java.util.Map;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class PlayListController {

    final private PlayListService playListService;


    @PostMapping("/api/setPlayListLike")
    public Map<String,Object> setPlayListLike(@RequestBody PlayListDTO playListDTO){
        log.info("setPlayListLike");
        return playListService.setPlayListLike(playListDTO);
    }

    @PostMapping("/api/newPlayList")
    public Map<String, Object> newPlayList(@RequestBody PlayListDTO playListDTO){

        log.info("newPlayList");
        return playListService.newPlayList(playListDTO);
    }



    @PostMapping("/api/setPlayListTrack")
    public Map<String,Object> setPlayListTrack(@RequestBody PlayListDTO playListDTO){
        log.info("setPlayListTrack");
        return playListService.setPlayListTrack(playListDTO);
    }




    @PostMapping("/api/setPlayListInfo")
    public Map<String,Object> setPlayListInfo(@RequestBody PlayListDTO playListDTO){
        log.info("setPlayListInfo");
        return playListService.setPlayListInfo(playListDTO);
    }


    @GetMapping("/api/getPlayList")
    public Map<String,Object> getPlayList(
            @RequestParam("memberId") Long memberId,
            @RequestParam("trackId") Long trackId,
            @RequestParam("listIndex") Long listIndex,
            @RequestParam("isAlbum") boolean isAlbum
            ) {

        log.info("getPlayList");
        return playListService.getPlayList(memberId,trackId,listIndex,isAlbum);
    }

    @GetMapping("/api/getPlayListInfo")
    public Map<String,Object> getPlayListInfo(@RequestParam("memberId") Long memberId, @RequestParam("playListId") Long playListId) {
        log.info("getPlayListInfo");
        return playListService.getPlayListInfo(memberId,playListId);
    }

}
