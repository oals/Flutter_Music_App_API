package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.MemberDto;
import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.dto.TrackRequestDto;
import com.skrrskrr.project.dto.TrackResponseDto;
import com.skrrskrr.project.entity.TrackLike;

import java.util.List;
import java.util.Map;

public interface TrackLikeService {


    void setTrackLike(TrackRequestDto trackRequestDto);

    TrackResponseDto getLikeTrackList(TrackRequestDto trackRequestDto);

    TrackLike getTrackLikeEntity(TrackRequestDto trackRequestDto);

    List<Long> getRecommendLikeTrackMemberId(TrackRequestDto trackRequestDto);
}
