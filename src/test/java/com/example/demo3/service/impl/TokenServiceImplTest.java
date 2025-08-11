package com.example.demo3.service.impl;

import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.impl.testutil.BaseServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceImplTest extends BaseServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenServiceImpl underTest;

    @Nested
    @DisplayName("Generate access token tests")
    class GenerateAccessToken {

        @Test
        @DisplayName("Should generate access token")
        void generateAccessToken() {
            /*when(jwtUtil.generateAccessToken(USERNAME)).thenReturn(ACCESS_TOKEN);

            String result = underTest.generateAccessToken(USERNAME);

            assertThat(result).isNotBlank().isEqualTo(ACCESS_TOKEN);

            verify(jwtUtil).generateAccessToken(USERNAME);*/
        }
    }

    @Nested
    @DisplayName("Generate refresh token tests")
    class GenerateRefreshToken {

        @Test
        @DisplayName("Should generate refresh token")
        void generateRefreshToken() {
            when(jwtUtil.generateRefreshToken(USERNAME)).thenReturn(REFRESH_TOKEN);

            String result = underTest.generateRefreshToken(USERNAME);

            assertThat(result).isNotBlank().isEqualTo(REFRESH_TOKEN);

            verify(jwtUtil).generateRefreshToken(USERNAME);
        }
    }

    @Nested
    @DisplayName("Get username from jwt tests")
    class GetUsernameFromJwt {

        @Test
        @DisplayName("Should throw unauthorized exception")
        void getUsernameFromJwt_ShouldThrowUnauthorizedException() {
            when(jwtUtil.validateJwtToken(TOKEN)).thenReturn(false);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> underTest.getUsernameFromJwt(TOKEN, requestId));

            assertThat(exception.getMessage()).contains(requestId);

            verify(jwtUtil).validateJwtToken(TOKEN);
        }

        @Test
        @DisplayName("Should return username")
        void getUsernameFromJwt() {
            when(jwtUtil.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtil.getUsernameFromJwt(TOKEN)).thenReturn(USERNAME);

            String result = underTest.getUsernameFromJwt(TOKEN, requestId);

            assertThat(result).isNotBlank().isEqualTo(USERNAME);

            verify(jwtUtil).validateJwtToken(TOKEN);
            verify(jwtUtil).getUsernameFromJwt(TOKEN);
        }
    }
}