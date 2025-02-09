package com.skrrskrr.project.restController;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.skrrskrr.project.dto.MemberDTO;
import com.skrrskrr.project.service.FireBaseService;
import com.skrrskrr.project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; import java.util.Map;
import java.util.Map;
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Log4j2
public class MemberController {

    private final MemberService memberService;



    @GetMapping("/api/getMemberInfo")
    public Map<String,Object> getMemberInfo(@RequestParam("memberEmail") String memberEmail,@RequestParam("deviceToken") String deviceToken){
        log.info("getMemberInfo");
        Map<String,Object> hashMap = new HashMap<>();
        MemberDTO memberDTO = memberService.getMemberInfo(memberEmail);

        if (memberDTO != null){
            // 회원인 경우
            if (!Objects.equals(memberDTO.getMemberDeviceToken(), deviceToken)) {
                // 디바이스 토큰 업데이트
                hashMap = memberService.setMemberDeviceToken(memberDTO);
                if(hashMap.get("status").equals("200")){
                    memberDTO.setDeviceToken(deviceToken);
                    hashMap.put("member", memberDTO);
                }
            }
        } else {
            // 비회원인 경우
            hashMap = memberService.setMemberInfo(memberEmail,deviceToken);
        }

        return hashMap;
    }




    @PostMapping("/api/setMemberInfoUpdate")
    public Map<String,Object> setMemberInfoUpdate(@RequestBody MemberDTO memberDTO){

        log.info("setMemberInfoUpdate");
        return memberService.setMemberInfoUpdate(memberDTO);

    }



    @GetMapping("/api/getMemberPageInfo")
    public Map<String,Object> getMemberPageInfo(@RequestParam("memberId") Long memberId,
                                                @RequestParam("loginMemberId") Long loginMemberId)
    {
        log.info("getMemberPageInfo");
        return memberService.getMemberPageInfo(memberId, loginMemberId);

    }


}
