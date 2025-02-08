package com.skrrskrr.project.dto;

import com.skrrskrr.project.entity.Comment;
import com.skrrskrr.project.entity.Member;
import com.skrrskrr.project.entity.Track;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    private Long commentId;

    private Long trackId; // 댓글 쓴 트랙

    private Long memberId;

    private String memberNickName; // 작성자

    private Long parentCommentMemberId;

    private String parentCommentMemberNickName;

    private String memberImagePath;

    private boolean commentLikeStatus;

    private Long commentLikeCnt;


    private String commentText;

    private String commentDate;

    private Long parentCommentId; // 부모 댓글
;
    private boolean isChildCommentActive;

    private List<CommentDTO> childComments; // 자식 댓글 목록




}
