package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.HistoryDto;
import com.skrrskrr.project.dto.SearchRequestDto;

import java.util.List;
import java.util.Map;

public interface SearchService {


    Map<String,Object> setSearchHistory(SearchRequestDto searchRequestDto);

    Map<String,Object> getSearchTextHistory(SearchRequestDto searchRequestDto);

    List<String> processSearchKeywords(SearchRequestDto searchRequestDto);

}
