package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.service.TrackLikeService;
import com.skrrskrr.project.service.TrackService;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TrackController {

    private final TrackService trackService;
    private final TrackLikeService trackLikeService;

    @PutMapping("/api/setTrackInfo")
    public ResponseEntity<Void> setTrackInfo(@RequestBody TrackRequestDto trackRequestDto) {
        log.info("setTrackInfo");
        trackService.setTrackInfo(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/api/setLockTrack")
    public ResponseEntity<Void> setLockTrack(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setLockTrack");
        trackService.setLockTrack(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/getLikeTrack")
    public ResponseEntity<TrackResponseDto> getLikeTrack(TrackRequestDto trackRequestDto){
        log.info("getLikeTrack");
        TrackResponseDto trackResponseDto = trackLikeService.getLikeTrackList(trackRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getMemberPageTrack")
    public ResponseEntity<TrackResponseDto> getMemberPageTrack(MemberRequestDto memberRequestDto){
        log.info("getMemberPageTrack");
        TrackResponseDto trackResponseDto = trackService.getMemberPageTrack(memberRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getMemberPagePopularTrack")
    public ResponseEntity<TrackResponseDto> getMemberPagePopularTrack(MemberRequestDto memberRequestDto){
        log.info("getMemberPagePopularTrack");
        TrackResponseDto trackResponseDto = trackService.getMemberPagePopularTrack(memberRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @PostMapping("/api/setTrackLike")
    public ResponseEntity<Void> setTrackLike(@RequestBody TrackRequestDto trackRequestDto) {
        log.info("setTrackLike");
        trackLikeService.setTrackLike(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/setTrackPlayCnt")
    public ResponseEntity<Void> setTrackPlayCnt(@RequestBody TrackRequestDto trackRequestDto) {
        log.info("setTrackPlayCnt");
        trackService.setTrackPlayCnt(trackRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/getSearchTrack")
    public ResponseEntity<TrackResponseDto> getSearchTrack(SearchRequestDto searchRequestDto) {
        log.info("getSearchTrack");
        TrackResponseDto trackResponseDto = trackService.getSearchTrack(searchRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getPlayListTrackList")
    public ResponseEntity<TrackResponseDto> getPlayListTrackList(PlayListRequestDto playListRequestDto) {
        log.info("getPlayListTrackList");
        TrackResponseDto trackResponseDto = trackService.getPlayListTrackList(playListRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getTrackInfo")
    public ResponseEntity<TrackResponseDto> getTrackInfoList(TrackRequestDto trackRequestDto){
        log.info("getTrackInfo");
        TrackResponseDto trackResponseDto = trackService.getTrackInfo(trackRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getAudioPlayerTrackList")
    public ResponseEntity<TrackResponseDto> getAudioPlayerTrackList(TrackRequestDto trackRequestDto){
        log.info("getAudioPlayList");
        TrackResponseDto trackResponseDto = trackService.getAudioPlayerTrackList(trackRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }

    @GetMapping("/api/getUploadTrack")
    public ResponseEntity<TrackResponseDto> getUploadTrack(TrackRequestDto trackRequestDto){
        log.info("getUploadTrack");
        TrackResponseDto trackResponseDto = trackService.getUploadTrack(trackRequestDto);
        return ResponseEntity.ok(trackResponseDto);
    }
}
