package com.example.demo3.service;

public interface TokenService {
    String generateAccessToken(String username);

    String generateRefreshToken(String username);

    String getUsernameFromJwt(String token);
}
