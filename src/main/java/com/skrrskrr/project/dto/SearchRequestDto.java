package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto extends BaseRequestDto {

    private Long memberId;

    private Long memberTrackId;

//    private Long moreId;

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
