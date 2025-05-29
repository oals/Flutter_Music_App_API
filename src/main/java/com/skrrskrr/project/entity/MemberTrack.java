package com.skrrskrr.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberTrackId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    @ManyToOne
    @JoinColumn(name = "track_id")
    @JsonIgnore
    private Track track;

    @OneToMany(mappedBy = "memberTrack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrackLike> trackLikeList;

    @ManyToMany(mappedBy = "playListTrackList", fetch = FetchType.LAZY)
    private List<PlayList> playlistTrack;

}
