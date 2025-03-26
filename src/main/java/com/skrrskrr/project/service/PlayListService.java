package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.PlayListRequestDto;

import java.util.List;
import java.util.Map;

public interface PlayListService {

    Map<String,Object> getPlayList(PlayListRequestDto playListRequestDto);

    Map<String,Object> getPlayListInfo(PlayListRequestDto playListRequestDto);

    Map<String,Object> setPlayListTrack(PlayListRequestDto playListRequestDto);

    Map<String,Object> setPlayListInfo(PlayListRequestDto playListRequestDto);

    Map<String,Object> setPlayListLike(PlayListRequestDto playListRequestDto);

    Map<String,Object> newPlayList(PlayListRequestDto playListRequestDto);

    List<PlayListDto> getPopularPlayList(PlayListRequestDto playListRequestDto);
}
