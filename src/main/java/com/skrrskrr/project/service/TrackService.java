package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.MemberTrack;
import com.skrrskrr.project.entity.TrackLike;

import java.util.List;
import java.util.Map;

public interface TrackService {

    TrackResponseDto getSearchTrack(SearchRequestDto searchRequestDto);

    TrackResponseDto getPlayListTrackList(PlayListRequestDto playListRequestDto);

    TrackResponseDto getMemberPageTrack(MemberRequestDto memberRequestDto);

    TrackResponseDto getMemberPagePopularTrack(MemberRequestDto memberRequestDto);

    TrackResponseDto getTrackInfo(TrackRequestDto trackRequestDto);

    TrackResponseDto getAudioPlayerTrackList(TrackRequestDto trackRequestDto);

    TrackResponseDto getUploadTrack(TrackRequestDto trackRequestDto);

    TrackResponseDto getRecommendTrack(TrackRequestDto trackRequestDto);

    TrackResponseDto getLastListenTrackList(TrackRequestDto trackRequestDto);

    Boolean updateTrackImage(UploadDto uploadDto);

    Long getMemberTrackListCnt(MemberRequestDto memberRequestDto);

    Long getTrackLastId();

    void saveTrack(UploadDto uploadDto);

    void setTrackInfo(TrackRequestDto trackRequestDto);

    void setLockTrack(TrackRequestDto trackRequestDto);

}
