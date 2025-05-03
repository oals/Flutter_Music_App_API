package com.skrrskrr.project.service;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skrrskrr.project.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class HomeServiceImpl implements HomeService{

    private final MemberService memberService;
    private final TrackService trackService;
    private final PlayListService playlistService;

    @Override
    public HomeResponseDto firstLoad(HomeRequestDto homeRequestDto) {

        MemberDto memberDto = memberService.getMemberInfo(homeRequestDto);

        List<FollowDto> recommendMemberList = memberService.getRecommendMember(memberDto.getMemberId());

        List<TrackDto> recommendTrackList = trackService.getRecommendTrack(memberDto.getMemberId());

        List<PlayListDto> recommendPlayLists = playlistService.getRecommendPlayList(memberDto.getMemberId());

        List<PlayListDto> recommendAlbums = playlistService.getRecommendAlbum(memberDto.getMemberId());

        List<TrackDto> lastListenTrackList = trackService.getLastListenTrackList(memberDto.getMemberId());

        return HomeResponseDto.builder()
                .member(memberDto)
                .recommendMembers(recommendMemberList)
                .recommendTrackList(recommendTrackList)
                .recommendPlayLists(recommendPlayLists)
                .recommendAlbums(recommendAlbums)
                .lastListenTrackList(lastListenTrackList)
                .build();
    }
}
