package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchDto {

    private Long memberId;

    private Long memberTrackId;

    private String searchText;

    private Long playListId;

    private Long trackCategoryId;

    private String memberNickName;

    private Long trackId;

    private String trackNm;

    private String trackTime;

    private Long trackPlayCnt;

    private String trackCategoryNm;

    private Long trackLikeCnt;

    private String trackImagePath;

    private Boolean trackLikeStatus;
}
