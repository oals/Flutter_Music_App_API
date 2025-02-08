package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;

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

    private boolean trackLikeStatus;

    @ManyToOne
    @JoinColumn(name = "member_track_id")
    private MemberTrack memberTrack;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;



}
