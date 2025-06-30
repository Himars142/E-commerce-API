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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserMapper userMapper;

    public static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

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
        logger.info("Creating user with username: {}", request.getUsername());
        String accessToken = tokenService.generateAccessToken(request.getUsername());
        String refreshToken = tokenService.generateRefreshToken(request.getUsername());
        logger.debug("User date before saving: {}", request);
        userRepository.save(userMapper.createUserEntity(request, refreshToken, passwordEncoder.encode(request.getPassword())));
        logger.info("User saved with username {}", request.getUsername());
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
        return new JwtResponseDTO(accessToken);
    }

    @Override
    public UserProfileDTO getMyProfile(String token) {
        UserEntity entity = userRepository.findByUsername(tokenService.getUsernameFromJwt(token))
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(entity);
    }

    @Override
    public UserProfileDTO getUserProfile(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found. User ID:" + id));
        return userMapper.toDTO(entity);
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
        userRepository.save(userMapper.updateProfile(entity, request));
    }

    @Override
    public UserEntity findByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with this username: " + username + " not found"));
    }
}
