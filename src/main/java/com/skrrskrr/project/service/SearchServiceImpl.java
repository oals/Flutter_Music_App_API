package com.skrrskrr.project.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.HistorySelectQueryBuilder;
import com.skrrskrr.project.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class SearchServiceImpl implements SearchService{

    private final JPAQueryFactory jpaQueryFactory;
    private final HistoryRepository historyRepository;
    private final MemberService memberService;


    @Override
    public Map<String,Object> setSearchHistory(SearchRequestDto searchRequestDto) {

        Map<String, Object> hashMap = new HashMap<>();

        try {
            HistorySelectQueryBuilder historySelectQueryBuilder = new HistorySelectQueryBuilder(jpaQueryFactory);

            List<History> historyList = historySelectQueryBuilder.selectFrom(QHistory.history)
                    .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                    .fetch(History.class);

            Boolean isNewHistory = saveNewSearchHistory(searchRequestDto, historyList );

            if (isNewHistory) {
                deleteLastSearchHistory(searchRequestDto, historyList);
            }


        } catch(Exception e) {
            e.printStackTrace();;
            hashMap.put("status", "500");   // 트랙의 전체 개수
        }
        return hashMap;
    }


    private Boolean saveNewSearchHistory(SearchRequestDto searchRequestDto, List<History> historyList){

        Member member = memberService.getMemberEntity(searchRequestDto.getLoginMemberId());

        boolean isNewSearchHistory = true;
        for (History history : historyList) {
            if (searchRequestDto.getSearchText().equals(history.getHistoryText())) {
                isNewSearchHistory = false;
            }
        }

        if (isNewSearchHistory) {
            History history = History.builder()
                    .member(member)
                    .historyText(searchRequestDto.getSearchText())
                    .historyDate(LocalDate.now())
                    .build();

            historyRepository.save(history);
        }


        return isNewSearchHistory;
    }

    private void deleteLastSearchHistory(SearchRequestDto searchRequestDto, List<History> historyList) {

        HistorySelectQueryBuilder historySelectQueryBuilder = new HistorySelectQueryBuilder(jpaQueryFactory);
        QHistory qHistory = QHistory.history;

        if(!historyList.isEmpty()){
            if(historyList.size() >= 30){
                List<Long> idsToDeleteList = historySelectQueryBuilder
                        .selectFrom(QHistory.history)
                        .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                        .orderByHistoryIdAsc()
                        .limit(10)
                        .fetchHistoryIdList();

                // 삭제 쿼리 실행
                jpaQueryFactory.delete(qHistory)
                        .where(qHistory.historyId.in(idsToDeleteList))
                        .execute();
            }
        }
    }



    @Override
    public Map<String, Object> getSearchTextHistory(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {
            List<HistoryDto> searchHistoryDtoList = getSearchHistory(searchRequestDto);

            hashMap.put("status","200");
            hashMap.put("searchHistory",searchHistoryDtoList);

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private List<HistoryDto> getSearchHistory(SearchRequestDto searchRequestDto){

        HistorySelectQueryBuilder historySelectQueryBuilder = new HistorySelectQueryBuilder(jpaQueryFactory);

        return historySelectQueryBuilder.selectFrom(QHistory.history)
                .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                .orderByHistoryIdDesc()
                .fetchHistoryListDto(HistoryDto.class);

    }

}
