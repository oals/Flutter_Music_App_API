package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import java.util.List;

public interface PlayListService {

    List<PlayListDto> getRecommendPlayList(Long loginMemberId);

    List<PlayListDto> getRecommendAlbum(Long loginMemberId);

    PlayListResponseDto getSearchPlayList(SearchRequestDto searchRequestDto);

    PlayListResponseDto getMemberPagePlayList(PlayListRequestDto playListRequestDto);

    PlayListResponseDto getMemberPageAlbums(PlayListRequestDto playListRequestDto);

    PlayListResponseDto getPlayList(PlayListRequestDto playListRequestDto);

    PlayListResponseDto getPlayListInfo(PlayListRequestDto playListRequestDto);

    void setPlayListTrack(PlayListRequestDto playListRequestDto);

    void setPlayListInfo(PlayListRequestDto playListRequestDto);

    Long newPlayList(PlayListRequestDto playListRequestDto);

}
