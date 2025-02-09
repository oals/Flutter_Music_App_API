package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackSearchDTO;

import java.util.HashMap; import java.util.Map;

public interface SearchService {

    Map<String,Object> searchTrack(TrackSearchDTO trackSearchDTO,Long listIndex);

    void setSearchHistory(Long memberId, String searchText);

    Map<String,Object> getSearchInit(Long memberId);

    Map<String,Object> getSearchMore(Long memberId, Long moreId, String searchText,Long listIndex);

}
