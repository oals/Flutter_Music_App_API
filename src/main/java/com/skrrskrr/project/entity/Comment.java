package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne // 여러 댓글이 하나의 트랙에 속함
    @JoinColumn(name = "track_id")
    private Track track; // 댓글 쓴 트랙

    @ManyToOne // 여러 댓글이 하나의 회원에 속함
    @JoinColumn(name = "member_id")
    private Member member; // 작성자

    private String commentText;

    private Long commentLikeCnt;

    private String commentDate;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentLike> commentLikeList;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")  //null일경우 부모댓글
    private Comment parentComment; // 부모 댓글

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> childComments; // 자식 댓글 목록
}