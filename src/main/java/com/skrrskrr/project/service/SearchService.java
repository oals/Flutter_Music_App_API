package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.SearchRequestDto;
import com.skrrskrr.project.dto.SearchResponseDto;
import java.util.List;

public interface SearchService {

    SearchResponseDto getSearchTextHistory(SearchRequestDto searchRequestDto);

    void setSearchHistory(SearchRequestDto searchRequestDto);

    List<String> processSearchKeywords(SearchRequestDto searchRequestDto);

}
