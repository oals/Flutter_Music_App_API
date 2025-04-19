package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;

import java.util.List;
import java.util.Map;

public interface PlayListService {

    Map<String,Object> getRecommendPlayList(PlayListRequestDto playListRequestDto);

    Map<String,Object> getRecommendAlbum(PlayListRequestDto playListRequestDto);

    Map<String, Object> getSearchPlayList(SearchRequestDto searchRequestDto);

    Map<String, Object> getMemberPagePlayList(PlayListRequestDto playListRequestDto);

    Map<String, Object> getMemberPageAlbums(PlayListRequestDto playListRequestDto);

    Map<String,Object> getPlayList(PlayListRequestDto playListRequestDto);

    Map<String,Object> getPlayListInfo(PlayListRequestDto playListRequestDto);

    Map<String,Object> setPlayListTrack(PlayListRequestDto playListRequestDto);

    Map<String,Object> setPlayListInfo(PlayListRequestDto playListRequestDto);

    Map<String,Object> newPlayList(PlayListRequestDto playListRequestDto);

    List<PlayListDto> getMemberPlayList(PlayListRequestDto playListRequestDto);

    Long getMemberPlayListCnt(PlayListRequestDto playListRequestDto);

    List<PlayListDto> getSearchPlayLists(SearchRequestDto searchRequestDto);

    Long getSearchPlayListCnt(SearchRequestDto searchRequestDto);

    List<PlayListDto> getRecommendPlayLists(PlayListRequestDto playListRequestDto);
}
