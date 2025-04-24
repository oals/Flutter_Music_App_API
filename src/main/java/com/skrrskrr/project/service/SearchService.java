package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.HistoryDto;
import com.skrrskrr.project.dto.SearchRequestDto;
import com.skrrskrr.project.dto.SearchResponseDto;

import java.util.List;
import java.util.Map;

public interface SearchService {

    SearchResponseDto getSearchTextHistory(SearchRequestDto searchRequestDto);

    void setSearchHistory(SearchRequestDto searchRequestDto);

    List<String> processSearchKeywords(SearchRequestDto searchRequestDto);

}
