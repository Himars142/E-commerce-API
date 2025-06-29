package com.example.demo3.service;

import com.example.demo3.entity.UserEntity;

public interface AuthService {
    void validateToken(String token);

    UserEntity validateTokenAndGetUser(String token);

    void checkIsUserAdmin(String token);
}
