package com.example.demo3.service.impl;

import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    public TokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateAccessToken(String username) {
        logger.info("Attempt to generate access token for username: {}", username);
        return jwtUtil.generateAccessToken(username);
    }

    @Override
    public String generateRefreshToken(String username) {
        logger.info("Attempt to generate refresh token for username: {}", username);
        return jwtUtil.generateRefreshToken(username);
    }

    @Override
    public String getUsernameFromJwt(String token) {
        logger.info("Attempt to get username from token");
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException("Invalid token");
        }
        return jwtUtil.getUsernameFromJwt(token);
    }
}
