package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto extends BaseRequestDto {

    private String searchText;

    private List<String> searchKeywordList;

    private Boolean isAlbum;

}
