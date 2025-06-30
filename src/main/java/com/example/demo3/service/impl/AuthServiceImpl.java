package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public void validateToken(String token) {
        logger.info("Attempt to validate token");
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException("Invalid token!");
        }
        logger.info("Success token validation");
    }

    @Override
    public UserEntity validateTokenAndGetUser(String token) {
        validateToken(token);
        logger.info("Attempt to get user from token");
        return userService.findByUserName(jwtUtil.getUsernameFromJwt(token));
    }

    @Override
    public void checkIsUserAdmin(String token) {
        logger.info("Attempt to check is user admin");
        if (!validateTokenAndGetUser(token).getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ForbiddenException("You must be admin");
        }
        logger.info("Success. User is admin");
    }
}
