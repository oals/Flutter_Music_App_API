package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.TrackResponseDto;
import com.skrrskrr.project.entity.TrackLike;
import java.util.List;

public interface TrackLikeService {


    void setTrackLike(TrackRequestDto trackRequestDto);

    TrackResponseDto getLikeTrackList(TrackRequestDto trackRequestDto);

    TrackLike getTrackLikeEntity(TrackRequestDto trackRequestDto);

    List<Long> getRecommendLikeTrackMemberId(TrackRequestDto trackRequestDto);
}
