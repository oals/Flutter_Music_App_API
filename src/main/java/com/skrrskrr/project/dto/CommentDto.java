package com.skrrskrr.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long commentId;

    private Long trackId; // 댓글 쓴 트랙

    private Long memberId;

    private String memberNickName; // 작성자

    private String memberImagePath;

    private Boolean commentLikeStatus;

    private Long commentLikeCnt;

    private String commentText;

    private String commentDate;

    private Long parentCommentId; // 부모 댓글

    private List<CommentDto> childComments; // 자식 댓글 목록

}
