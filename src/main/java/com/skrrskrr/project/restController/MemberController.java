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

    @GetMapping("/api/getSearchMember")
    public ResponseEntity<MemberResponseDto> getSearchMember(SearchRequestDto searchRequestDto) {
        log.info("getSearchMember");
        MemberResponseDto memberResponseDto = memberService.getSearchMember(searchRequestDto);
        return ResponseEntity.ok(memberResponseDto);
    }
}
