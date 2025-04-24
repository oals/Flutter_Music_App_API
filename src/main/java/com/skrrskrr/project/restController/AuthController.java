package com.skrrskrr.project.restController;

import com.skrrskrr.project.dto.AuthResponseDto;
import com.skrrskrr.project.dto.CommentResponseDto;
import com.skrrskrr.project.service.AuthService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<AuthResponseDto> createJwtToken(@RequestBody Map<String,Object> hashMap) {

        String uid = hashMap.get("uid").toString();
        String email = hashMap.get("email").toString();

        String jwtToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 10),"access");
        String refreshToken = authService.generateJwtToken(uid, email,new Date(System.currentTimeMillis() + 1000 * 60 * 30),"refresh");

        return ResponseEntity.ok(
                AuthResponseDto.builder()
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }


    @PostMapping("/auth/refreshToken")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestHeader("Refresh-Token") String clientRefreshToken) {
        AuthResponseDto authResponseDto = authService.refreshAccessToken(clientRefreshToken);
        return ResponseEntity.ok(authResponseDto);
    }

    /**
     * jwt token 및 jwt Refresh token 검증
     * @param clientJwtToken
     * @return
     */
    @PostMapping("/auth/jwtAuthing")
    public ResponseEntity<Void> jwtAuthing(@RequestHeader("Authorization") String clientJwtToken) {

        String jwtToken = clientJwtToken.replace("Bearer ", "");

        authService.validateJwtToken(jwtToken); // JWT 토큰 검증

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
