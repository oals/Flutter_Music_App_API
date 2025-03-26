package com.skrrskrr.project.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap; import java.util.Map;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {


    private final JPAQueryFactory jpaQueryFactory;
    private final TrackService trackService;
    private final MemberService memberService;
    private final PlayListService playListService;
    private final ModelMapper modelMapper;

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


            /// 인기 앨범 추천  - 카테고리에 해당하는 곡의 수 , 조회수, 좋아요 수 ,
            List<PlayListDto> popularPlayList = playListService.getPopularPlayList(playListRequestDto);

            /// 인기 유저 추천 -  곡 한개 이상 업로드 ~ / 선택된 카테고리 (카테고리는 폰에 캐시로 저장 )
            /// 팔로우 여부 필요
            List<MemberDto> randomMemberList = memberService.getRandomMemberList(memberRequestDto);

            /// 내가 팔로우 한 유저의 곡 , 최신날짜 ,
            List<TrackDto> followMemberTrackList = trackService.getFollowMemberTrackList(trackRequestDto);

            /// 관심 트랙 - 관심트랙 리스트에서 랜덤 , 선택된 카테고리 ,
            List<TrackDto> likedTrackList = trackService.getLikeTrackList(trackRequestDto);

            // 트랜드 음악 조회
            List<TrackDto> trendingTrackList = trackService.getTrendingTrackList(trackRequestDto);

            hashMap.put("popularPlayList",popularPlayList);
            hashMap.put("followMemberTrackList",followMemberTrackList);
            hashMap.put("likedTrackList",likedTrackList);
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
