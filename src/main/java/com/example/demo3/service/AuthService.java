package com.example.demo3.service;

import com.example.demo3.entity.UserEntity;

public interface AuthService {
    UserEntity getCurrentAuthenticatedUser();

    void validateToken(String token, String requestId);

    UserEntity validateTokenAndGetUser(String token, String requestId);

    void checkIsUserAdmin(String token, String requestId);

    void checkIsUserAdmin(UserEntity user, String requestId);
}
