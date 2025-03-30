package com.skrrskrr.project.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseRequestDto extends BaseQueryDto{

    private Long loginMemberId;
    private Long offset = 0L;
    private Long limit;

}
