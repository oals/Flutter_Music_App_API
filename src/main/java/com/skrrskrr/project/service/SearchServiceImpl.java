package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.queryBuilder.select.HistorySelectQueryBuilder;
import com.skrrskrr.project.queryBuilder.select.MemberSelectQueryBuilder;
import com.skrrskrr.project.repository.HistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class SearchServiceImpl implements SearchService{

    private final JPAQueryFactory jpaQueryFactory;
    private final HistoryRepository historyRepository;

    @Override
    public void setSearchHistory(SearchRequestDto searchRequestDto) {

        HistorySelectQueryBuilder historySelectQueryBuilder = new HistorySelectQueryBuilder(jpaQueryFactory);

        List<History> historyList = historySelectQueryBuilder.selectFrom(QHistory.history)
                .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                .fetch(History.class);

        Boolean isNewHistory = saveNewSearchHistory(searchRequestDto, historyList );

        if (isNewHistory) {
            deleteLastSearchHistory(searchRequestDto, historyList);
        }

    }


    private Member getMemberEntity(Long memberId) {

        MemberSelectQueryBuilder memberSelectQueryBuilder = new MemberSelectQueryBuilder(jpaQueryFactory);

        return (Member) memberSelectQueryBuilder.selectFrom(QMember.member)
                .findMemberByMemberId(memberId)
                .fetchOne(Member.class);

    }

    private Boolean saveNewSearchHistory(SearchRequestDto searchRequestDto, List<History> historyList){

        Member member = getMemberEntity(searchRequestDto.getLoginMemberId());

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

        if (!historyList.isEmpty()) {
            if (historyList.size() >= 15) {
                List<Long> idsToDeleteList = historySelectQueryBuilder
                        .selectFrom(QHistory.history)
                        .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                        .orderByHistoryIdAsc()
                        .limit(1)
                        .fetchHistoryIdList();

                // 삭제 쿼리 실행
                jpaQueryFactory.delete(qHistory)
                        .where(qHistory.historyId.in(idsToDeleteList))
                        .execute();
            }
        }
    }

    @Override
    public SearchResponseDto getSearchTextHistory(SearchRequestDto searchRequestDto) {

        List<HistoryDto> searchHistoryDtoList = getSearchHistory(searchRequestDto);

        return SearchResponseDto.builder()
                .searchHistoryList(searchHistoryDtoList)
                .build();
    }

    @Override
    public List<String> processSearchKeywords(SearchRequestDto searchRequestDto) {
        List<String> keywordList = new ArrayList<>();

        List<HistoryDto> searchHistoryDtoList = getSearchHistory(searchRequestDto);

        for (HistoryDto historyDto : searchHistoryDtoList) {

            String keyword = historyDto.getHistoryText();

            String cleanKeyword = keyword.replaceAll("[^a-zA-Z0-9 가-힣]", " ");
            String[] splitKeyword = cleanKeyword.split(" "); // 공백 기준으로 분리

            if (splitKeyword.length == 1) {
                keywordList.add(keyword);
            } else {

                String firstText = splitKeyword[0];
                String lastText = splitKeyword[splitKeyword.length - 1];

                keywordList.add(firstText);
                keywordList.add(lastText);

                for (int i = 0; i < splitKeyword.length; i++) {
                    if (splitKeyword[i].equalsIgnoreCase("feat") && i + 1 < splitKeyword.length) {
                        keywordList.add(splitKeyword[i + 1]); // feat 다음의 문자 가져오기
                        break;
                    }
                }
            }
        }

        Set<String> set = new LinkedHashSet<>(keywordList);
        String[] uniqueKeywordArray = set.toArray(new String[0]);
        return Arrays.stream(uniqueKeywordArray).toList();
    }


    private List<HistoryDto> getSearchHistory(SearchRequestDto searchRequestDto){

        HistorySelectQueryBuilder historySelectQueryBuilder = new HistorySelectQueryBuilder(jpaQueryFactory);

        return historySelectQueryBuilder.selectFrom(QHistory.history)
                .findHistoryByMemberId(searchRequestDto.getLoginMemberId())
                .orderByHistoryIdDesc()
                .fetchHistoryListDto(HistoryDto.class);

    }

}
