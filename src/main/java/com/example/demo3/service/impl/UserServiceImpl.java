package com.example.demo3.service.impl;

import com.example.demo3.dto.*;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.UserMapper;
import com.example.demo3.repository.UserRepository;
import com.example.demo3.service.TokenService;
import com.example.demo3.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserMapper userMapper;

    public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public JwtResponseDTO registerUser(UserRegistrationRequestDTO request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new BadRequestException("User with this username already exist!");
                });
        userRepository.findByEmail(request.getEmail())
                .ifPresent(email -> {
                    throw new BadRequestException("User with this email already exist!");
                });
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new BadRequestException("Password and password confirmation are not equal");
        }
        String accessToken = tokenService.generateAccessToken(request.getUsername());
        String refreshToken = tokenService.generateRefreshToken(request.getUsername());
        UserEntity userEntity = userRepository
                .save(userMapper.createUserEntity(request, refreshToken, passwordEncoder.encode(request.getPassword())));
        logger.info("Saved user: {}", userEntity.getId());
        return new JwtResponseDTO(accessToken);
    }

    @Override
    public JwtResponseDTO loginUser(UserLoginDTO request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User with this username not exist"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }
        String accessToken = tokenService.generateAccessToken(user.getUsername());
        logger.info("Successfully user logged in: {}", user.getId());
        return new JwtResponseDTO(accessToken);
    }

    @Override
    public UserProfileDTO getMyProfile(String token) {
        UserEntity entity = userRepository.findByUsername(tokenService.getUsernameFromJwt(token))
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserProfileDTO response = userMapper.toDTO(entity);
        logger.info("Successfully get profile: {}", response.getId());
        return response;
    }

    @Override
    public UserProfileDTO getUserProfile(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found. User ID:" + id));
        UserProfileDTO response = userMapper.toDTO(entity);
        logger.info("Successfully get user profile: {}", response.getId());
        return response;
    }

    @Override
    public void updateUserProfile(String token, UserUpdateRequestDTO request) {
        UserEntity entity = userRepository.findByUsername(tokenService.getUsernameFromJwt(token))
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (request.getUsername() != null) {
            userRepository.findByUsername(request.getUsername()).ifPresent(username -> {
                throw new BadRequestException("Username must be unique");
            });
        }
        UserEntity user = userRepository.save(userMapper.updateProfile(entity, request));
        logger.info("Successfully updated user : {}", user.getId());
    }

    @Override
    public UserEntity findByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with this username: " + username + " not found"));
    }
}
