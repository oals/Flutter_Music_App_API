package com.skrrskrr.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackLikeId;

    @Column(name = "is_track_like_status", nullable = false)
    private Boolean isTrackLikeStatus;

    @ManyToOne
    @JoinColumn(name = "member_track_id")
    private MemberTrack memberTrack;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime trackLikeDate;



}
