package com.example.demo3.mapper;

import com.example.demo3.dto.UserProfileDTO;
import com.example.demo3.dto.UserRegistrationRequestDTO;
import com.example.demo3.dto.UserUpdateRequestDTO;
import com.example.demo3.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserProfileDTO toDTO(UserEntity entity) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public UserEntity createUserEntity(UserRegistrationRequestDTO request, String refreshToken, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(password);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRefreshToken(refreshToken);
        return user;
    }

    public UserEntity updateProfile(UserEntity entity, UserUpdateRequestDTO request) {
        entity.setUsername((request.getUsername() == null)
                ? entity.getUsername()
                : request.getUsername());
        entity.setFirstName((request.getFirstName() == null)
                ? entity.getFirstName()
                : request.getFirstName());
        entity.setLastName((request.getLastName() == null)
                ? entity.getLastName()
                : request.getLastName());
        entity.setPhone((request.getPhone() == null)
                ? entity.getPhone()
                : request.getPhone());
        return entity;
    }
}
