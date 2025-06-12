package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayListRequestDto extends BaseRequestDto{

    private Long playListId;

    private Long trackId;

    private String playListNm;

    private Boolean isPlayListPrivacy;

    private List<Long> playListIdList;

    private Boolean isAlbum;

}
