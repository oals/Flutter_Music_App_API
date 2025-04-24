package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.PlayListResponseDto;
import com.skrrskrr.project.entity.PlayListLike;

import java.util.Map;

public interface PlayListLikeService {

    void setPlayListLike(PlayListRequestDto playListRequestDto);

    PlayListResponseDto getLikePlayList(PlayListRequestDto playListRequestDto);

}
