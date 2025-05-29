package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.PlayListRequestDto;
import com.skrrskrr.project.dto.PlayListResponseDto;
import java.util.List;

public interface PlayListLikeService {

    void setPlayListLike(PlayListRequestDto playListRequestDto);

    PlayListResponseDto getLikePlayList(PlayListRequestDto playListRequestDto);

    List<Long> getRecommendLikePlayListsMemberId(PlayListRequestDto playListRequestDto);

}
