package com.skrrskrr.project.dto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackRequestDto extends BaseRequestDto {

     private Long trackId;

     private List<Long> trackIdList;

     private String trackInfo;

     private Long trackCategoryId;

     private Boolean isTrackPrivacy;

}
