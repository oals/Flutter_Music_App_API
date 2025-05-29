package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.SearchRequestDto;
import com.skrrskrr.project.dto.SearchResponseDto;
import com.skrrskrr.project.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/setSearchHistory")
    public ResponseEntity<Void> setSearchHistory(SearchRequestDto searchRequestDto) {
        log.info("setSearchHistory");
        searchService.setSearchHistory(searchRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/getSearchTextHistory")
    public  ResponseEntity<SearchResponseDto> getSearchTextHistory(SearchRequestDto searchRequestDto){
        log.info("getSearchTextHistory");
        SearchResponseDto searchResponseDto = searchService.getSearchTextHistory(searchRequestDto);
        return ResponseEntity.ok(searchResponseDto);
    }

}
