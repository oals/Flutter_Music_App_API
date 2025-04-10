package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.MemberRequestDto;
import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.SearchRequestDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.service.TrackLikeService;
import com.skrrskrr.project.service.TrackService;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Log4j2
public class TrackController {

    private final TrackService trackService;
    private final TrackLikeService trackLikeService;


    @PutMapping("/api/setTrackInfo")
    public Map<String,Object> setTrackInfo(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setTrackInfo");
        return trackService.setTrackinfo(trackRequestDto);
    }


    @PutMapping("/api/setLockTrack")
    public Map<String,Object> setLockTrack(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setLockTrack");
        return trackService.setLockTrack(trackRequestDto);
    }


    @GetMapping("/api/getLikeTrack")
    public Map<String,Object> getLikeTrack(TrackRequestDto trackRequestDto){
        log.info("getLikeTrack");
        return trackLikeService.getLikeTrackList(trackRequestDto);
    }

    @GetMapping("/api/getMemberPageTrack")
    public Map<String,Object> getMemberPageTrack(MemberRequestDto memberRequestDto){
        log.info("getMemberPageTrack");
        return trackService.getMemberPageTrack(memberRequestDto);
    }
    @GetMapping("/api/getMemberPagePopularTrack")
    public Map<String,Object> getMemberPagePopularTrack(MemberRequestDto memberRequestDto){
        log.info("getMemberPagePopularTrack");
        return trackService.getMemberPagePopularTrack(memberRequestDto);
    }




    @PostMapping("/api/setTrackLike")
    public Map<String,String> setInsertTrackLike(@RequestBody TrackRequestDto trackRequestDto){
        log.info("setInsertTrackLike");
        return trackLikeService.setTrackLike(trackRequestDto);
    }


    @GetMapping("/api/getLastListenTrackList")
    public Map<String,Object> getLastListenTrackList(TrackRequestDto trackRequestDto) {
        log.info("getLastListenTrackList");
        return trackService.getLastListenTrackList(trackRequestDto);
    }

    @GetMapping("/api/getFollowMemberTrackList")
    public Map<String,Object> getFollowMemberTrackList(TrackRequestDto trackRequestDto) {
        log.info("getFollowMemberTrackList");
        return trackService.getFollowMemberTrackList(trackRequestDto);
    }

    @GetMapping("/api/getSearchTrack")
    public Map<String,Object> getSearchTrack(SearchRequestDto searchRequestDto) {
        log.info("getSearchTrack");
        return trackService.getSearchTrack(searchRequestDto);
    }

    @GetMapping("/api/getPlayListTrackList")
    public Map<String,Object> getPlayListTrackList(PlayListRequestDto playListRequestDto) {
        log.info("getPlayListTrackList");
        return trackService.getPlayListTrackList(playListRequestDto);
    }


    @GetMapping("/api/getTrackInfo")
    public Map<String,Object> getTrackInfo(TrackRequestDto trackRequestDto){
        log.info("getTrackInfo");
        return trackService.getTrackInfo(trackRequestDto);
    }

    @GetMapping("/api/getRecommendTrack")
    public Map<String,Object> getRecommendTrack(TrackRequestDto trackRequestDto){
        log.info("getRecommendTrack");
        return trackService.getRecommendTrack(trackRequestDto);
    }


    @GetMapping("/api/getUploadTrack")
    public Map<String,Object> getUploadTrack(TrackRequestDto trackRequestDto){
        log.info("getUploadTrack");
        return trackService.getUploadTrack(trackRequestDto);
    }



}
