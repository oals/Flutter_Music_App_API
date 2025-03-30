package com.skrrskrr.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackId;

    private String trackNm;

    private String trackInfo;

    private String trackTime;

    private Long trackLikeCnt;

    private Long trackPlayCnt;

    private Long trackCategoryId;

    private String trackPath;

    private String trackImagePath;

    private String trackUploadDate;

    private Boolean isTrackPrivacy;


    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemberTrack> memberTrackList;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrackCategory> trackCategoryList;

    @ManyToMany(mappedBy = "playListTrackList", fetch = FetchType.LAZY)
    private List<PlayList> playlistTrack; // 여러 플레이리스트에 포함될 수 있다.

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> commentList; // 트랙에 대한 댓글 목록



}
