package com.skrrskrr.project.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.MemberDto;
import com.skrrskrr.project.dto.MemberRequestDto;
import com.skrrskrr.project.dto.PlayListDto;
import com.skrrskrr.project.dto.TrackDto;
import com.skrrskrr.project.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap; import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MainServiceImpl implements MainService {


    private final JPAQueryFactory jpaQueryFactory;
    private final ModelMapper modelMapper;
    
    @Override
    public Map<String, Object> firstLoad(MemberRequestDto memberRequestDto) {

        
        Map<String, Object> hashMap = new HashMap<>();
        QMember qMember = QMember.member;


        try {
            //내 멤버 엔티티
            Member myMember = jpaQueryFactory.selectFrom(qMember)
                    .where(qMember.memberId.eq(memberRequestDto.getLoginMemberId()))
                    .fetchFirst();

            // fcm 메세지에 추가하는걸로 변경?
            boolean notificationIsView = isNotificationView(memberRequestDto.getLoginMemberId());



            /// 인기 앨범 추천  - 카테고리에 해당하는 곡의 수 , 조회수, 좋아요 수 ,
            List<PlayListDto> popularPlayList = getPopularPlayList();


            /// 내가 팔로우 한 유저의 곡 , 최신날짜 ,
            List<TrackDto> followMemberTrackList = getFollowMemberTrackList(myMember);


            /// 관심 트랙 - 관심트랙 리스트에서 랜덤 , 선택된 카테고리 ,
            List<TrackDto> likedTrackList = getLikeTrackList(myMember);



            /// 인기 유저 추천 -  곡 한개 이상 업로드 ~ / 선택된 카테고리 (카테고리는 폰에 캐시로 저장 )
            /// 팔로우 여부 필요
            List<MemberDto> randomMemberList = getRandomMemberList(myMember);


            // 트랜드 음악 조회
            List<TrackDto> trendingTrackList = getTrendingTrackList();


            hashMap.put("notificationIsView",notificationIsView);
            hashMap.put("popularPlayList",popularPlayList);
            hashMap.put("followMemberTrackList",followMemberTrackList);
            hashMap.put("likedTrackList",likedTrackList);
            hashMap.put("randomMemberList", randomMemberList);
            hashMap.put("trendingTrackList", trendingTrackList);

            hashMap.put("status","200");
        } catch (Exception e) {
            hashMap.put("status","500");
        }


        return hashMap;
    }


    private List<TrackDto> getTrendingTrackList(){

        /// 트렌딩 음원 추천
        List<MemberTrack> queryResultTrackPlayDesc = getTrendingTrackPlayDesc();

        List<MemberTrack> queryResultTrackLikeDesc = getTrendingTrackLikeDesc();

        List<MemberTrack> combinedResult = mergeDescListTrack(queryResultTrackPlayDesc,queryResultTrackLikeDesc);

        List<TrackDto> trendingTrackList = new ArrayList<>();
        for (MemberTrack memberTrack : combinedResult) {
            TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
            trendingTrackList.add(trackDto);
        }

        return trendingTrackList;
    }


    private List<MemberTrack> mergeDescListTrack(List<MemberTrack> queryResultTrackPlayDesc, List<MemberTrack> queryResultTrackLikeDesc){

        List<MemberTrack> combinedResult = new ArrayList<>();

        combinedResult.addAll(queryResultTrackPlayDesc);
        combinedResult.addAll(queryResultTrackLikeDesc);

        // 중복 제거 (필요한 경우)
        return combinedResult.stream()
                .distinct()
                .collect(Collectors.toList());
    }


    private List<MemberTrack> getTrendingTrackPlayDesc(){

        
        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.selectFrom(qMemberTrack)
                .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                ).orderBy(qMemberTrack.track.trackPlayCnt.desc())
                .limit(5)
                .fetch();


    }


    private List<MemberTrack> getTrendingTrackLikeDesc(){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        return jpaQueryFactory.selectFrom(qMemberTrack)
                .where(isUploadedThisWeekOrLastWeek(qMemberTrack.track.trackUploadDate)
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse())
                ).orderBy(qMemberTrack.track.trackLikeCnt.desc())
                .limit(5)
                .fetch();
    }


    private List<MemberDto> getRandomMemberList(Member member){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;

        List<MemberTrack> queryResultMember = jpaQueryFactory.selectFrom(qMemberTrack)
                .where(
                        qMemberTrack.member.memberId.ne(member.getMemberId()) // 자기 자신 제외
                                .and(qMemberTrack.member.memberTrackList.isNotEmpty())  // 트랙 리스트가 비어 있지 않음
                )
                .groupBy(qMemberTrack.member.memberId) // 멤버 아이디별로 그룹화
                .orderBy(
                        Expressions.numberTemplate(Double.class, "function('RAND')").asc()  // 랜덤 정렬
                )
                .limit(8)  // 랜덤으로 8개만 추출
                .fetch();



        return createRandomMmeberDtoList(queryResultMember,member);
    }


    private List<MemberDto> createRandomMmeberDtoList(List<MemberTrack> queryResultMember, Member member){

        List<MemberDto> randomMemberList = new ArrayList<>();

        for (MemberTrack randomItem : queryResultMember) {

            int isFollowedCd = 0; // 관계없음

            for (int i = 0; i < randomItem.getMember().getFollowers().size(); i++) {
                if (randomItem.getMember().getFollowers().get(i).getFollowing().getMemberId().equals(member.getMemberId())) {
                    isFollowedCd = 1; // 내가 팔로우 중
                    break;
                }
            }

            for (int i = 0; i < randomItem.getMember().getFollowing().size(); i++) {
                if (randomItem.getMember().getFollowing().get(i).getFollower().getMemberId().equals(member.getMemberId())) {
                    if(isFollowedCd == 1){
                        isFollowedCd = 3; //맞팔
                    } else {
                        isFollowedCd = 2; //내 팔로워
                    }
                }
            }

            MemberDto memberDto = modelMapper.map(randomItem.getMember(), MemberDto.class);
            memberDto.setIsFollowedCd(isFollowedCd);


            randomMemberList.add(memberDto);
        }
        return randomMemberList;
    }

    private List<TrackDto> getLikeTrackList(Member member) {

        
        QTrackLike qTrackLike = QTrackLike.trackLike;

        List<TrackLike> queryResultLikedTrack = jpaQueryFactory.selectFrom(qTrackLike)
                .where(qTrackLike.trackLikeStatus.isTrue()
                        .and(qTrackLike.memberTrack.track.isTrackPrivacy.isFalse())
                        .and(qTrackLike.member.eq(member)))
                .groupBy(qTrackLike.memberTrack.track.trackId) // 멤버 아이디별로 그룹화
                .orderBy(
                        Expressions.numberTemplate(Double.class, "function('RAND')").asc()  // 랜덤 정렬
                ).limit(4)
                .fetch();

        List<TrackDto> likedTrackList = new ArrayList<>();
        for (TrackLike trackLike : queryResultLikedTrack) {
            TrackDto trackDto = modelMapper.map(trackLike.getMemberTrack().getTrack(), TrackDto.class);
            trackDto.setMemberNickName(trackLike.getMemberTrack().getMember().getMemberNickName());
            trackDto.setMemberId(trackLike.getMemberTrack().getMember().getMemberId());

            likedTrackList.add(trackDto);
        }
        return likedTrackList;

    }

    private List<TrackDto> getFollowMemberTrackList(Member myMember){

        QMemberTrack qMemberTrack = QMemberTrack.memberTrack;
        QMember qMember = QMember.member;
        QFollow qFollow = QFollow.follow;

        List<MemberTrack> queryResultFollowMemberTrack = jpaQueryFactory
                .selectFrom(qMemberTrack)
                .join(qMemberTrack.member, qMember) // MemberTrack과 Member를 조인
                .join(qMember.followers, qFollow)   // Member와 Follow를 조인 (팔로우 관계)
                .where(qFollow.following.eq(myMember)
                        .and(qMemberTrack.track.isTrackPrivacy.isFalse()))  // 내가 팔로우한 유저들
                .orderBy(qMemberTrack.track.trackUploadDate.desc()) // 최신 날짜 순으로 정렬
                .limit(4)
                .fetch();

        List<TrackDto> followMemberTrackList = new ArrayList<>();
        for (MemberTrack memberTrack : queryResultFollowMemberTrack) {
            TrackDto trackDto = modelMapper.map(memberTrack.getTrack(), TrackDto.class);
            trackDto.setMemberNickName(memberTrack.getMember().getMemberNickName());
            trackDto.setMemberId(memberTrack.getMember().getMemberId());

            followMemberTrackList.add(trackDto);
        }
        return followMemberTrackList;

    }


    private List<PlayListDto> getPopularPlayList() {

        
        QPlayList qPlayList = QPlayList.playList;
        QTrack qTrack = QTrack.track;


        List<PlayList> queryResultPlayList =  jpaQueryFactory.selectFrom(qPlayList)
                .join(qPlayList.playListTrackList, qTrack)
                .groupBy(qPlayList.playListId)  // PlayList 단위로 그룹화
                .orderBy(
                        qTrack.trackPlayCnt.count().desc(),      // trackPlayCnt의 등장 횟수
                        qTrack.trackLikeCnt.count().desc()
                )
                .where(qPlayList.playListTrackList.isNotEmpty()
                        .and(qPlayList.isPlayListPrivacy.isFalse()))  // Track 리스트가 비어 있지 않은 PlayList만
                .limit(4)  // 상위 8개만 조회
                .fetch();


        List<PlayListDto> popularPlayList = new ArrayList<>();
        for (PlayList playList : queryResultPlayList) {
            PlayListDto playListDto = PlayListDto.builder()
                    .playListId(playList.getPlayListId())
                    .playListNm(playList.getPlayListNm())
                    .playListImagePath(playList.getPlayListTrackList().get(0).getTrackImagePath())
                    .memberNickName(playList.getMember().getMemberNickName())
                    .build();

            popularPlayList.add(playListDto);
        }
        return popularPlayList;
    }


    private boolean isNotificationView(Long memberId) {

        
        QNotifications qNotifications = QNotifications.notifications;

        return Boolean.FALSE.equals(
                jpaQueryFactory.select(
                                qNotifications.notificationIsView
                        ).from(qNotifications)
                        .where(qNotifications.member.memberId.eq(memberId)
                                .and(qNotifications.notificationIsView.isFalse()))
                        .fetchFirst()
        );

    }



    private BooleanExpression isUploadedThisWeekOrLastWeek(StringExpression trackUploadDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 현재 날짜 기준으로 이번 주와 저번 주의 날짜 범위를 계산합니다.
        LocalDate today = LocalDate.now();

        // 이번 주 시작일과 종료일
        LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfThisWeek = startOfThisWeek.plusDays(6);  // 일요일까지

        // 저번 주 시작일과 종료일
        LocalDate startOfLastWeek = startOfThisWeek.minusWeeks(1);
        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);

        // 날짜를 문자열로 변환
        String startOfThisWeekStr = startOfThisWeek.format(formatter);
        String endOfThisWeekStr = endOfThisWeek.format(formatter);

        String startOfLastWeekStr = startOfLastWeek.format(formatter);
        String endOfLastWeekStr = endOfLastWeek.format(formatter);

        // 이번 주 또는 저번 주 업로드 날짜 필터
        BooleanExpression uploadThisWeek = trackUploadDate.between(startOfThisWeekStr, endOfThisWeekStr);
        BooleanExpression uploadLastWeek = trackUploadDate.between(startOfLastWeekStr, endOfLastWeekStr);

        return uploadThisWeek.or(uploadLastWeek);
    }

}
