package com.example.demo3.service.impl;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.entity.UserRole;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.ForbiddenException;
import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.UserService;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.*;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceImplTest extends BaseServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl underTest;

    private static final UserEntity USER = new UserEntity();

    @BeforeAll
    static void setUp() {
        USER.setId(EXISTING_ENTITY_ID);
        USER.setUsername(USERNAME);
        USER.setRole(UserRole.ROLE_CUSTOMER);
    }

    @BeforeEach
    void clearRole() {
        USER.setRole(UserRole.ROLE_CUSTOMER);
    }

    @Nested
    @DisplayName("Validate token tests")
    class ValidateToken {

        @Test
        @DisplayName("Should throw unauthorized exception")
        void validateToken_ShouldThrowsUnauthorizedException() {
            when(jwtUtil.validateJwtToken(TOKEN_INVALID)).thenReturn(false);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> underTest.validateToken(TOKEN_INVALID, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            verify(jwtUtil).validateJwtToken(TOKEN_INVALID);
        }

        @Test
        @DisplayName("When token is valid should not throw exception")
        void validateToken_WhenTokenIsValid_ShouldNotThrowException() {
            when(jwtUtil.validateJwtToken(TOKEN)).thenReturn(true);

            assertDoesNotThrow(() -> underTest.validateToken(TOKEN, requestId));

            verify(jwtUtil).validateJwtToken(TOKEN);
        }
    }

    @Nested
    @DisplayName("Validate token and get user tests")
    class ValidateTokenAndGetUser {

        @Test
        @DisplayName("When token is invalid should throw unauthorized exception")
        void validateToken_WhenTokenIsInvalid_ShouldThrowUnauthorizedException() {
            when(jwtUtil.validateJwtToken(TOKEN_INVALID)).thenReturn(false);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> underTest.validateTokenAndGetUser(TOKEN_INVALID, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            verify(jwtUtil).validateJwtToken(TOKEN_INVALID);
        }

        @Test
        @DisplayName("When token is valid should return user")
        void validateTokenAndGetUser_WhenTokenIsValid_ShouldReturnUser() {
            when(jwtUtil.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtil.getUsernameFromJwt(TOKEN)).thenReturn(USERNAME);
            when(userService.findByUserName(USERNAME, requestId)).thenReturn(USER);

            UserEntity result = underTest.validateTokenAndGetUser(TOKEN, requestId);

            assertThat(result).isNotNull().isSameAs(USER);

            InOrder inOrder = inOrder(jwtUtil, jwtUtil, userService);
            inOrder.verify(jwtUtil).validateJwtToken(TOKEN);
            inOrder.verify(jwtUtil).getUsernameFromJwt(TOKEN);
            inOrder.verify(userService).findByUserName(USERNAME, requestId);
        }
    }

    @Nested
    @DisplayName("Check is user admin")
    class CheckIsUserAdmin {

        @BeforeEach
        void setUpAdminCheckMocks() {
            when(jwtUtil.validateJwtToken(TOKEN)).thenReturn(true);
            when(jwtUtil.getUsernameFromJwt(TOKEN)).thenReturn(USERNAME);
            when(userService.findByUserName(USERNAME, requestId)).thenReturn(USER);
        }

        @Test
        @DisplayName("When user is not admin should throw forbidden exception")
        void checkIsUserAdmin_WhenUserIsNotAdmin_ShouldThrowForbiddenException() {
            USER.setRole(UserRole.ROLE_CUSTOMER);

            ForbiddenException exception =
                    assertThrows(ForbiddenException.class, () -> underTest.checkIsUserAdmin(TOKEN, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            InOrder inOrder = inOrder(jwtUtil, jwtUtil, userService);
            inOrder.verify(jwtUtil).validateJwtToken(TOKEN);
            inOrder.verify(jwtUtil).getUsernameFromJwt(TOKEN);
            inOrder.verify(userService).findByUserName(USERNAME, requestId);
        }

        @Test
        @DisplayName("When user is admin should not throw exception")
        void checkIsUserAdmin_WhenUserIsAdmin_ShouldNotThrowException() {
            USER.setRole(UserRole.ROLE_ADMIN);

            assertDoesNotThrow(() -> underTest.checkIsUserAdmin(TOKEN, requestId));

            InOrder inOrder = inOrder(jwtUtil, jwtUtil, userService);
            inOrder.verify(jwtUtil).validateJwtToken(TOKEN);
            inOrder.verify(jwtUtil).getUsernameFromJwt(TOKEN);
            inOrder.verify(userService).findByUserName(USERNAME, requestId);
        }

        @Test
        @DisplayName("When user role is null should throw bad request exception")
        void checkIsUserAdmin_WhenUserRoleIsNull_ShouldThrowBadRequestException() {
            USER.setRole(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> underTest.checkIsUserAdmin(TOKEN, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            InOrder inOrder = inOrder(jwtUtil, jwtUtil, userService);
            inOrder.verify(jwtUtil).validateJwtToken(TOKEN);
            inOrder.verify(jwtUtil).getUsernameFromJwt(TOKEN);
            inOrder.verify(userService).findByUserName(USERNAME, requestId);
        }
    }
}