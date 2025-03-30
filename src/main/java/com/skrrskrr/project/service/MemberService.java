package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.Member;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MemberService {


    default MemberDto EntityToDto(Member member){

        MemberDto memberDto = new MemberDto();
        memberDto.setMemberId(member.getMemberId());
        memberDto.setMemberNickName(member.getMemberNickName());
        memberDto.setMemberInfo(member.getMemberInfo());
        memberDto.setMemberEmail(member.getMemberEmail());
        memberDto.setMemberBirth(member.getMemberBirth());
        memberDto.setMemberAddr(member.getMemberAddr());
        memberDto.setMemberImagePath(member.getMemberImagePath());
        memberDto.setMemberFollowCnt(0L);
        memberDto.setMemberFollowerCnt(0L);
        memberDto.setMemberDate(LocalDateTime.now().toString());
        memberDto.setDeviceToken(member.getMemberDeviceToken());

        return memberDto;
    }

    Member getMemberEntity(Long memberId);

    Map<String,Object> getMemberInfo(MemberRequestDto memberRequestDto);

    Map<String,Object> setMemberDeviceToken(MemberRequestDto memberRequestDto);

    Map<String,Object> setMemberInfoUpdate(MemberRequestDto memberRequestDto);

    Map<String,Object> setMemberInfo(MemberRequestDto memberRequestDto);

    Map<String,Object> getMemberPageInfo(MemberRequestDto memberRequestDto);

    Map<String,Object> setMemberImage(UploadDto uploadDto);

    List<FollowDto> getSearchMemberList(SearchRequestDto searchRequestDto);

    List<MemberDto> getRandomMemberList(MemberRequestDto memberRequestDto);

    Long getSearchMemberListCnt(SearchRequestDto searchRequestDto);

}