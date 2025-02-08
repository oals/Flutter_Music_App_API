package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.TrackSearchDTO;

import java.util.HashMap;

public interface SearchService {

    HashMap<String,Object> searchTrack(TrackSearchDTO trackSearchDTO,Long listIndex);

    void setSearchHistory(Long memberId, String searchText);

    HashMap<String,Object> getSearchInit(Long memberId);

    HashMap<String,Object> getSearchMore(Long memberId, Long moreId, String searchText,Long listIndex);

}
