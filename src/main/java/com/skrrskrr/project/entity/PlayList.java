package com.skrrskrr.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playListId;

    private String playListNm;

    private Long playListLikeCnt;

    private Boolean isPlayListPrivacy;

    private Boolean isAlbum;

    private String albumDate;

    private String totalPlayTime;

    private String playListImagePath;

    private Long trackCnt;

    @ManyToOne
    @JoinColumn(name = "member_id") // 플레이리스트 소유자 ID
    @JsonIgnore
    private Member member;

    @OneToMany(mappedBy = "playList",cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    private List<MemberPlayList> memberPlayListList;


    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "playlist_track", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "playlist_id"), // 플레이리스트 외래 키
            inverseJoinColumns = @JoinColumn(name = "member_track_Id") // 트랙 외래 키
    )
    @Builder.Default
    private List<MemberTrack> playListTrackList = new ArrayList<>(); // 여러 트랙을 가진다.

}
