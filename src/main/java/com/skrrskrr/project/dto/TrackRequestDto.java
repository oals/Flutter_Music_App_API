package com.skrrskrr.project.dto;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackRequestDto extends BaseRequestDto {

     private Long trackId;

     private String trackInfo;

     private Long trackCategoryId;

     private Boolean trackPrivacy;

}
