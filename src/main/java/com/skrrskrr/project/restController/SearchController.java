package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.SearchRequestDto;
import com.skrrskrr.project.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class SearchController {


    private final SearchService searchService;

    @GetMapping("/api/setSearchHistory")
    public Map<String,Object> setSearchHistory(SearchRequestDto searchRequestDto) {
        log.info("setSearchHistory");


        return searchService.setSearchHistory(searchRequestDto);
    }

    @GetMapping("/api/getSearchTextHistory")
    public Map<String,Object> getSearchTextHistory(SearchRequestDto searchRequestDto){
        log.info("getSearchTextHistory");
        return searchService.getSearchTextHistory(searchRequestDto);
    }


}
