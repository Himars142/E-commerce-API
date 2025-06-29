package com.example.demo3.service;

import com.example.demo3.dto.*;
import com.example.demo3.entity.UserEntity;

public interface UserService {
    JwtResponseDTO registerUser(UserRegistrationRequestDTO request);

    JwtResponseDTO loginUser(UserLoginDTO request);

    UserProfileDTO getMyProfile(String token);

    UserProfileDTO getUserProfile(Long id);

    void updateUserProfile(String token, UserUpdateRequestDTO request);

    UserEntity findByUserName(String username);
}
