package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.AuthResponseDto;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.ArrayList;
import java.util.Date;


@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    @Value("${SECRET_KEY}")
    private String SECRET_KEY;

    // jwt 토큰 및 refresh 토큰 생성
    public String generateJwtToken(String uid, String email,Date exp,String tokenType) {

        // JWT 토큰 생성
        String jwtToken = Jwts.builder()
                .setSubject(uid)  // uid를 subject로 설정
                .claim("email", email)  // 이메일을 클레임으로 추가
                .claim("token_type", tokenType)  // 리프레시 토큰임을 나타내는 클레임 추가
                .setIssuer("skrrskrr.com")  // 발급자 설정
                .setIssuedAt(new Date())  // 발급일 설정
                .setExpiration(exp)  // 만료일 설정 (1시간 후)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // 서명 알고리즘 및 비밀 키 설정
                .compact();

        return jwtToken;
    }

    @Override
    public void validateJwtToken(String jwtToken) {

            // JWT 토큰을 파싱하고 서명을 검증
            Jwts.parser()
                    .setSigningKey(SECRET_KEY) // 비밀 키 설정
                    .build()
                    .parseSignedClaims(jwtToken); // 토큰 검증

    }

    // 토큰에서 인증 정보 가져오기
    public Authentication getAuthentication(String token) {
        User userDetails = new User(getUsername(token), "", new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Override
    public AuthResponseDto refreshAccessToken(String refreshToken) {

        // 리프레시 토큰을 검증하고 사용자 정보를 추출
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)  // 서명 검증을 위한 비밀 키 설정
                .build()
                .parseSignedClaims(refreshToken)  // 리프레시 토큰을 파싱하여 클레임 추출
                .getBody();  // 클레임 추출

        String uid = claims.getSubject();  // 사용자 ID
        String email = claims.get("email", String.class);  // 이메일 추출

        // 새로운 액세스 토큰 발급
        Date accessTokenExpiration = new Date(System.currentTimeMillis() + 1000 * 60 * 30);  // 15분 후 만료

        String jwtToken = generateJwtToken(uid, email, accessTokenExpiration, "access");

        return AuthResponseDto.builder()
                .jwtToken(jwtToken)
                .build();
    }


    // 토큰에서 사용자 이름 가져오기
    public String getUsername(String token) {

        // JWT 토큰 검증 및 Claims 추출
        Claims claims = Jwts.parser()  // parserBuilder 사용
                .setSigningKey(SECRET_KEY)  // 서명 검증을 위한 비밀 키 설정
                .build()  // 빌더를 사용하여 파서 생성
                .parseClaimsJws(token)  // 토큰 파싱 및 서명 검증
                .getBody();  // Claims 객체 반환

        return claims.getSubject();
    }

}
