package com.example.demo3.service;

import com.example.demo3.entity.UserEntity;

public interface AuthService {
    void validateToken(String token, String UUID);

    UserEntity validateTokenAndGetUser(String token, String UUID);

    void checkIsUserAdmin(String token, String UUID);
}
