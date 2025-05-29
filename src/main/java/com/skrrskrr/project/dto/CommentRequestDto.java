package com.skrrskrr.project.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequestDto extends BaseRequestDto{

    private Long commentId;

    private Long trackId;

    private String commentText;

}
