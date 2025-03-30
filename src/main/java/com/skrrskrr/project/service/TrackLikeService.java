package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.entity.TrackLike;

import java.util.Map;

public interface TrackLikeService {


    Map<String,String> setTrackLike(TrackRequestDto trackRequestDto);

    Map<String,Object> getLikeTrackList(TrackRequestDto trackRequestDto);

    Boolean getTrackLikeStatus(TrackRequestDto trackRequestDto);

    TrackLike getTrackLikeEntity(TrackRequestDto trackRequestDto);
}
