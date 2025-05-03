package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeResponseDto {

    private MemberDto member;

    List<PlayListDto> recommendPlayLists;

    List<PlayListDto> recommendAlbums;

    List<FollowDto> recommendMembers;

    List<TrackDto> recommendTrackList;

    List<TrackDto> lastListenTrackList;


}
