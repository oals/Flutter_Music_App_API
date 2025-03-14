package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.SearchRequestDto;

import java.util.Map;

public interface SearchService {

    Map<String,Object> search(SearchRequestDto searchRequestDto);

    void setSearchHistory(SearchRequestDto searchRequestDto);

    Map<String,Object> getSearchTextHistory(SearchRequestDto searchRequestDto);

    Map<String,Object> getSearchMore(SearchRequestDto searchRequestDto);

}
