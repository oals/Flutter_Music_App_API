package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.service.PlayListLikeService;
import com.skrrskrr.project.service.PlayListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class PlayListController {

    final private PlayListService playListService;
    final private PlayListLikeService playListLikeService;

    @PostMapping("/api/setPlayListLike")
    public ResponseEntity<Void> setPlayListLike(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListLike");
        playListLikeService.setPlayListLike(playListRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/newPlayList")
    public ResponseEntity<Void> newPlayList(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("newPlayList");
        playListService.newPlayList(playListRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/setPlayListTrack")
    public ResponseEntity<Void> setPlayListTrack(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListTrack");
        playListService.setPlayListTrack(playListRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/setPlayListInfo")
    public ResponseEntity<Void> setPlayListInfo(@RequestBody PlayListRequestDto playListRequestDto){
        log.info("setPlayListInfo");
        playListService.setPlayListInfo(playListRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/getPlayList")
    public ResponseEntity<PlayListResponseDto> getPlayList(PlayListRequestDto playListRequestDto) {
        log.info("getPlayList");
        PlayListResponseDto playListResponseDto = playListService.getPlayList(playListRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }

    @GetMapping("/api/getLikePlayList")
    public ResponseEntity<PlayListResponseDto> getLikePlayList(PlayListRequestDto playListRequestDto) {
        log.info("getLikePlayList");
        PlayListResponseDto playListResponseDto = playListLikeService.getLikePlayList(playListRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }

    @GetMapping("/api/getPlayListInfo")
    public ResponseEntity<PlayListResponseDto> getPlayListInfo(PlayListRequestDto playListRequestDto) {
        log.info("getPlayListInfo");
        PlayListResponseDto playListResponseDto = playListService.getPlayListInfo(playListRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }

    @GetMapping("/api/getMemberPagePlayList")
    public ResponseEntity<PlayListResponseDto> getMemberPagePlayList(PlayListRequestDto playListRequestDto) {
        log.info("getMemberPagePlayList");
        PlayListResponseDto playListResponseDto = playListService.getMemberPagePlayList(playListRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }

    @GetMapping("/api/getMemberPageAlbums")
    public ResponseEntity<PlayListResponseDto> getMemberPageAlbums(PlayListRequestDto playListRequestDto) {
        log.info("getMemberPageAlbums");
        PlayListResponseDto playListResponseDto = playListService.getMemberPageAlbums(playListRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }

    @GetMapping("/api/getSearchPlayList")
    public ResponseEntity<PlayListResponseDto> getSearchTrack(SearchRequestDto searchRequestDto) {
        log.info("getSearchTrack");
        PlayListResponseDto playListResponseDto = playListService.getSearchPlayList(searchRequestDto);
        return ResponseEntity.ok(playListResponseDto);
    }
}
