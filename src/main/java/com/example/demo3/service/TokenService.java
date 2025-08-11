package com.example.demo3.service;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface TokenService {
    String generateAccessToken(String username);

    String generateRefreshToken(String username);

    String getUsernameFromJwt(String token, String requestId);

    String generateAccessTokenForAuth(String username);

    String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities);
}
