package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.CustomUserDetails;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    public static class ErrorMessages {
        public static final String INVALID_TOKEN_MESSAGE = "Invalid token! Request id: ";
        public static final String ROLE_IS_NULL_MESSAGE = "Role is null! Request id: ";
        public static final String ACCESS_DENIED_MESSAGE = "Access is denied! Request id: ";
    }

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public UserEntity getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }

        throw new BadRequestException("Invalid authentication principal");
    }

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
