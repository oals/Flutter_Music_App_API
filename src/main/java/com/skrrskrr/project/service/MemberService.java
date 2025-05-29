package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.entity.Member;
import java.time.LocalDateTime;
import java.util.List;

public interface MemberService {

    default MemberDto EntityToDto(Member member){

        MemberDto memberDto = new MemberDto();
        memberDto.setMemberId(member.getMemberId());
        memberDto.setMemberNickName(member.getMemberNickName());
        memberDto.setMemberInfo(member.getMemberInfo());
        memberDto.setMemberEmail(member.getMemberEmail());
        memberDto.setMemberImagePath(member.getMemberImagePath());
        memberDto.setMemberFollowCnt(member.getMemberFollowCnt());
        memberDto.setMemberFollowerCnt(member.getMemberFollowerCnt());
        memberDto.setMemberDate(LocalDateTime.now().toString());
        memberDto.setMemberDeviceToken(member.getMemberDeviceToken());

        return memberDto;
    }

    List<FollowDto> getRecommendMember(Long loginMemberId);

    MemberResponseDto getSearchMember(SearchRequestDto searchRequestDto);

    Member getMemberEntity(Long memberId);

    MemberDto getMemberInfo(HomeRequestDto homeRequestDto);

    void setMemberInfoUpdate(MemberRequestDto memberRequestDto);

    MemberResponseDto getMemberPageInfo(MemberRequestDto memberRequestDto);

    Boolean setMemberImage(UploadDto uploadDto);

    List<FollowDto> getSearchMemberList(SearchRequestDto searchRequestDto);

    List<FollowDto> getRandomMemberList(MemberRequestDto memberRequestDto);

    Long getSearchMemberListCnt(SearchRequestDto searchRequestDto);

}