package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.UploadDto;
import com.skrrskrr.project.entity.TrackLike;

import java.util.List;
import java.util.Map;


public interface TrackService {


    Map<String,Object> saveTrack(UploadDto uploadDto);

    Map<String,Object> updateTrackImage(UploadDto uploadDto);

    Map<String,Object> setTrackinfo(TrackRequestDto trackRequestDto);

    Map<String,String> setTrackLike(TrackRequestDto trackRequestDto);

    Map<String,Object> getLikeTrack(TrackRequestDto trackRequestDto);

    List<TrackDto> getLikeTrackList(TrackRequestDto trackRequestDto);

    List<TrackDto> getFollowMemberTrackList(TrackRequestDto trackRequestDto);

    Long getLikeTrackListCnt(TrackRequestDto trackRequestDto);

    Map<String,Object> setLockTrack(TrackRequestDto trackRequestDto);

    Map<String,Object> getTrackInfo(TrackRequestDto trackRequestDto);

    Map<String,Object> getUploadTrack(TrackRequestDto trackRequestDto);

    List<TrackDto> getUploadTrackList(TrackRequestDto trackRequestDto);

    Long getUploadTrackListCnt(TrackRequestDto trackRequestDto);

    TrackLike getTrackLikeStatus(TrackRequestDto trackRequestDto);

    Map<String, Object> getRecommendTrack(TrackRequestDto trackRequestDto);

    List<TrackDto> getTrendingTrackList(TrackRequestDto trackRequestDto);

    Long getTrackLastId();

}
