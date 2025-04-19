package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto extends BaseRequestDto {

    private Long memberTrackId;

    private String searchText;

    private List<String> searchTextList;

    private Long playListId;

    private Long trackCategoryId;

    private String memberNickName;

    private Boolean isAlbum;

    private Long trackId;

    private String trackNm;

    private String trackTime;

    private Long trackPlayCnt;

    private String trackCategoryNm;

    private Long trackLikeCnt;

    private String trackImagePath;

    private Boolean trackLikeStatus;

}
