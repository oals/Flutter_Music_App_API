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

    MemberResponseDto getRecommendMember(MemberRequestDto memberRequestDto);

    MemberResponseDto getSearchMember(SearchRequestDto searchRequestDto);

    Member getMemberEntity(Long memberId);

    MemberResponseDto getMemberInfo(MemberRequestDto memberRequestDto);

    Boolean setMemberDeviceToken(MemberRequestDto memberRequestDto);

    void setMemberInfoUpdate(MemberRequestDto memberRequestDto);

    MemberResponseDto setMemberInfo(MemberRequestDto memberRequestDto);

    MemberResponseDto getMemberPageInfo(MemberRequestDto memberRequestDto);

    Boolean setMemberImage(UploadDto uploadDto);

    List<FollowDto> getSearchMemberList(SearchRequestDto searchRequestDto);

    List<MemberDto> getRandomMemberList(MemberRequestDto memberRequestDto);

    Long getSearchMemberListCnt(SearchRequestDto searchRequestDto);

}