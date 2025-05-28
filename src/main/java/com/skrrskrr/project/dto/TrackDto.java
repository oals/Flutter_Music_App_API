package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TrackDto {

    private Long trackId;

    private String trackNm;

    private String trackInfo;

    private String trackTime;

    private Long trackLikeCnt;

    private Long trackPlayCnt;

    private Long trackCategoryId;

    private String trackPath;

    private String trackUploadDate;

    private String trackImagePath;

    private Boolean isTrackPrivacy;

    private Boolean isFollowMember;

    private Boolean isTrackLikeStatus;

    private Long commentsCnt;

    private Long memberId;

    private String memberNickName;

    private String memberImagePath;

}
