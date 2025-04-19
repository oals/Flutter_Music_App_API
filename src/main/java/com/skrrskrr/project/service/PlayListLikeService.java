package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.entity.PlayListLike;

import java.util.Map;

public interface PlayListLikeService {

    Map<String,Object> setPlayListLike(PlayListRequestDto playListRequestDto);

    PlayListLike selectPlayListLikeEntity(PlayListRequestDto playListRequestDto);

    Map<String,Object> getLikePlayList(PlayListRequestDto playListRequestDto);

}
