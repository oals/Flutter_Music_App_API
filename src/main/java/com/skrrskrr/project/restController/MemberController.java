package com.skrrskrr.project.restController;


import com.skrrskrr.project.dto.*;
import com.skrrskrr.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;



    @GetMapping("/api/getMemberInfo")
    public ResponseEntity<MemberResponseDto> getMemberInfo(MemberRequestDto memberRequestDto){
        log.info("getMemberInfo");

        MemberResponseDto memberResponseDto = memberService.getMemberInfo(memberRequestDto);

        if (memberResponseDto.getMember() != null){
            MemberDto memberDto = memberResponseDto.getMember();
            // 회원인 경우
            if (!Objects.equals(memberDto.getMemberDeviceToken(), memberRequestDto.getDeviceToken())) {
                // 디바이스 토큰 업데이트
                memberRequestDto.setLoginMemberId(memberDto.getMemberId());
                Boolean isSuccess = memberService.setMemberDeviceToken(memberRequestDto);
                if (isSuccess) {
                    memberDto.setDeviceToken(memberRequestDto.getDeviceToken());
                    memberResponseDto.setMember(memberDto);
                }
            }
        } else {
            // 비회원인 경우
            memberResponseDto = memberService.setMemberInfo(memberRequestDto);
        }

        return ResponseEntity.ok(memberResponseDto);
    }

    @PostMapping("/api/setMemberInfoUpdate")
    public ResponseEntity<MemberResponseDto> setMemberInfoUpdate(@RequestBody MemberRequestDto memberRequestDto){
        log.info("setMemberInfoUpdate");
        memberService.setMemberInfoUpdate(memberRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @GetMapping("/api/getMemberPageInfo")
    public ResponseEntity<MemberResponseDto> getMemberPageInfo(MemberRequestDto memberRequestDto) {
        log.info("getMemberPageInfo");
        MemberResponseDto memberResponseDto = memberService.getMemberPageInfo(memberRequestDto);
        return ResponseEntity.ok(memberResponseDto);
    }

    @GetMapping("/api/getRecommendMember")
    public ResponseEntity<MemberResponseDto> getRecommendMember(MemberRequestDto memberRequestDto) {
        log.info("getRecommendMember");
        MemberResponseDto memberResponseDto = memberService.getRecommendMember(memberRequestDto);
        return ResponseEntity.ok(memberResponseDto);
    }


    @GetMapping("/api/getSearchMember")
    public ResponseEntity<MemberResponseDto> getSearchMember(SearchRequestDto searchRequestDto) {
        log.info("getSearchMember");
        MemberResponseDto memberResponseDto = memberService.getSearchMember(searchRequestDto);
        return ResponseEntity.ok(memberResponseDto);
    }
}
