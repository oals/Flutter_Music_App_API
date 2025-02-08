package com.skrrskrr.project.service;

import com.querydsl.core.Tuple;
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
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class SearchServiceImpl implements SearchService{

    @PersistenceContext
    EntityManager em;

    private final HistoryRepository historyRepository;


    @Override
    public HashMap<String,Object> searchTrack(TrackSearchDTO trackSearchDTO, Long listIndex) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        HashMap<String,Object> hashMap = new HashMap<>();
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QTrackCategory qTrackCategory = QTrackCategory.trackCategory;
        QMember qMember = QMember.member;
        QTrackLike qTrackLike = QTrackLike.trackLike;
        QPlayList qPlayList = QPlayList.playList;


        try {
            Member member = Member.builder()
                    .memberId(trackSearchDTO.getMemberId())
                    .build();

            List<Member> queryMemberResult = jpaQueryFactory.selectFrom(qMember)
                    .where(qMember.memberNickName.lower().contains(trackSearchDTO.getSearchText().toLowerCase())
                            .and(qMember.memberId.ne(member.getMemberId())))
                    .limit(4)
                    .fetch();

            Long queryMemberResultCnt = jpaQueryFactory.select(qMember.memberId.count())
                    .from(qMember)
                    .where(qMember.memberNickName.lower().contains(trackSearchDTO.getSearchText().toLowerCase())
                            .and(qMember.memberId.ne(member.getMemberId())))
                    .fetchOne();


            List<FollowDTO> searchMemberDTOs = new ArrayList<>();

            for (int i = 0; i < queryMemberResult.size(); i++) {

                FollowDTO followDTO = FollowDTO.builder()
                        .followNickName(queryMemberResult.get(i).getMemberNickName())
                        .followMemberId(queryMemberResult.get(i).getMemberId())
                        .followImagePath(queryMemberResult.get(i).getMemberImagePath())
                        .isFollowedCd(0L)
                        .isMutualFollow(false)
                        .build();

                if (!queryMemberResult.get(i).getFollowers().isEmpty()
                        || !queryMemberResult.get(i).getFollowing().isEmpty()) {

                    if(!queryMemberResult.get(i).getFollowers().isEmpty()){
                        for(Follow item : queryMemberResult.get(i).getFollowers()){
                            if(item.getFollowing().getMemberId().equals(member.getMemberId())){
                                followDTO.setIsFollowedCd(1L);   // 내가 팔로우
                            }
                        }
                    }

                    if(!queryMemberResult.get(i).getFollowing().isEmpty()) {
                        for(Follow item : queryMemberResult.get(i).getFollowing()){
                            if(item.getFollower().getMemberId().equals(member.getMemberId())){
                                if(followDTO.getIsFollowedCd() == 1L) {
                                    followDTO.setIsFollowedCd(3L); // 맞팔
                                    followDTO.setMutualFollow(true);
                                } else {
                                    followDTO.setIsFollowedCd(2L); // 내팔로워
                                }

                            }
                        }
                    }
                }
                searchMemberDTOs.add(followDTO);
            }



            hashMap.put("memberList",searchMemberDTOs);
            hashMap.put("memberListCnt",queryMemberResultCnt);



            List<PlayList> queryPlayListResult = jpaQueryFactory.selectFrom(qPlayList)
                    .where(qPlayList.playListNm.contains(trackSearchDTO.getSearchText())
                            .and(qPlayList.isPlayListPrivacy.isFalse())
                            .and(qPlayList.playListTrackList.isNotEmpty()))
                    .limit(8)
                    .fetch();

            Long queryPlatListResultCnt = jpaQueryFactory.select(qPlayList.playListId.count()).from(qPlayList)
                    .where(qPlayList.playListNm.contains(trackSearchDTO.getSearchText())
                            .and(qPlayList.isPlayListPrivacy.isFalse())
                            .and(qPlayList.playListTrackList.isNotEmpty()))
                    .fetchOne();




            List<PlayListDTO>  playListDTOList = new ArrayList<>();

            for(int i = 0; i < queryPlayListResult.size(); i++) {
                PlayListDTO playListDTO = PlayListDTO.builder()
                        .playListId(queryPlayListResult.get(i).getPlayListId())
                        .playListNm(queryPlayListResult.get(i).getPlayListNm())
                        .playListImagePath(queryPlayListResult.get(i).getPlayListTrackList().get(0).getTrackImagePath())
                        .memberNickName(queryPlayListResult.get(i).getMember().getMemberNickName())
                        .memberId(queryPlayListResult.get(i).getMember().getMemberId())
                        .build();

                playListDTOList.add(playListDTO);
            }

            hashMap.put("playListList",playListDTOList);
            hashMap.put("playListListCnt", queryPlatListResultCnt);

            List<Tuple> queryTrackResult = jpaQueryFactory.select(
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
                    .from(qMemberTrack)
                    .join(qTrackCategory)
                    .on(qTrackCategory.category.trackCategoryId.eq(qMemberTrack.track.trackCategoryId))
                    .leftJoin(qTrackLike)
                    .on(qTrackLike.member.eq(member)
                            .and(qTrackLike.memberTrack.eq(qMemberTrack)))
                    .where(qMemberTrack.track.trackNm.contains(trackSearchDTO.getSearchText())
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
                    .limit(20)
                    .orderBy(qMemberTrack.memberTrackId.desc())
                    .fetch();




            Long searchTrackCount = jpaQueryFactory
                    .select(
                            qMemberTrack.memberTrackId.count() // count()로 카운트
                    )
                    .from(qMemberTrack)
                    .where(qMemberTrack.track.trackNm.contains(trackSearchDTO.getSearchText())
                            .and(qMemberTrack.track.isTrackPrivacy.isFalse())) ///사용자 닉네임 or 조건 추가
                    .fetchFirst();



            if(queryTrackResult.isEmpty()){
                hashMap.put("totalCount",0);
                hashMap.put("status", "200");
            } else {
                hashMap.put("totalCount",searchTrackCount);
                hashMap.put("status", "200");
            }


            List<TrackSearchDTO> trackSearchDTOs = new ArrayList<>();

            for (Tuple tuple : queryTrackResult) {


                Long trackLikeCnt = jpaQueryFactory
                        .select(qTrackLike.count())
                        .from(qTrackLike)
                        .where(qTrackLike.memberTrack.track.trackId.eq(tuple.get(qMemberTrack.track.trackId))
                                .and(qTrackLike.trackLikeStatus.isTrue()))
                        .fetchOne();


                trackSearchDTOs.add(
                        TrackSearchDTO.builder()
                                .memberId(tuple.get(qMemberTrack.member.memberId))
                                .memberTrackId(tuple.get(qMemberTrack.memberTrackId))
                                .memberNickName(tuple.get(qMemberTrack.member.memberNickName))
                                .trackId(tuple.get(qMemberTrack.track.trackId))
                                .trackNm(tuple.get(qMemberTrack.track.trackNm))
                                .trackTime(tuple.get(qMemberTrack.track.trackTime))
                                .trackPlayCnt(tuple.get(qMemberTrack.track.trackPlayCnt))
                                .trackImagePath(tuple.get(qMemberTrack.track.trackImagePath))
                                .trackLikeCnt(trackLikeCnt)
                                .trackCategoryId(tuple.get(qTrackCategory.category.trackCategoryId))
                                .trackLikeStatus(Boolean.TRUE.equals(tuple.get(qTrackLike.trackLikeStatus)))
                                .build()
                );


            }


            hashMap.put("trackList",trackSearchDTOs);
            hashMap.put("status","200");
        } catch(Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }

    @Override
    public void setSearchHistory(Long memberId, String searchText) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember qMember = QMember.member;
        QHistory qHistory = QHistory.history;

        Member member = jpaQueryFactory.selectFrom(qMember)
                .where(qMember.memberId.eq(memberId))
                .fetchFirst();



        History history = History.builder()
                .member(member)
                .historyText(searchText)
                .historyDate(LocalDate.now())
                .build();


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



        historyRepository.save(history);

    }

    @Override
    public HashMap<String, Object> getSearchInit(Long memberId) {

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QHistory qHistory = QHistory.history;
        HashMap<String,Object> hashMap = new HashMap<>();

        QTrack qTrack = QTrack.track;

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));

        try {
            /// 검색어 30개 조회
            List<History> searchHistory = jpaQueryFactory.selectFrom(qHistory)
                    .where(qHistory.member.memberId.eq(memberId))
                    .orderBy(qHistory.historyId.desc())
                    .limit(30)
                    .fetch();

            List<HistoryDTO> searchHistoryDTOList = new ArrayList<>();
            for (History history : searchHistory) {
                HistoryDTO historyDTO = HistoryDTO.builder()
                        .historyId(history.getHistoryId())
                        .historyText(history.getHistoryText())
                        .historyDate(history.getHistoryDate().toString())
                        .build();

                searchHistoryDTOList.add(historyDTO);
            }




            ///인기 검색어 조회 (7일 기준)
            List<String> popularTrackList = jpaQueryFactory.select(qTrack.trackNm)
                    .from(qTrack)
                    .where(qTrack.trackUploadDate.goe(startOfWeek.toString())
                            .and(qTrack.trackUploadDate.loe(endOfWeek.toString())))
                    .orderBy(qTrack.trackPlayCnt.desc()) // 조회수 기준 내림차순
                    .limit(8)
                    .fetch();


//        List<String> popularSearchHistory = jpaQueryFactory
//                .select(qHistory.historyText)
//                .from(qHistory)
//                .where(qHistory.historyDate.between(startOfWeek, endOfWeek))
//                .groupBy(qHistory.historyText) // 검색어별 그룹화
//                .orderBy(Expressions.numberTemplate(Long.class, "count(*)").desc())
//                .limit(8)
//                .fetch();



            hashMap.put("searchHistory",searchHistoryDTOList);
            hashMap.put("popularTrackHistory",popularTrackList);
            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }

        return hashMap;
    }



    @Override
    public HashMap<String, Object> getSearchMore(Long memberId, Long moreId, String searchText,Long listIndex) {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);

        HashMap<String,Object> hashMap = new HashMap<>();

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QTrackCategory qTrackCategory = QTrackCategory.trackCategory;
        QMember qMember = QMember.member;
        QTrackLike qTrackLike = QTrackLike.trackLike;
        QPlayList qPlayList = QPlayList.playList;

        try {
            Member member = Member.builder()
                    .memberId(memberId)
                    .build();

            if(moreId == 1) {

                List<Member> queryMemberResult = jpaQueryFactory.selectFrom(qMember)
                        .where(qMember.memberNickName.lower().contains(searchText.toLowerCase())
                                .and(qMember.memberId.ne(member.getMemberId())))
                        .offset(listIndex)
                        .limit(20)
                        .fetch();


                List<FollowDTO> searchMemberDTOs = new ArrayList<>();

                for (int i = 0; i < queryMemberResult.size(); i++) {

                    FollowDTO followDTO = FollowDTO.builder()
                            .followNickName(queryMemberResult.get(i).getMemberNickName())
                            .followMemberId(queryMemberResult.get(i).getMemberId())
                            .followImagePath(queryMemberResult.get(i).getMemberImagePath())
                            .isFollowedCd(0L)
                            .isMutualFollow(false)
                            .build();

                    if (!queryMemberResult.get(i).getFollowers().isEmpty()
                            || !queryMemberResult.get(i).getFollowing().isEmpty()) {

                        if(!queryMemberResult.get(i).getFollowers().isEmpty()){
                            for(Follow item : queryMemberResult.get(i).getFollowers()){
                                if(item.getFollowing().getMemberId().equals(member.getMemberId())){
                                    followDTO.setIsFollowedCd(1L);   // 내가 팔로우
                                }
                            }
                        }

                        if(!queryMemberResult.get(i).getFollowing().isEmpty()) {
                            for(Follow item : queryMemberResult.get(i).getFollowing()){
                                if(item.getFollower().getMemberId().equals(member.getMemberId())){
                                    if(followDTO.getIsFollowedCd() == 1L) {
                                        followDTO.setIsFollowedCd(3L); // 맞팔
                                        followDTO.setMutualFollow(true);
                                    } else {
                                        followDTO.setIsFollowedCd(2L); // 내팔로워
                                    }

                                }
                            }
                        }
                    }
                    searchMemberDTOs.add(followDTO);
                }

                hashMap.put("memberList",searchMemberDTOs);

            } else if (moreId == 2) {


                List<PlayList> queryPlayListResult = jpaQueryFactory.selectFrom(qPlayList)
                        .where(qPlayList.playListNm.contains(searchText)
                                .and(qPlayList.isPlayListPrivacy.isFalse())
                                .and(qPlayList.playListTrackList.isNotEmpty()))
                        .offset(listIndex)
                        .limit(20)
                        .fetch();



                List<PlayListDTO>  playListDTOList = new ArrayList<>();

                for(int i = 0; i < queryPlayListResult.size(); i++) {
                    PlayListDTO playListDTO = PlayListDTO.builder()
                            .playListId(queryPlayListResult.get(i).getPlayListId())
                            .playListNm(queryPlayListResult.get(i).getPlayListNm())
                            .playListImagePath(queryPlayListResult.get(i).getPlayListTrackList().get(0).getTrackImagePath())
                            .memberNickName(queryPlayListResult.get(i).getMember().getMemberNickName())
                            .memberId(queryPlayListResult.get(i).getMember().getMemberId())
                            .build();

                    playListDTOList.add(playListDTO);
                }

                hashMap.put("playListList",playListDTOList);


            } else if(moreId == 3) {


                List<Tuple> queryTrackResult = jpaQueryFactory.select(
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
                        .from(qMemberTrack)
                        .join(qTrackCategory)
                        .on(qTrackCategory.category.trackCategoryId.eq(qMemberTrack.track.trackCategoryId))
                        .leftJoin(qTrackLike)
                        .on(qTrackLike.member.eq(member)
                                .and(qTrackLike.memberTrack.eq(qMemberTrack)))
                        .where(qMemberTrack.track.trackNm.contains(searchText)
                                .and(qMemberTrack.track.isTrackPrivacy.isFalse())) ///사용자 닉네임 or 조건 추가
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
                        .limit(20)
                        .orderBy(qMemberTrack.memberTrackId.desc())
                        .fetch();


                List<TrackSearchDTO> trackSearchDTOs = new ArrayList<>();

                for (Tuple tuple : queryTrackResult) {


                    Long trackLikeCnt = jpaQueryFactory
                            .select(qTrackLike.count())
                            .from(qTrackLike)
                            .where(qTrackLike.memberTrack.track.trackId.eq(tuple.get(qMemberTrack.track.trackId))
                                    .and(qTrackLike.trackLikeStatus.isTrue()))
                            .fetchOne();


                    trackSearchDTOs.add(
                            TrackSearchDTO.builder()
                                    .memberId(tuple.get(qMemberTrack.member.memberId))
                                    .memberTrackId(tuple.get(qMemberTrack.memberTrackId))
                                    .memberNickName(tuple.get(qMemberTrack.member.memberNickName))
                                    .trackId(tuple.get(qMemberTrack.track.trackId))
                                    .trackNm(tuple.get(qMemberTrack.track.trackNm))
                                    .trackTime(tuple.get(qMemberTrack.track.trackTime))
                                    .trackPlayCnt(tuple.get(qMemberTrack.track.trackPlayCnt))
                                    .trackImagePath(tuple.get(qMemberTrack.track.trackImagePath))
                                    .trackLikeCnt(trackLikeCnt)
                                    .trackCategoryId(tuple.get(qTrackCategory.category.trackCategoryId))
                                    .trackLikeStatus(Boolean.TRUE.equals(tuple.get(qTrackLike.trackLikeStatus)))
                                    .build()
                    );
                }
                hashMap.put("trackList",trackSearchDTOs);
            }
            hashMap.put("status","200");
        } catch (Exception e) {
            hashMap.put("status","500");
        }

        return hashMap;
    }

}
