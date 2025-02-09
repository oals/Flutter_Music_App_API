package com.skrrskrr.project.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    @Value("${secret.key}")
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
    public Map<String,Object> validateJwtToken(String jwtToken) {
        Map<String,Object> hashMap = new HashMap<>();
        try {
            // JWT 토큰을 파싱하고 서명을 검증
            Jws<Claims> jwts = Jwts.parser()
                    .setSigningKey(SECRET_KEY) // 비밀 키 설정
                    .build()
                    .parseSignedClaims(jwtToken); // 토큰 검증

            hashMap.put("claims",jwts.getBody());
            hashMap.put("status","200");

            return hashMap; // 유효한 클레임을 반환

        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            return null;
        } catch (JwtException e) {
            e.printStackTrace();
            // JWT 관련 다른 예외 처리
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            // 예기치 않은 오류 처리
            return null;
        }
    }

    // 토큰에서 인증 정보 가져오기
    public Authentication getAuthentication(String token) {
        User userDetails = new User(getUsername(token), "", new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
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
