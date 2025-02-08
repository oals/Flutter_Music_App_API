package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentTrackId;

    @ManyToOne
    @JoinColumn(name = "member_track_id")
    private MemberTrack memberTrack;







}
