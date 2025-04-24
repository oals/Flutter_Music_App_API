package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private CommentDto comment;

    private List<CommentDto> commentList;

}
