package com.skrrskrr.project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.HashMap; import java.util.Map;

public interface AuthService {

    String generateJwtToken(String uid, String email, Date exp,String tokenType);

    Map<String,Object> validateJwtToken(String jwtToken);

    Authentication getAuthentication(String token);
}
