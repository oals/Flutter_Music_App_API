package com.skrrskrr.project.restController;


import com.skrrskrr.project.dto.MemberDto;
import com.skrrskrr.project.dto.MemberRequestDto;
import com.skrrskrr.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;



    @GetMapping("/api/getMemberInfo")
    public Map<String,Object> getMemberInfo(MemberRequestDto memberRequestDto){
        log.info("getMemberInfo");
        Map<String,Object> hashMap = new HashMap<>();
        MemberDto memberDto = memberService.getMemberInfo(memberRequestDto);

        if (memberDto != null){
            // 회원인 경우
            if (!Objects.equals(memberDto.getMemberDeviceToken(), memberRequestDto.getDeviceToken())) {
                // 디바이스 토큰 업데이트
                memberRequestDto.setLoginMemberId(memberDto.getMemberId());
                hashMap = memberService.setMemberDeviceToken(memberRequestDto);
                if(hashMap.get("status").equals("200")){
                    memberDto.setDeviceToken(memberRequestDto.getDeviceToken());
                    hashMap.put("member", memberDto);
                }
            }
        } else {
            // 비회원인 경우
            hashMap = memberService.setMemberInfo(memberRequestDto);
        }

        return hashMap;
    }


    @PostMapping("/api/setMemberInfoUpdate")
    public Map<String,Object> setMemberInfoUpdate(@RequestBody MemberRequestDto memberRequestDto){

        log.info("setMemberInfoUpdate");
        return memberService.setMemberInfoUpdate(memberRequestDto);

    }


    @GetMapping("/api/getMemberPageInfo")
    public Map<String,Object> getMemberPageInfo(MemberRequestDto memberRequestDto)
    {
        log.info("getMemberPageInfo");
        return memberService.getMemberPageInfo(memberRequestDto);

    }


}
