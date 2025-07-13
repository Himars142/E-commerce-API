package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.UserService;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AuthServiceImplTest extends BaseServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;


    @Test
    void validateToken_ShouldThrowsUnauthorizedException() {
        String token = "invalid-token";
        String requestId = "request-123";

        when(jwtUtil.validateJwtToken(token)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.validateToken(token, requestId));
    }

    @Test
    void validateTokenAndGetUser_ValidTokenAndReturnsUser() {
        String token = "valid-token";
        String requestId = "request-123";
        String username = "testUser";
        UserEntity user = new UserEntity();

        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJwt(token)).thenReturn(username);
        when(userService.findByUserName(username, requestId)).thenReturn(user);

        UserEntity result = authService.validateTokenAndGetUser(token, requestId);

        assertEquals(result.getId(), user.getId());
    }

    @Test
    void validateTokenAndGetUser_ValidTokenAndThrowsUnauthorizedException() {
        String token = "invalid-token";
        String requestId = "request-123";

        when(jwtUtil.validateJwtToken(token)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.validateTokenAndGetUser(token, requestId));
    }

    @Test
    void checkIsUserAdmin_UserCustomerThrowsForbiddenException() {
        String token = "valid-token";
        String requestId = "request-123";
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_CUSTOMER);

        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJwt(token)).thenReturn(username);
        when(userService.findByUserName(username, requestId)).thenReturn(user);

        assertThrows(ForbiddenException.class, () -> authService.checkIsUserAdmin(token, requestId));
    }

    @Test
    void checkIsUserAdmin() {
        String token = "valid-token";
        String requestId = "request-123";
        String username = "testUser";
        UserEntity user = new UserEntity();
        user.setRole(UserRole.ROLE_ADMIN);

        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJwt(token)).thenReturn(username);
        when(userService.findByUserName(username, requestId)).thenReturn(user);

        authService.checkIsUserAdmin(token, requestId);
    }
}