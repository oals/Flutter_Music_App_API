package com.skrrskrr.project.dto;

import com.skrrskrr.project.entity.Track;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayListDTO {

    private Long playListId;

    private Long trackId;

    private String playListNm;

    private Long playListLikeCnt;

    private Boolean isPlayListPrivacy;

    private boolean isPlayListLike;

    private String playListImagePath;

    private Boolean isInPlayList;

    private String totalPlayTime;

    private Long trackCnt;

    private List<TrackDTO> playListTrackList;

    private Long memberId;

    private String memberNickName;

    private boolean isAlbum;

    private String albumDate;

}
