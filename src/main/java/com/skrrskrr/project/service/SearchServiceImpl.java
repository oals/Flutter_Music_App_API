package com.skrrskrr.project.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
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
    public Map<String,Object> search(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            Long memberListCnt = getSearchMemberListCnt(searchRequestDto);
            /* 검색된 멤버 정보*/
            List<FollowDto> memberList = new ArrayList<>();
            if (memberListCnt != 0L) {
                memberList = getSearchMemberList(searchRequestDto,5L);
            }

            /* 검색된 앨범, 플레이리스트 수 */
            Long playListCnt = getSearchPlayListCnt(searchRequestDto);

            List<PlayListDto> playListDtoList = new ArrayList<>();
            if (playListCnt != 0L) {
                /* 검색된 앨범, 플레이리스트 정보 */
                playListDtoList = getSearchPlayList(searchRequestDto,8L);
            }

            /* 검색된 트랙 수 */
            Long totalCount = getSearchTrackListCnt(searchRequestDto);

            List<SearchDto> searchTrackList = new ArrayList<>();
            /* 검색된 트랙 정보 */
            if (totalCount != 0L) {
                searchTrackList = getSearchTrackList(searchRequestDto,8L);
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

        saveNewSearchHistory(searchRequestDto);

        deleteLastSearchHistory(searchRequestDto);

    }


    private void saveNewSearchHistory(SearchRequestDto searchRequestDto){
        
        QMember qMember = QMember.member;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(searchRequestDto.getLoginMemberId()))
                .fetchFirst();

        History history = History.builder()
                .member(member)
                .historyText(searchRequestDto.getSearchText())
                .historyDate(LocalDate.now())
                .build();

        historyRepository.save(history);
    }

    private void deleteLastSearchHistory(SearchRequestDto searchRequestDto) {

        QHistory qHistory = QHistory.history;

        List<History> queryResult = jpaQueryFactory.selectFrom(qHistory)
                .where(qHistory.member.memberId.eq(searchRequestDto.getLoginMemberId()))
                .fetch();

        if(!queryResult.isEmpty()){
            if(queryResult.size() >= 60){
                List<Long> idsToDelete = jpaQueryFactory
                        .select(qHistory.member.memberId)
                        .from(qHistory)
                        .orderBy(qHistory.historyDate.asc())
                        .limit(queryResult.size() - 30)
                        .fetch(); // 결과를 리스트로 가져옴

                // 삭제 쿼리 실행
                jpaQueryFactory.delete(qHistory)
                        .where(qHistory.member.memberId.in(idsToDelete))
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
        
        QHistory qHistory = QHistory.history;

        /// 검색어 30개 조회
        return jpaQueryFactory.select(
                        Projections.bean(
                                HistoryDto.class,
                                qHistory.historyId.as("historyId"),
                                qHistory.historyText.as("historyText"),
                                qHistory.historyDate.as("historyDate")
                        )
                )
                .from(qHistory)
                .where(qHistory.member.memberId.eq(searchRequestDto.getLoginMemberId()))
                .orderBy(qHistory.historyId.desc())
                .limit(searchRequestDto.getLimit())
                .fetch();

    }

    @Override
    public Map<String, Object> getSearchMore(SearchRequestDto searchRequestDto) {

        Map<String,Object> hashMap = new HashMap<>();

        MemberRequestDto memberRequestDto = new MemberRequestDto();
        memberRequestDto.setMemberId(searchRequestDto.getMemberId());
        memberRequestDto.setLoginMemberId(searchRequestDto.getLoginMemberId());

        try {
            switch (searchRequestDto.getMoreId().toString()) {
                case "1":
                    List<FollowDto> searchMemberDtos = getSearchMemberList(searchRequestDto, searchRequestDto.getLimit());
                    hashMap.put("memberList", searchMemberDtos);
                    break;
                case "2":
                    List<PlayListDto> searchPlayListDtos = getSearchPlayList(searchRequestDto, searchRequestDto.getLimit());
                    hashMap.put("playListList", searchPlayListDtos);
                    break;
                case "3":
                    List<SearchDto> searchTrackListDtos = getSearchTrackList(searchRequestDto, searchRequestDto.getLimit());
                    hashMap.put("trackList", searchTrackListDtos);
                    break;
                case "4":
                    List<PlayListDto> userPlayListDtos = memberService.getMemberPlayList(memberRequestDto, searchRequestDto.getOffset(), searchRequestDto.getLimit());
                    hashMap.put("playListList", userPlayListDtos);
                    break;
                case "5":
                    List<TrackDto> userAllTrackDtos = memberService.getMemberTrack(memberRequestDto, false, searchRequestDto.getOffset(), searchRequestDto.getLimit());
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



    private List<SearchDto> getSearchTrackList(SearchRequestDto searchRequestDto, Long limit) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QTrackCategory qTrackCategory = QTrackCategory.trackCategory;
        QTrackLike qTrackLike = QTrackLike.trackLike;

        Member member = Member.builder()
                .memberId(searchRequestDto.getLoginMemberId())
                .build();

        List<SearchDto> searchDtoList = jpaQueryFactory.select(
                        Projections.bean(
                                SearchDto.class,
                                qMemberTrack.memberTrackId.as("memberTrackId"),  // 별칭 추가
                                qMemberTrack.member.memberId.as("memberId"),
                                qMemberTrack.member.memberNickName.as("memberNickName"),
                                qMemberTrack.track.trackId.as("trackId"),
                                qMemberTrack.track.trackNm.as("trackNm"),
                                qMemberTrack.track.trackTime.as("trackTime"),
                                qMemberTrack.track.trackPlayCnt.as("trackPlayCnt"),
                                qMemberTrack.track.trackImagePath.as("trackImagePath"),
                                qTrackCategory.category.trackCategoryId.as("trackCategoryId"),
                                qTrackLike.trackLikeStatus.as("trackLikeStatus")
                        )
                )
                .from(qMemberTrack)
                .join(qTrackCategory)
                .on(qTrackCategory.category.trackCategoryId.eq(qMemberTrack.track.trackCategoryId))
                .leftJoin(qTrackLike)
                .on(qTrackLike.member.eq(member)
                        .and(qTrackLike.memberTrack.eq(qMemberTrack)))
                .where(qMemberTrack.track.trackNm.contains(searchRequestDto.getSearchText())
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                )///사용자 닉네임 or 조건 추가
                .groupBy(
                        qMemberTrack.memberTrackId,
                        qMemberTrack.member.memberId,
                        qMemberTrack.member.memberNickName,
                        qMemberTrack.track.trackId,
                        qMemberTrack.track.trackNm,
                        qMemberTrack.track.trackTime,
                        qMemberTrack.track.trackPlayCnt,
                        qMemberTrack.track.trackImagePath,
                        qTrackCategory.category.trackCategoryId,
                        qTrackLike.trackLikeStatus
                )
                .offset(searchRequestDto.getOffset())
                .limit(limit)
                .orderBy(qMemberTrack.memberTrackId.desc())
                .fetch();


        for (SearchDto searchDto : searchDtoList) {
            /* 해당 곡의 좋아요 수 추가 */
            Long trackLikeCnt = getTrackLikeCnt(searchDto.getTrackId());
            searchDto.setTrackLikeCnt(trackLikeCnt);
        }


        return searchDtoList;
    }



    private Long getSearchTrackListCnt(SearchRequestDto searchRequestDto) {
        
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory
                .select(
                        qMemberTrack.memberTrackId.count() // count()로 카운트
                )
                .from(qMemberTrack)
                .where(qMemberTrack.track.trackNm.contains(searchRequestDto.getSearchText())
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())) ///사용자 닉네임 or 조건 추가
                .fetchFirst();
    }

    private Long getTrackLikeCnt(Long trackId) {
        
        QTrackLike qTrackLike = QTrackLike.trackLike;

        return jpaQueryFactory
                .select(qTrackLike.count())
                .from(qTrackLike)
                .where(qTrackLike.memberTrack.track.trackId.eq(trackId)
                        .and(qTrackLike.trackLikeStatus.isTrue()))
                .fetchOne();
    }



    private List<PlayListDto> getSearchPlayList(SearchRequestDto searchRequestDto, Long limit) {
        
        QPlayList qPlayList = QPlayList.playList;

        List<PlayList> playListList = jpaQueryFactory.selectFrom(qPlayList)
                .where(qPlayList.playListNm.contains(searchRequestDto.getSearchText())
                        .and(qPlayList.isPlayListPrivacy.isFalse())
                        .and(qPlayList.playListTrackList.isNotEmpty()))
                .offset(searchRequestDto.getOffset())
                .limit(limit)
                .fetch();


        List<PlayListDto> playListDtoList = new ArrayList<>();
        for (PlayList playList : playListList) {
            PlayListDto playListDto = PlayListDto.builder()
                    .playListId(playList.getPlayListId())
                    .playListNm(playList.getPlayListNm())
                    .playListImagePath(playList.getPlayListTrackList().get(0).getTrackImagePath())
                    .memberNickName(playList.getMember().getMemberNickName())
                    .build();

            playListDtoList.add(playListDto);
        }

        return playListDtoList;
    }


    private Long getSearchPlayListCnt(SearchRequestDto searchRequestDto) {

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory.select(qPlayList.playListId.count()).from(qPlayList)
                .where(qPlayList.playListNm.contains(searchRequestDto.getSearchText())
                        .and(qPlayList.isPlayListPrivacy.isFalse())
                        .and(qPlayList.playListTrackList.isNotEmpty()))
                .fetchOne();
    }


    private Long getSearchMemberListCnt(SearchRequestDto searchRequestDto) {
        
        QMember qMember = QMember.member;

        return jpaQueryFactory.select(qMember.memberId.count())
                .from(qMember)
                .where(qMember.memberNickName.lower().contains(searchRequestDto.getSearchText().toLowerCase())
                        .and(qMember.memberId.ne(searchRequestDto.getLoginMemberId())))
                .fetchOne();

    }

    private List<FollowDto> getSearchMemberList(SearchRequestDto searchRequestDto, Long limit) {

        QMember qMember = QMember.member;

        List<Member> queryMemberResult = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberNickName.lower().contains(searchRequestDto.getSearchText().toLowerCase())
                        .and(qMember.memberId.ne(searchRequestDto.getLoginMemberId())))
                .offset(searchRequestDto.getOffset())
                .limit(limit)
                .fetch();

        List<FollowDto> searchMemberDtos = new ArrayList<>();

        for (Member member : queryMemberResult) {

            FollowDto followDto = FollowDto.builder()
                    .isFollowedCd(0L)
                    .followImagePath(member.getMemberImagePath())
                    .followMemberId(member.getMemberId())
                    .followNickName(member.getMemberNickName())
                    .isMutualFollow(false)
                    .build();

            if (!member.getFollowers().isEmpty()
                    || !member.getFollowing().isEmpty()) {

                if (!member.getFollowers().isEmpty()) {
                    for (Follow item : member.getFollowers()) {
                        if (item.getFollowing().getMemberId().equals(searchRequestDto.getLoginMemberId())) {
                            followDto.setIsFollowedCd(1L);   // 내가 팔로우
                        }
                    }
                }

                if (!member.getFollowing().isEmpty()) {
                    for (Follow item : member.getFollowing()) {
                        if (item.getFollower().getMemberId().equals(searchRequestDto.getLoginMemberId())) {
                            if (followDto.getIsFollowedCd() == 1L) {
                                followDto.setIsFollowedCd(3L); // 맞팔
                                followDto.setMutualFollow(true);
                            } else {
                                followDto.setIsFollowedCd(2L); // 내팔로워
                            }

                        }
                    }
                }
            }
            searchMemberDtos.add(followDto);
        }

        return searchMemberDtos;

    }
}
