package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.MemberTrack;
import com.skrrskrr.project.entity.TrackLike;

import java.util.List;
import java.util.Map;


public interface TrackService {


    Map<String,Object> saveTrack(UploadDto uploadDto);

    Map<String,Object> updateTrackImage(UploadDto uploadDto);

    Map<String,Object> setTrackinfo(TrackRequestDto trackRequestDto);

    List<TrackDto> getFollowMemberTrackList(TrackRequestDto trackRequestDto);

    Map<String,Object> setLockTrack(TrackRequestDto trackRequestDto);

    Map<String,Object> getTrackInfo(TrackRequestDto trackRequestDto);

    Map<String,Object> getUploadTrack(TrackRequestDto trackRequestDto);

    Map<String, Object> getRecommendTrack(TrackRequestDto trackRequestDto);

    List<TrackDto> getPopularMemberTrackList(MemberRequestDto memberRequestDto);

    List<TrackDto> getAllMemberTrackList(MemberRequestDto memberRequestDto);

    Long getMemberTrackListCnt(MemberRequestDto memberRequestDto);

    List<TrackDto> getPlayListTracks(PlayListRequestDto playListRequestDto);

    List<TrackDto> getTrendingTrackList(TrackRequestDto trackRequestDto);

    List<SearchDto> getSearchTrackList(SearchRequestDto searchRequestDto);

    Long getSearchTrackListCnt(SearchRequestDto searchRequestDto);

    Long getTrackLastId();

    List<TrackDto> getLastListenTrackList(TrackRequestDto trackRequestDto);

}
