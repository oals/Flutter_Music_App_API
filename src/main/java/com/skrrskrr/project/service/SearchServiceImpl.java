package com.skrrskrr.project.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.*;
import com.skrrskrr.project.repository.HistoryRepository;
import com.skrrskrr.project.repository.MemberTrackRepository;
import com.skrrskrr.project.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.jdbc.Expectation;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    private final ModelMapper modelMapper;


    @Override
    public Map<String,Object> searchTrack(TrackSearchDTO trackSearchDTO, Long listIndex) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            Long memberListCnt = getSearchMemberListCnt(trackSearchDTO.getMemberId(),trackSearchDTO.getSearchText());
            /* 검색된 멤버 정보*/
            List<FollowDTO> memberList = new ArrayList<>();
            if (memberListCnt != 0L) {
                memberList = getSearchMemberList(trackSearchDTO.getMemberId(),trackSearchDTO.getSearchText(),listIndex,8L);
            }

            /* 검색된 앨범, 플레이리스트 수 */
            Long playListCnt = getSearchPlayListCnt(trackSearchDTO.getMemberId(),trackSearchDTO.getSearchText());

            List<PlayListDTO> playListDTOList = new ArrayList<>();
            if (playListCnt != 0L) {
                /* 검색된 앨범, 플레이리스트 정보 */
                playListDTOList = getSearchPlayList(trackSearchDTO.getMemberId(),trackSearchDTO.getSearchText(),listIndex,8L);
            }

            /* 검색된 트랙 수 */
            Long totalCount = getSearchTrackListCnt(trackSearchDTO.getMemberId(), trackSearchDTO.getSearchText());

            List<TrackSearchDTO> searchTrackList = new ArrayList<>();
            /* 검색된 트랙 정보 */
            if (totalCount != 0L) {
                searchTrackList = getSearchTrackList(trackSearchDTO.getMemberId(), trackSearchDTO.getSearchText(),listIndex,8L);
            }


            return createResultSearchMap(memberList,memberListCnt,searchTrackList,totalCount,playListDTOList,playListCnt);

        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private Map<String, Object> createResultSearchMap(
            List<FollowDTO> memberList, Long memberListCnt,
            List<TrackSearchDTO> searchTrackList, Long totalCount,
            List<PlayListDTO> playListDTOList, Long playListCnt) {

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalCount", totalCount);   // 트랙의 전체 개수
        resultMap.put("memberList", memberList);   // 검색된 멤버 리스트
        resultMap.put("memberListCnt", memberListCnt);   // 전체 멤버 수
        resultMap.put("trackList", searchTrackList);   // 검색된 트랙 리스트
        resultMap.put("playListList", playListDTOList);   // 검색된 플레이리스트 리스트
        resultMap.put("playListListCnt", playListCnt);   // 전체 플레이리스트 수
        resultMap.put("status", "200");
        return resultMap;
    }


    @Override
    public void setSearchHistory(Long memberId, String searchText) {

        saveNewSearchHistory(memberId, searchText);

        deleteLastSearchHistory(memberId);

    }


    private void saveNewSearchHistory(Long memberId, String searchText){
        
        QMember qMember = QMember.member;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(memberId))
                .fetchFirst();

        History history = History.builder()
                .member(member)
                .historyText(searchText)
                .historyDate(LocalDate.now())
                .build();

        historyRepository.save(history);
    }

    private void deleteLastSearchHistory(Long memberId) {

        
        QHistory qHistory = QHistory.history;

        /// 해당 멤버의 검색 히스토리 갯수 조회 후 특정 갯수 이상일때 마지막 히스토리 엔티티 삭제
        List<History> queryResult = jpaQueryFactory.selectFrom(qHistory)
                .where(qHistory.member.memberId.eq(memberId))
                .fetch();
        /// 화면에는 30개만 보여주고 60개가 쌓였을 때 30개를 삭제함

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
    public Map<String, Object> getSearchInit(Long memberId) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            List<HistoryDTO> searchHistoryDtoList = getSearchHistory(memberId);

            ///인기 검색어 조회 (7일 기준)
            List<String> popularTrackList = getPopularSearchHistory();

            hashMap.put("searchHistory",searchHistoryDtoList);
            hashMap.put("popularTrackHistory",popularTrackList);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }


    private List<HistoryDTO> getSearchHistory(Long memberId){
        
        QHistory qHistory = QHistory.history;

        /// 검색어 30개 조회
        return jpaQueryFactory.select(
                        Projections.bean(
                                HistoryDTO.class,
                                qHistory.historyId.as("historyId"),
                                qHistory.historyText.as("historyText"),
                                qHistory.historyDate.as("historyDate")
                        )
                )
                .from(qHistory)
                .where(qHistory.member.memberId.eq(memberId))
                .orderBy(qHistory.historyId.desc())
                .limit(30)
                .fetch();

    }

    private List<String> getPopularSearchHistory(){
        
        QTrack qTrack = QTrack.track;

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        return jpaQueryFactory.select(qTrack.trackNm)
                .from(qTrack)
                .where(qTrack.trackUploadDate.goe(startOfWeek.toString())
                        .and(qTrack.trackUploadDate.loe(endOfWeek.toString())))
                .orderBy(qTrack.trackPlayCnt.desc()) // 조회수 기준 내림차순
                .limit(8)
                .fetch();
    }


    @Override
    public Map<String, Object> getSearchMore(Long memberId, Long moreId, String searchText,Long listIndex) {

        Map<String,Object> hashMap = new HashMap<>();

        try {

            if(moreId == 1) {
                List<FollowDTO> searchMemberDtos = getSearchMemberList(memberId,searchText,listIndex,20L);
                hashMap.put("memberList",searchMemberDtos);
            } else if (moreId == 2) {

                List<PlayListDTO> playListDtoList = getSearchPlayList(memberId,searchText,listIndex,20L);
                hashMap.put("playListList",playListDtoList);

            } else if(moreId == 3) {

                List<TrackSearchDTO> trackSearchDtos = getSearchTrackList(memberId,searchText,listIndex,20L);
                hashMap.put("trackList",trackSearchDtos);
            }
            hashMap.put("status","200");
        } catch (Exception e) {
            hashMap.put("status","500");
        }

        return hashMap;
    }



    private List<TrackSearchDTO> getSearchTrackList(Long memberId, String searchText, Long listIndex,Long limit) {

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QTrackCategory qTrackCategory = QTrackCategory.trackCategory;
        QTrackLike qTrackLike = QTrackLike.trackLike;

        Member member = Member.builder()
                .memberId(memberId)
                .build();

        List<TrackSearchDTO> trackSearchDtoList = jpaQueryFactory.select(
                        Projections.bean(
                                TrackSearchDTO.class,
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
                .where(qMemberTrack.track.trackNm.contains(searchText)
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
                .offset(listIndex)
                .limit(limit)
                .orderBy(qMemberTrack.memberTrackId.desc())
                .fetch();

        for (TrackSearchDTO trackSearchDto : trackSearchDtoList) {
            /* 해당 곡의 좋아요 수 추가 */
            Long trackLikeCnt = getTrackLikeCnt(trackSearchDto.getTrackId());
            trackSearchDto.setTrackLikeCnt(trackLikeCnt);
        }


        return trackSearchDtoList;
    }



    private Long getSearchTrackListCnt(Long memberId, String searchText) {
        
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory
                .select(
                        qMemberTrack.memberTrackId.count() // count()로 카운트
                )
                .from(qMemberTrack)
                .where(qMemberTrack.track.trackNm.contains(searchText)
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



    private List<PlayListDTO> getSearchPlayList(Long memberId, String searchText, Long listIndex, Long limit) {
        
        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory.select(
                    Projections.bean(
                            PlayListDTO.class,
                            qPlayList.playListId.as("playListId"),
                            qPlayList.playListNm.as("playListNm"),
                            qPlayList.playListTrackList.get(0).track.trackImagePath.as("playListImagePath"),
                            qPlayList.member.memberNickName.as("memberNickName"),
                            qPlayList.member.memberId.as("memberId")

                    )
                ).from(qPlayList)
                .where(qPlayList.playListNm.contains(searchText)
                        .and(qPlayList.isPlayListPrivacy.isFalse())
                        .and(qPlayList.playListTrackList.isNotEmpty()))
                .offset(listIndex)
                .limit(limit)
                .fetch();
    }


    private Long getSearchPlayListCnt(Long memberId, String searchText) {

        QPlayList qPlayList = QPlayList.playList;

        return jpaQueryFactory.select(qPlayList.playListId.count()).from(qPlayList)
                .where(qPlayList.playListNm.contains(searchText)
                        .and(qPlayList.isPlayListPrivacy.isFalse())
                        .and(qPlayList.playListTrackList.isNotEmpty()))
                .fetchOne();
    }


    private Long getSearchMemberListCnt(Long memberId, String searchMemberNickName) {
        
        QMember qMember = QMember.member;

        return jpaQueryFactory.select(qMember.memberId.count())
                .from(qMember)
                .where(qMember.memberNickName.lower().contains(searchMemberNickName.toLowerCase())
                        .and(qMember.memberId.ne(memberId)))
                .fetchOne();

    }

    private List<FollowDTO> getSearchMemberList(Long memberId, String searchMemberNickName, Long listIndex, Long limit) {

        
        QMember qMember = QMember.member;

        List<Member> queryMemberResult = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberNickName.lower().contains(searchMemberNickName.toLowerCase())
                        .and(qMember.memberId.ne(memberId)))
                .offset(listIndex)
                .limit(limit)
                .fetch();


        List<FollowDTO> searchMemberDtos = new ArrayList<>();

        for (Member member : queryMemberResult) {

            FollowDTO followDTO = modelMapper.map(member,FollowDTO.class);
            followDTO.setIsFollowedCd(0L);
            followDTO.setMutualFollow(false);


            if (!member.getFollowers().isEmpty()
                    || !member.getFollowing().isEmpty()) {

                if (!member.getFollowers().isEmpty()) {
                    for (Follow item : member.getFollowers()) {
                        if (item.getFollowing().getMemberId().equals(memberId)) {
                            followDTO.setIsFollowedCd(1L);   // 내가 팔로우
                        }
                    }
                }

                if (!member.getFollowing().isEmpty()) {
                    for (Follow item : member.getFollowing()) {
                        if (item.getFollower().getMemberId().equals(memberId)) {
                            if (followDTO.getIsFollowedCd() == 1L) {
                                followDTO.setIsFollowedCd(3L); // 맞팔
                                followDTO.setMutualFollow(true);
                            } else {
                                followDTO.setIsFollowedCd(2L); // 내팔로워
                            }

                        }
                    }
                }
            }
            searchMemberDtos.add(followDTO);
        }

        return searchMemberDtos;

    }
}
