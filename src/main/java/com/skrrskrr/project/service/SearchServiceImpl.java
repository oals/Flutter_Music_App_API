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
    private final TrackService trackService;
    private final PlayListService playListService;
    private final MemberService memberService;

    @Override
    public Map<String,Object> search(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        List<FollowDto> memberList = new ArrayList<>();
        List<PlayListDto> playListDtoList = new ArrayList<>();
        List<SearchDto> searchTrackList = new ArrayList<>();

        try {

            Long memberListCnt = memberService.getSearchMemberListCnt(searchRequestDto);
            /* 검색된 멤버 정보*/

            if (memberListCnt != 0L) {
                searchRequestDto.setLimit(5L);
                memberList = memberService.getSearchMemberList(searchRequestDto);
            }

            /* 검색된 앨범, 플레이리스트 수 */
            Long playListCnt = playListService.getSearchPlayListCnt(searchRequestDto);

            if (playListCnt != 0L) {
                /* 검색된 앨범, 플레이리스트 정보 */
                searchRequestDto.setLimit(5L);
                playListDtoList = playListService.getSearchPlayList(searchRequestDto);
            }

            /* 검색된 트랙 수 */
            Long totalCount = trackService.getSearchTrackListCnt(searchRequestDto);

            /* 검색된 트랙 정보 */
            if (totalCount != 0L) {
                searchRequestDto.setLimit(8L);
                searchTrackList = trackService.getSearchTrackList(searchRequestDto);
            }

            return createResultSearchMap(memberList,memberListCnt,searchTrackList,totalCount,playListDtoList,playListCnt);

        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private Map<String, Object> createResultSearchMap(
            List<FollowDto> memberList, Long memberListCnt,
            List<SearchDto> searchTrackList, Long totalCount,
            List<PlayListDto> playListDtoList, Long playListCnt) {

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalCount", totalCount);   // 트랙의 전체 개수
        resultMap.put("memberList", memberList);   // 검색된 멤버 리스트
        resultMap.put("memberListCnt", memberListCnt);   // 전체 멤버 수
        resultMap.put("trackList", searchTrackList);   // 검색된 트랙 리스트
        resultMap.put("playListList", playListDtoList);   // 검색된 플레이리스트 리스트
        resultMap.put("playListListCnt", playListCnt);   // 전체 플레이리스트 수
        resultMap.put("status", "200");
        return resultMap;
    }


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

    @Override
    public Map<String, Object> getSearchMore(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();
        MemberRequestDto memberRequestDto = new MemberRequestDto();
        memberRequestDto.setMemberId(searchRequestDto.getMemberId());
        memberRequestDto.setLoginMemberId(searchRequestDto.getLoginMemberId());
        memberRequestDto.setLimit(searchRequestDto.getLimit());
        memberRequestDto.setOffset(searchRequestDto.getOffset());

        try {
            switch (searchRequestDto.getMoreId().toString()) {
                case "1":
                    List<FollowDto> searchMemberDtos = memberService.getSearchMemberList(searchRequestDto);
                    hashMap.put("memberList", searchMemberDtos);
                    break;
                case "2":
                    List<PlayListDto> searchPlayListDtos = playListService.getSearchPlayList(searchRequestDto);
                    hashMap.put("playListList", searchPlayListDtos);
                    break;
                case "3":
                    List<SearchDto> searchTrackListDtos = trackService.getSearchTrackList(searchRequestDto);
                    hashMap.put("trackList", searchTrackListDtos);
                    break;
                case "4":
                    List<PlayListDto> userPlayListDtos = playListService.getMemberPlayList(memberRequestDto);
                    hashMap.put("playListList", userPlayListDtos);
                    break;
                case "5":
                    List<TrackDto> userAllTrackDtos = trackService.getAllMemberTrackList(memberRequestDto);
                    hashMap.put("trackList", userAllTrackDtos);
                    break;
                default:
                    hashMap.put("status", "500");
                    return hashMap;
            }

            hashMap.put("status","200");
        } catch (Exception e) {
            hashMap.put("status","500");
        }

        return hashMap;
    }

}
