package com.example.demo3.service.impl;

import com.example.demo3.dto.*;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.UserMapper;
import com.example.demo3.repository.UserRepository;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenServiceImpl tokenService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl underTest;

    private static final UserEntity USER = new UserEntity();
    private static final String EMAIL = "email@gmail.com";
    private static final String INVALID_PASSWORD = "invalid-password";
    private static final String VALID_PASSWORD = "valid-password";
    private static final Long EXISTING_USER_ID = 1L;
    private static final Long INVALID_USER_ID = 3L;

    @BeforeAll
    static void setUp() {
        USER.setId(EXISTING_USER_ID);
        USER.setUsername(USERNAME);
        USER.setPassword(VALID_PASSWORD);
    }

    @Nested
    @DisplayName("Register user tests")
    class RegisterUser {

        private static final UserRegistrationRequestDTO REQUEST = new UserRegistrationRequestDTO();

        @BeforeAll
        static void setUp() {
            REQUEST.setUsername(USERNAME);
            REQUEST.setEmail(EMAIL);
            REQUEST.setPassword(VALID_PASSWORD);
        }

        @Test
        @DisplayName("Should throw bad request exception username not unique")
        void registerUser_ShouldThrowBadRequestExceptionUsernameNotUnique() {
            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.of(new UserEntity()));

            assertThrows(BadRequestException.class, () -> underTest.registerUser(REQUEST, USER_AGENT));

            verify(userRepository).findByUsername(REQUEST.getUsername());
        }

        @Test
        @DisplayName("Should throw bad request exception email not unique")
        void registerUser_ShouldThrowBadRequestExceptionEmailNotUnique() {
            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(REQUEST.getEmail())).thenReturn(Optional.of(new UserEntity()));

            assertThrows(BadRequestException.class, () -> underTest.registerUser(REQUEST, USER_AGENT));

            verify(userRepository).findByUsername(REQUEST.getUsername());
            verify(userRepository).findByEmail(REQUEST.getEmail());
        }

        @Test
        @DisplayName("Should throw bad request exception password and password confirmation are not equal")
        void registerUser_ShouldThrowBadRequestExceptionPasswordAndPasswordConfirmationAreNotEqual() {
            REQUEST.setPasswordConfirmation(INVALID_PASSWORD);

            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(REQUEST.getEmail())).thenReturn(Optional.empty());

            assertThrows(BadRequestException.class, () -> underTest.registerUser(REQUEST, USER_AGENT));

            verify(userRepository).findByUsername(REQUEST.getUsername());
            verify(userRepository).findByEmail(REQUEST.getEmail());
        }

        @Test
        @DisplayName("Should save new user")
        void registerUser_ShouldSaveNewUser() {
            REQUEST.setPasswordConfirmation(VALID_PASSWORD);

            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(REQUEST.getEmail())).thenReturn(Optional.empty());
            when(tokenService.generateAccessToken(REQUEST.getUsername())).thenReturn(ACCESS_TOKEN);
            when(tokenService.generateRefreshToken(REQUEST.getUsername())).thenReturn(REFRESH_TOKEN);
            when(userMapper.createUserEntity(REQUEST, REFRESH_TOKEN, passwordEncoder.encode(REQUEST.getPassword())))
                    .thenReturn(USER);
            when(userRepository.save(any())).thenReturn(USER);

            JwtResponseDTO result = underTest.registerUser(REQUEST, USER_AGENT);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isNotBlank().isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isNotBlank().isEqualTo(REFRESH_TOKEN);

            InOrder inOrder = inOrder(userRepository, tokenService, userRepository);
            inOrder.verify(userRepository).findByUsername(REQUEST.getUsername());
            inOrder.verify(userRepository).findByEmail(REQUEST.getEmail());
            inOrder.verify(tokenService).generateAccessToken(REQUEST.getUsername());
            inOrder.verify(tokenService).generateRefreshToken(REQUEST.getUsername());
            inOrder.verify(userRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Login user tests")
    class LoginUser {

        private static final UserLoginDTO REQUEST = new UserLoginDTO();

        @BeforeAll
        static void setUp() {
            REQUEST.setUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw not found error user not found")
        void loginUser_ShouldThrowNotFoundErrorUserNotFound() {
            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.loginUser(REQUEST, USER_AGENT));

            verify(userRepository).findByUsername(REQUEST.getUsername());
        }

        @Test
        @DisplayName("Should throw bad request exception incorrect password")
        void loginUser_ShouldThrowBadRequestExceptionIncorrectPassword() {
            REQUEST.setPassword(INVALID_PASSWORD);

            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.of(USER));

            assertThrows(BadRequestException.class, () -> underTest.loginUser(REQUEST, USER_AGENT));

            verify(userRepository).findByUsername(REQUEST.getUsername());
        }

        @Test
        @DisplayName("Should return jwt response DTO")
        void loginUser_ShouldReturnJwtResponseDTO() {
            REQUEST.setPassword(VALID_PASSWORD);

            when(userRepository.findByUsername(REQUEST.getUsername())).thenReturn(Optional.of(USER));
            when(passwordEncoder.matches(REQUEST.getPassword(), USER.getPassword())).thenReturn(true);
            when(tokenService.generateAccessToken(USER.getUsername())).thenReturn(ACCESS_TOKEN);
            when(tokenService.generateRefreshToken(USER.getUsername())).thenReturn(REFRESH_TOKEN);

            JwtResponseDTO result = underTest.loginUser(REQUEST, USER_AGENT);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isNotBlank().isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isNotBlank().isEqualTo(REFRESH_TOKEN);

            InOrder inOrder = inOrder(userRepository, passwordEncoder, tokenService);
            inOrder.verify(userRepository).findByUsername(REQUEST.getUsername());
            inOrder.verify(passwordEncoder).matches(REQUEST.getPassword(), USER.getPassword());
            inOrder.verify(tokenService).generateAccessToken(USER.getUsername());
            inOrder.verify(tokenService).generateRefreshToken(USER.getUsername());
        }
    }

    @Nested
    @DisplayName("Get my profile tests")
    class GetMyProfile {

        @Test
        @DisplayName("Should return my profile")
        void getMyProfile() {
            when(tokenService.getUsernameFromJwt(eq(TOKEN), anyString())).thenReturn(USERNAME);
            when(userRepository.findByUsername(any())).thenReturn(Optional.of(USER));
            when(userMapper.toDTO(USER)).thenReturn(new UserProfileDTO());

            UserProfileDTO result = underTest.getMyProfile(TOKEN, USER_AGENT);

            assertThat(result).isNotNull();

            InOrder inOrder = inOrder(tokenService, userRepository, userMapper);
            inOrder.verify(tokenService).getUsernameFromJwt(eq(TOKEN), anyString());
            inOrder.verify(userRepository).findByUsername(any());
            inOrder.verify(userMapper).toDTO(USER);
        }
    }

    @Nested
    @DisplayName("Get user profile by id tests")
    class GetUserProfile {

        @Test
        @DisplayName("Should not found exception profile not found")
        void getUserProfile_ShouldNotFoundExceptionProfileNotFound() {
            when(userRepository.findById(eq(INVALID_USER_ID))).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.getUserProfile(INVALID_USER_ID, USER_AGENT));

            verify(userRepository).findById(eq(INVALID_USER_ID));
        }

        @Test
        @DisplayName("Should return user profile")
        void getUserProfile_ShouldReturnUserProfile() {
            when(userRepository.findById(eq(EXISTING_USER_ID))).thenReturn(Optional.of(USER));
            when(userMapper.toDTO(USER)).thenReturn(new UserProfileDTO());

            UserProfileDTO result = underTest.getUserProfile(EXISTING_USER_ID, USER_AGENT);

            assertThat(result).isNotNull();

            InOrder inOrder = inOrder(userRepository, userMapper);
            inOrder.verify(userRepository).findById(eq(EXISTING_USER_ID));
            inOrder.verify(userMapper).toDTO(USER);
        }
    }

    @Nested
    @DisplayName("Update user profile tests")
    class UpdateUserProfile {

        private static final UserUpdateRequestDTO request = new UserUpdateRequestDTO();

        @Test
        @DisplayName("Should throw not found exception user not found")
        void updateUserProfile_ShouldThrowNotFoundExceptionUserNotFound() {
            when(tokenService.getUsernameFromJwt(eq(TOKEN), any())).thenReturn(USERNAME);
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.updateUserProfile(TOKEN, request, USER_AGENT));

            InOrder inOrder = inOrder(tokenService, userRepository);
            inOrder.verify(tokenService).getUsernameFromJwt(eq(TOKEN), any());
            inOrder.verify(userRepository).findByUsername(anyString());
        }

        @Test
        @DisplayName("Should throw bad request exception user name not unique")
        void updateUserProfile_ShouldThrowBadRequestExceptionUserNameNotUnique() {
            request.setUsername(ANOTHER_USER_USERNAME);

            when(tokenService.getUsernameFromJwt(eq(TOKEN), any())).thenReturn(USERNAME);
            when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.of(USER));
            when(userRepository.findByUsername(eq(request.getUsername()))).thenReturn(Optional.of(new UserEntity()));

            assertThrows(BadRequestException.class, () -> underTest.updateUserProfile(TOKEN, request, USER_AGENT));

            InOrder inOrder = inOrder(tokenService, userRepository);
            inOrder.verify(tokenService).getUsernameFromJwt(eq(TOKEN), any());
            inOrder.verify(userRepository).findByUsername(eq(USERNAME));
            inOrder.verify(userRepository).findByUsername(eq(request.getUsername()));
        }

        @Test
        @DisplayName("Should save updated user")
        void updateUserProfile_ShouldSaveUpdatedUser() {
            when(tokenService.getUsernameFromJwt(eq(TOKEN), any())).thenReturn(USERNAME);
            when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.of(USER));
            when(userRepository.save(any())).thenReturn(USER);

            underTest.updateUserProfile(TOKEN, request, USER_AGENT);

            InOrder inOrder = inOrder(tokenService, userRepository);
            inOrder.verify(tokenService).getUsernameFromJwt(eq(TOKEN), any());
            inOrder.verify(userRepository).findByUsername(eq(USERNAME));
            inOrder.verify(userRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Find by user name tests")
    class FindByUserName {

        @Test
        @DisplayName("Should throw not found exception user not found")
        void findByUserName_ShouldThrowNotFoundExceptionUserNotFound() {
            when(userRepository.findByUsername(eq(USERNAME))).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.findByUserName(USERNAME, requestId));

            verify(userRepository).findByUsername(eq(USERNAME));
        }
    }
}