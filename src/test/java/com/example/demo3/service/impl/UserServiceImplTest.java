package com.example.demo3.service.impl;

import com.example.demo3.dto.UserLoginDTO;
import com.example.demo3.dto.UserProfileDTO;
import com.example.demo3.dto.UserRegistrationRequestDTO;
import com.example.demo3.dto.UserUpdateRequestDTO;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.UserMapper;
import com.example.demo3.repository.UserRepository;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void registerUser_ShouldThrowBadRequestExceptionUsernameNotUnique() {
        String userAgent = "testing";

        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setUsername("username");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new UserEntity()));

        assertThrows(BadRequestException.class, () -> underTest.registerUser(request, userAgent));

        verify(userRepository).findByUsername(request.getUsername());
    }

    @Test
    void registerUser_ShouldThrowBadRequestExceptionEmailNotUnique() {
        String userAgent = "testing";

        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setUsername("username");
        request.setEmail("email@gmail.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new UserEntity()));

        assertThrows(BadRequestException.class, () -> underTest.registerUser(request, userAgent));

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    void registerUser_ShouldThrowBadRequestExceptionPasswordAndPasswordConfirmationAreNotEqual() {
        String userAgent = "testing";

        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setUsername("username");
        request.setEmail("email@gmail.com");
        request.setPassword("1");
        request.setPasswordConfirmation("2");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> underTest.registerUser(request, userAgent));

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    void registerUser_ShouldSaveNewUser() {
        String userAgent = "testing";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setUsername("username");
        request.setEmail("email@gmail.com");
        request.setPassword("1");
        request.setPasswordConfirmation("1");

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(tokenService.generateAccessToken(request.getUsername())).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(request.getUsername())).thenReturn(refreshToken);
        when(userMapper.createUserEntity(request, refreshToken, passwordEncoder.encode(request.getPassword()))).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);

        underTest.registerUser(request, userAgent);

        verify(userRepository).findByUsername(request.getUsername());
        verify(userRepository).findByEmail(request.getEmail());
        verify(tokenService).generateAccessToken(request.getUsername());
        verify(tokenService).generateRefreshToken(request.getUsername());
        verify(userRepository).save(any());
    }

    @Test
    void loginUser_ShouldThrowNotFoundErrorUserNotFound() {
        String userAgent = "testing";

        UserLoginDTO request = new UserLoginDTO();
        request.setUsername("username");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.loginUser(request, userAgent));

        verify(userRepository).findByUsername(request.getUsername());
    }

    @Test
    void loginUser_ShouldThrowBadRequestExceptionIncorrectPassword() {
        String userAgent = "testing";

        UserLoginDTO request = new UserLoginDTO();
        request.setUsername("username");
        request.setPassword("2");

        UserEntity user = new UserEntity();
        user.setPassword("1");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> underTest.loginUser(request, userAgent));

        verify(userRepository).findByUsername(request.getUsername());
    }

    @Test
    void loginUser_ShouldReturnJwtResponseDTO() {
        String userAgent = "testing";

        UserLoginDTO request = new UserLoginDTO();
        request.setUsername("username");
        request.setPassword("1");

        UserEntity user = new UserEntity();
        user.setPassword("1");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        underTest.loginUser(request, userAgent);

        verify(userRepository).findByUsername(request.getUsername());
    }

    @Test
    void getMyProfile() {
        String userAgent = "testing";
        String token = "valid-token";

        UserEntity entity = new UserEntity();

        when(tokenService.getUsernameFromJwt(eq(token), anyString())).thenReturn("username");
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(new UserProfileDTO());

        underTest.getMyProfile(token, userAgent);

        verify(tokenService).getUsernameFromJwt(eq(token), anyString());
        verify(userRepository).findByUsername(any());
        verify(userMapper).toDTO(entity);
    }

    @Test
    void getUserProfile_ShouldNotFoundExceptionProfileNotFound() {
        Long id = 1L;
        String userAgent = "testing";

        when(userRepository.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.getUserProfile(id, userAgent));

        verify(userRepository).findById(eq(id));
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile() {
        Long id = 1L;
        String userAgent = "testing";

        UserEntity entity = new UserEntity();

        when(userRepository.findById(eq(id))).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(new UserProfileDTO());

        underTest.getUserProfile(id, userAgent);

        verify(userRepository).findById(eq(id));
        verify(userMapper).toDTO(entity);
    }

    @Test
    void updateUserProfile_ShouldThrowNotFoundExceptionUserNotFound() {
        String token = "valid-token";
        String userAgent = "testing";

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();

        when(tokenService.getUsernameFromJwt(eq(token), any())).thenReturn("username");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.updateUserProfile(token, request, userAgent));

        verify(tokenService).getUsernameFromJwt(eq(token), any());
        verify(userRepository).findByUsername(anyString());
    }

    @Test
    void updateUserProfile_ShouldThrowBadRequestExceptionUserNameNotUnique() {
        String token = "valid-token";
        String userAgent = "testing";
        String username = "username";

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setUsername("other-username");

        UserEntity entity = new UserEntity();

        when(tokenService.getUsernameFromJwt(eq(token), any())).thenReturn(username);
        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(entity));
        when(userRepository.findByUsername(eq(request.getUsername()))).thenReturn(Optional.of(entity));

        assertThrows(BadRequestException.class, () -> underTest.updateUserProfile(token, request, userAgent));

        verify(tokenService).getUsernameFromJwt(eq(token), any());
        verify(userRepository).findByUsername(eq(username));
        verify(userRepository).findByUsername(eq(request.getUsername()));
    }

    @Test
    void updateUserProfile_ShouldSaveUpdatedUser() {
        String token = "valid-token";
        String userAgent = "testing";
        String username = "username";

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();

        UserEntity entity = new UserEntity();
        entity.setId(1L);

        when(tokenService.getUsernameFromJwt(eq(token), any())).thenReturn(username);
        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(entity);

        underTest.updateUserProfile(token, request, userAgent);

        verify(tokenService).getUsernameFromJwt(eq(token), any());
        verify(userRepository).findByUsername(eq(username));
        verify(userRepository).save(any());
    }

    @Test
    void findByUserName_ShouldThrowNotFoundExceptionUserNotFound() {
        String username = "username";
        String requestId = java.util.UUID.randomUUID().toString();

        when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.findByUserName(username, requestId));

        verify(userRepository).findByUsername(eq(username));
    }
}