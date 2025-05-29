package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayListResponseDto {

    List<PlayListDto> playLists;

    PlayListDto playList;

    Long totalCount;

}
