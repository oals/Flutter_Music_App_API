package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import java.util.List;

public interface TrackService {

    TrackResponseDto getSearchTrack(SearchRequestDto searchRequestDto);

    TrackResponseDto getPlayListTrackList(PlayListRequestDto playListRequestDto);

    TrackResponseDto getMemberPageTrack(MemberRequestDto memberRequestDto);

    TrackResponseDto getMemberPagePopularTrack(MemberRequestDto memberRequestDto);

    TrackResponseDto getTrackInfo(TrackRequestDto trackRequestDto);

    TrackResponseDto getAudioPlayerTrackList(TrackRequestDto trackRequestDto);

    TrackResponseDto getUploadTrack(TrackRequestDto trackRequestDto);

    List<TrackDto> getRecommendTrack(Long loginMemberId);

    List<TrackDto> getLastListenTrackList(Long loginMemberId);

    Boolean updateTrackImage(UploadDto uploadDto);

    Long getMemberTrackListCnt(MemberRequestDto memberRequestDto);

    Long getTrackLastId();

    void setTrackPlayCnt(TrackRequestDto trackRequestDto);

    void saveTrack(UploadDto uploadDto);

    void setTrackInfo(TrackRequestDto trackRequestDto);

    void setLockTrack(TrackRequestDto trackRequestDto);

}
