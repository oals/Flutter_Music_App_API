package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayListDto {

    private Long playListId;

    private Long trackId;

    private String playListNm;

    private Long playListLikeCnt;

    private Boolean isPlayListPrivacy;

    private Boolean isPlayListLike;

    private String playListImagePath;

    private Boolean isInPlayList;

    private String totalPlayTime;

    private Long trackCnt;

    private List<TrackDto> playListTrackList;

    private Long memberId;

    private Long loginMemberId;

    private String memberNickName;

    private Boolean isAlbum;

    private String albumDate;

}
