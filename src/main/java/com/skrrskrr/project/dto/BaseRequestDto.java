package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseRequestDto {

    private Long loginMemberId;
    private Long offset;
    private Long limit;

}
