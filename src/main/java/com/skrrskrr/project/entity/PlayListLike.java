package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayListLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playListLikeId;

    private Boolean playListLikeStatus;

    @ManyToOne
    @JoinColumn(name = "member_play_list_id")
    private MemberPlayList memberPlayList;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime playListLikeDate;
}
