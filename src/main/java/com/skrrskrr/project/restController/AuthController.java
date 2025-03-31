package com.skrrskrr.project.restController;

import com.skrrskrr.project.service.AuthService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap; import java.util.Map; import java.util.Map;

@RestController
@Log4j2
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * jwt 토큰 생성
     * @param hashMap
     * @return
     */
    @PostMapping("/auth/createJwtToken")
    public Map<String,Object> createJwtToken(@RequestBody Map<String,Object> hashMap) {
        Map<String,Object> returnMap = new HashMap<>();
        String uid = hashMap.get("uid").toString();
        String email = hashMap.get("email").toString();
        try{
            String jwtToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 10),"access");
            String refreshToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 30),"refresh");

            returnMap.put("status","200");
            returnMap.put("jwtToken",jwtToken);
            returnMap.put("refreshToken",refreshToken);

        } catch(JwtException ignored){
            ignored.printStackTrace();
            returnMap.put("status","500");
        }
        return returnMap;

    }


    @PostMapping("/auth/refreshToken")
    public Map<String,Object> refreshToken(@RequestHeader("Refresh-Token") String clientRefreshToken) {
        return authService.refreshAccessToken(clientRefreshToken);
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

            authService.validateJwtToken(jwtToken); // JWT 토큰 검증

            hashMap.put("status", "200");

        } catch(JwtException ignored){
            hashMap.put("status", "500");
        }

        return hashMap;
    }


}
