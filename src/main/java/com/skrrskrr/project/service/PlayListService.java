package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListDTO;

import java.util.HashMap;

public interface PlayListService {

    HashMap<String,Object> getPlayList(Long memberId, Long trackId,Long listIndex,boolean isAlbum);

    HashMap<String,Object> getPlayListInfo(Long memberId,Long playListId);

    HashMap<String,Object> setPlayListTrack(PlayListDTO playListDTO);

    HashMap<String,Object> setPlayListInfo(PlayListDTO playListDTO);

    HashMap<String,Object> setPlayListLike(PlayListDTO playListDTO);

    HashMap<String,Object> newPlayList(PlayListDTO playListDTO);


}
