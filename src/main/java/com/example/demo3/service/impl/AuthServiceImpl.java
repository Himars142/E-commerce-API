package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthServiceImpl(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void validateToken(String token, String requestId) {
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException("Invalid token! Request id: " + requestId);
        }
    }

    @Override
    public UserEntity validateTokenAndGetUser(String token, String requestId) {
        validateToken(token, requestId);
        return userService.findByUserName(jwtUtil.getUsernameFromJwt(token), requestId);
    }

    @Override
    public void checkIsUserAdmin(String token, String requestId) {
        if (!validateTokenAndGetUser(token, requestId).getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ForbiddenException("You must be admin! Request id: " + requestId);
        }
    }
}
