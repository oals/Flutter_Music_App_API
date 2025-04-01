package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap; import java.util.Map;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {


    private final JPAQueryFactory jpaQueryFactory;
    private final TrackService trackService;
    private final TrackLikeService trackLikeService;
    private final MemberService memberService;
    private final PlayListService playListService;


    @Override
    public Map<String, Object> firstLoad(MemberRequestDto memberRequestDto) {
        
        Map<String, Object> hashMap = new HashMap<>();

        try {
            //내 멤버 엔티티
            TrackRequestDto trackRequestDto = new TrackRequestDto();
            PlayListRequestDto playListRequestDto = new PlayListRequestDto();
            playListRequestDto.setLimit(6L);
            trackRequestDto.setLimit(4L);
            memberRequestDto.setLimit(4L);
            trackRequestDto.setOffset(0L);
            trackRequestDto.setLoginMemberId(memberRequestDto.getLoginMemberId());
            playListRequestDto.setLoginMemberId(memberRequestDto.getLoginMemberId());

            // 최근 감상 음악 리스트
            List<TrackDto> lastListenTrackList =  trackService.getLastListenTrackList(trackRequestDto);

            /// 인기 플리 추천  - 카테고리에 해당하는 곡의 수 , 조회수, 좋아요 수 ,
            List<PlayListDto> popularPlayList = playListService.getPopularPlayLists(playListRequestDto);

            /// 인기 유저 추천 -  곡 한개 이상 업로드 ~ / 선택된 카테고리 (카테고리는 폰에 캐시로 저장 )
            List<MemberDto> randomMemberList = memberService.getRandomMemberList(memberRequestDto);

            /// 내가 팔로우 한 유저의 곡 , 최신날짜 ,
            List<TrackDto> followMemberTrackList = trackService.getFollowMemberTrackList(trackRequestDto);

            Map<String, Object> result = trackLikeService.getLikeTrackList(trackRequestDto);

            // 트랜드 음악 조회
            List<TrackDto> trendingTrackList = trackService.getTrendingTrackList(trackRequestDto);


            hashMap.put("lastListenTrackList",lastListenTrackList);
            hashMap.put("popularPlayList",popularPlayList);
            hashMap.put("followMemberTrackList",followMemberTrackList);
            hashMap.put("likedTrackList",result.get("likeTrackList"));
            hashMap.put("randomMemberList", randomMemberList);
            hashMap.put("trendingTrackList", trendingTrackList);

            hashMap.put("status","200");
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("status","500");
        }


        return hashMap;
    }







}
