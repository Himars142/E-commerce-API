package com.example.demo3.service.impl;

import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.TokenService;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;

    public TokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateAccessToken(String username) {
        return jwtUtil.generateAccessToken(username);
    }

    @Override
    public String generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }

    @Override
    public String getUsernameFromJwt(String token, String requestId) {
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException("Invalid token. Request id: " + requestId);
        }
        return jwtUtil.getUsernameFromJwt(token);
    }
}
