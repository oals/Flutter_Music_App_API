package com.skrrskrr.project.service;

import com.skrrskrr.project.dto.AuthResponseDto;
import org.springframework.security.core.Authentication;
import java.util.Date;

public interface AuthService {

    String generateJwtToken(String uid, String email, Date exp,String tokenType);

    void validateJwtToken(String jwtToken);

    Authentication getAuthentication(String token);

    AuthResponseDto refreshAccessToken(String refreshToken);
}
