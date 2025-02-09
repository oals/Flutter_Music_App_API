package com.skrrskrr.project.restController;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.skrrskrr.project.service.AuthService;
import com.skrrskrr.project.service.FireBaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.codelibs.jhighlight.fastutil.Hash;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap; import java.util.Map; import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class AuthController {

    private final FireBaseService fireBaseService;
    private final AuthService authService;


    /**
     * jwt 토큰 생성
     * @param hashMap
     * @return
     */
    @PostMapping("/auth/getJwtToken")
    public Map<String,Object> getJwtToken(@RequestBody Map<String,Object> hashMap) {
        Map<String,Object> returnMap = new HashMap<>();
        System.err.println("전달된 uid  :" + hashMap.get("uid"));
        System.err.println("전달된 email :" + hashMap.get("email"));

        String uid = hashMap.get("uid").toString();
        String email = hashMap.get("email").toString();

        try{
            String jwtToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 60),"access");
            String refreshToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7),"refresh");

            System.err.println("생성된 jwt 토큰 :" + jwtToken);
            System.err.println("생성된 refreshJwt 토큰 :" + refreshToken);


            returnMap.put("status","200");
            returnMap.put("jwtToken",jwtToken);
            returnMap.put("refreshToken",refreshToken);

        } catch(JwtException ignored){
            ignored.printStackTrace();
            returnMap.put("status","500");
        }
        return returnMap;
    }


    /**
     * jwt token 및 jwt Refresh token 검증
     * @param clientJwtToken
     * @return
     */
    @PostMapping("/auth/jwtAuthing")
    public Map<String,Object> jwtAuthing(@RequestHeader("Authorization") String clientJwtToken) {
        Map<String,Object> hashMap = new HashMap<>();
        try{

            String jwtToken = clientJwtToken.replace("Bearer ", "");
            System.err.println("1. jwt 토큰 검증 시도 ");
            Map<String,Object> authResultMap = authService.validateJwtToken(jwtToken); // JWT 토큰 검증

            if(authResultMap != null){
                System.err.println("2. jwt 토큰 검증 끝 ");
                if (authResultMap.get("status") == "200") {
                    System.err.println("3. jwt 토큰 유효함 ");
                    Claims claims = (Claims) authResultMap.get("claims");
                    String tokenType = claims.get("token_type", String.class);
                    if (tokenType != null && tokenType.equals("refresh")) {
                        System.err.println("4. jwt 리프레쉬 토큰 검증 완료 ");

                        String uid = claims.get("uid", String.class);
                        String email = claims.get("email", String.class);
                        String newJwtToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 60),"access");
                        System.err.println("5. refresh jwt 토큰 생성 ");
                        hashMap.put("status","200");
                        hashMap.put("new_jwt_token",newJwtToken);

                    } else if (tokenType != null && tokenType.equals("access")) {
                        System.err.println("4. jwt 토큰 유효하다 클라이언트에 전송 ");
                        hashMap.put("status", "200");
                    } else {
                        hashMap.put("status", "500");
                    }

                }
            }

        } catch(JwtException ignored){
            System.err.println("2. jwt 토큰 검증 실패 ");
            hashMap.put("status", "500");
        }

        return hashMap;
    }


    /**
     * idToken 검사
     * @param token
     * @return
     */
    @PostMapping("/auth/fireBaseAuthing")
    public Map<String,Object> fireBaseAuthing(@RequestHeader("Authorization") String token) {

        System.err.println("fireBaseAuthing");
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
