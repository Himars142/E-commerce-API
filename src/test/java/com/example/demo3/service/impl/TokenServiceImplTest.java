package com.example.demo3.service.impl;

import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceImplTest extends BaseServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenServiceImpl underTest;

    @Test
    void generateAccessToken() {
        String token = "valid-token";
        String username = "username";

        when(jwtUtil.generateAccessToken(username)).thenReturn(token);

        underTest.generateAccessToken(username);

        verify(jwtUtil).generateAccessToken(username);
    }

    @Test
    void generateRefreshToken() {
        String token = "valid-token";
        String username = "username";

        when(jwtUtil.generateRefreshToken(username)).thenReturn(token);

        underTest.generateRefreshToken(username);

        verify(jwtUtil).generateRefreshToken(username);
    }

    @Test
    void getUsernameFromJwt_ShouldThrowUnauthorizedException() {
        String token = "valid-token";
        String requestId = "request-id";

        when(jwtUtil.validateJwtToken(token)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> underTest.getUsernameFromJwt(token, requestId));

        verify(jwtUtil).validateJwtToken(token);
    }

    @Test
    void getUsernameFromJwt() {
        String token = "valid-token";
        String requestId = "request-id";
        String username = "username";

        when(jwtUtil.validateJwtToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromJwt(token)).thenReturn(username);

        underTest.getUsernameFromJwt(token, requestId);

        verify(jwtUtil).validateJwtToken(token);
        verify(jwtUtil).getUsernameFromJwt(token);
    }
}