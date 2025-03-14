package com.skrrskrr.project.dto;

import lombok.*;

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

    private boolean isAlbum;

}
