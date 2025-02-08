package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.MemberDTO;
import com.skrrskrr.project.dto.UploadDTO;
import com.skrrskrr.project.entity.Member;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public interface MemberService {

    default MemberDTO EntityToDto(Member member){

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberId(member.getMemberId());
        memberDTO.setMemberNickName(member.getMemberNickName()); // 수정 필요
        memberDTO.setMemberEmail(member.getMemberEmail());
        memberDTO.setMemberBirth(member.getMemberBirth()); // 수정 필요
        memberDTO.setMemberAddr(member.getMemberAddr()); // 수정 필요
        memberDTO.setMemberImagePath(member.getMemberImagePath());
        memberDTO.setMemberFollowCnt(0L);
        memberDTO.setMemberFollowerCnt(0L);
        memberDTO.setMemberDate(LocalDateTime.now().toString());
        memberDTO.setDeviceToken(member.getMemberDeviceToken());

        return memberDTO;
    }


    MemberDTO getMemberInfo(String memberEmail);

    HashMap<String,Object> setMemberDeviceToken(MemberDTO memberDTO);

    HashMap<String,Object> setMemberInfoUpdate(MemberDTO memberDTO);

    HashMap<String,Object> setMemberInfo(String memberEmail,String deviceToken);

    HashMap<String,Object> getMemberPageInfo(Long memberId, Long loginMemberId);

    HashMap<String,Object> setMemberImage(UploadDTO uploadDTO);





}