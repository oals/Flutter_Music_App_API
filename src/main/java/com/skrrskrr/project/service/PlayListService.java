package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListDTO;

import java.util.HashMap; import java.util.Map;

public interface PlayListService {

    Map<String,Object> getPlayList(Long memberId, Long trackId,Long listIndex,boolean isAlbum);

    Map<String,Object> getPlayListInfo(Long memberId,Long playListId);

    Map<String,Object> setPlayListTrack(PlayListDTO playListDTO);

    Map<String,Object> setPlayListInfo(PlayListDTO playListDTO);

    Map<String,Object> setPlayListLike(PlayListDTO playListDTO);

    Map<String,Object> newPlayList(PlayListDTO playListDTO);


}
