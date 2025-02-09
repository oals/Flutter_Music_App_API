package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.TrackSearchDTO;
import com.skrrskrr.project.service.SearchService;
import com.skrrskrr.project.service.TrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap; import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class SearchController {


    private final SearchService searchService;

    @GetMapping("/api/getSearchTrack")
    public Map<String,Object> getSearchTrack(@RequestParam Long memberId, @RequestParam String searchText, @RequestParam Long listIndex) {
        log.info("getSearchTrack");
        TrackSearchDTO trackSearchDTO = TrackSearchDTO.builder()
                .memberId(memberId)
                .searchText(searchText)
                .build();

        searchService.setSearchHistory(trackSearchDTO.getMemberId(),trackSearchDTO.getSearchText());

        return searchService.searchTrack(trackSearchDTO,listIndex);
    }

    @GetMapping("/api/getSearchInit")
    public Map<String,Object> getSearchInit(@RequestParam Long memberId){
        log.info("getSearchInit");
        return searchService.getSearchInit(memberId);
    }


    @GetMapping("/api/getSearchMore")
    public Map<String,Object> getsearchMore(@RequestParam Long memberId, @RequestParam Long moreId,@RequestParam String searchText,@RequestParam Long listIndex){
        log.info("getSearchMore");
        return searchService.getSearchMore(memberId,moreId,searchText,listIndex);
    }


}
