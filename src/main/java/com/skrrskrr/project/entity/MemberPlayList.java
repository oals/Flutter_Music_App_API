package com.skrrskrr.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberPlayList {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberPlayListId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    @ManyToOne
    @JoinColumn(name = "playList_id")
    @JsonIgnore
    private PlayList playList;



}
