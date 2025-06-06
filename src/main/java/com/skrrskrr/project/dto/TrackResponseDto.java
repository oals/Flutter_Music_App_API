package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackResponseDto {

    Long trackId;

    String trackImagePath;

    List<TrackDto> trackList;

    List<TrackDto> trackList2;

    TrackDto track;

    Long totalCount;

}