package com.example.demo3.service;

import com.example.demo3.dto.*;
import com.example.demo3.entity.UserEntity;

public interface UserService {
    JwtResponseDTO registerUser(UserRegistrationRequestDTO request, String userAgent);

    JwtResponseDTO loginUser(UserLoginDTO request, String userAgent);

    UserProfileDTO getMyProfile(String userAgent);

    UserProfileDTO getUserProfile(Long id, String userAgent);

    void updateUserProfile(UserUpdateRequestDTO request, String userAgent);

    UserEntity findByUserName(String username, String requestId);
}
