package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseRequestDto {

    private Long loginMemberId;
    private Long memberId;
    private Long offset = 0L;
    private Long limit;

}
