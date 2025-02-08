package com.skrrskrr.project.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentPlayList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentPlayListId;

    @ManyToOne
    @JoinColumn(name = "member_playList_id")
    private MemberPlayList memberPlayList;


}
