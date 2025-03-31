package com.skrrskrr.project.restController;

import com.google.firebase.auth.FirebaseAuthException;
import com.skrrskrr.project.service.FireBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class FireBaseController {

    private final FireBaseService fireBaseService;


    /**
     * idToken 검사
     * @param token
     * @return
     */
    @PostMapping("/auth/fireBaseAuthing")
    public Map<String,Object> fireBaseAuthing(@RequestHeader("Authorization") String token) {

        Map<String,Object> hashMap = new HashMap<>();
        String idToken = token.replace("Bearer ", "");

        try {
            fireBaseService.verifyFbToken(idToken);
            /// jwt토큰 갱신

            hashMap.put("status","200");
            return hashMap;

        } catch (FirebaseAuthException e) {
            /// 인증 실패 처리
            hashMap.put("status","500");
            return hashMap;
        }

    }

}
