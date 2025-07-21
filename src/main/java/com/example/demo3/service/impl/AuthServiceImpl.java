package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    static class ErrorMessages {
        private static final String INVALID_TOKEN_MESSAGE = "Invalid token! Request id: ";
        private static final String ROLE_IS_NULL_MESSAGE = "Role is null! Request id: ";
        private static final String ACCESS_DENIED_MESSAGE = "Access is denied! Request id: ";
    }

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthServiceImpl(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void validateToken(String token, String requestId) {
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException(ErrorMessages.INVALID_TOKEN_MESSAGE + requestId);
        }
    }

    @Override
    public UserEntity validateTokenAndGetUser(String token, String requestId) {
        validateToken(token, requestId);
        return userService.findByUserName(jwtUtil.getUsernameFromJwt(token), requestId);
    }

    @Override
    public void checkIsUserAdmin(String token, String requestId) {
        UserEntity user = validateTokenAndGetUser(token, requestId);
        checkIsUserAdmin(user, requestId);
    }

    @Override
    public void checkIsUserAdmin(UserEntity user, String requestId) {
        if (user.getRole() == null) {
            throw new BadRequestException(ErrorMessages.ROLE_IS_NULL_MESSAGE + requestId);
        }
        if (!user.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ForbiddenException(ErrorMessages.ACCESS_DENIED_MESSAGE + requestId);
        }
    }
}
