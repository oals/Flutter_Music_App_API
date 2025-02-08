package com.skrrskrr.project.entity;

import com.skrrskrr.project.dto.MemberDTO;
import lombok.*;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String memberNickName;

    private String memberEmail;

    private String memberInfo;

    private String memberBirth;

    private String memberAddr;

    private String memberDate;

    private Long memberFollowCnt;

    private Long memberFollowerCnt;

    private String memberImagePath;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> followers;

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> following;

    @Column(length = 512)
    private String memberDeviceToken;


    @OneToMany(mappedBy = "member",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemberTrack> memberTrackList;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemberPlayList> memberPlayListList;



    public static Member createMember(MemberDTO memberDTO) {
        Member member = new Member();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        member.setMemberId(memberDTO.getMemberId());
        member.setMemberNickName(memberDTO.getMemberNickName()); // 수정 필요
        member.setMemberEmail(memberDTO.getMemberEmail());
        member.setMemberBirth(memberDTO.getMemberBirth()); // 수정 필요
        member.setMemberAddr(memberDTO.getMemberAddr()); // 수정 필요
        member.setMemberImagePath("");
        member.setMemberFollowCnt(0L); // 기본값
        member.setMemberFollowerCnt(0L); // 기본값
        member.setMemberDate(LocalDateTime.now().format(formatter));

        return member;
    }

}
