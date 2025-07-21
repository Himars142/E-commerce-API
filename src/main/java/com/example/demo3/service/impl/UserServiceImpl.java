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

import static com.example.demo3.utill.GenerateRequestID.generateRequestID;

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
    public JwtResponseDTO registerUser(UserRegistrationRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to register user. Request id: {}, user agent: {}, user: {}.",
                requestId, userAgent, request.toString());
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new BadRequestException("User with this username already exist! Request id:" + requestId);
                });
        userRepository.findByEmail(request.getEmail())
                .ifPresent(email -> {
                    throw new BadRequestException("User with this email already exist! Request id: " + requestId);
                });
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new BadRequestException("Password and password confirmation are not equal. Request id:" + requestId);
        }
        String accessToken = tokenService.generateAccessToken(request.getUsername());
        String refreshToken = tokenService.generateRefreshToken(request.getUsername());
        UserEntity userEntity = userRepository
                .save(userMapper.createUserEntity(request, refreshToken, passwordEncoder.encode(request.getPassword())));
        logger.info("Success! Request id: {}. Saved user: {}", requestId, userEntity.getId());
        return new JwtResponseDTO(accessToken, refreshToken);
    }

    @Transactional
    @Override
    public JwtResponseDTO loginUser(UserLoginDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to login user. Request id: {}, user agent: {}, user: {}.",
                requestId, userAgent, request.toString());
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("User with this username not exist. Request id: " + requestId));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Incorrect password. Request id: " + requestId);
        }
        String accessToken = tokenService.generateAccessToken(user.getUsername());
        String refreshToken = tokenService.generateRefreshToken(request.getUsername());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        logger.info("Successfully user logged in: {}, Request id: {}", user.getId(), requestId);
        return new JwtResponseDTO(accessToken, refreshToken);
    }

    @Override
    public UserProfileDTO getMyProfile(String token, String userAgent) {
        String requestId = generateRequestID();
        logger.info("User attempt to get own profile. Request id: {}, user agent: {}",
                requestId, userAgent);
        UserEntity entity = userRepository.findByUsername(tokenService.getUsernameFromJwt(token, requestId))
                .orElseThrow(() -> new NotFoundException("User not found. Request id: " + requestId));
        UserProfileDTO response = userMapper.toDTO(entity);
        logger.info("Successfully get profile: {}. Request id; {}", response.getId(), requestId);
        return response;
    }

    @Override
    public UserProfileDTO getUserProfile(Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("User attempt to get profile. Request id: {}, user agent: {}, profile id: {}",
                requestId, userAgent, id);
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found. User ID:" + id + ". Request id: " + requestId));
        UserProfileDTO response = userMapper.toDTO(entity);
        logger.info("Successfully get user profile: {}. Request id: {}", response.getId(), requestId);
        return response;
    }

    @Override
    public void updateUserProfile(String token, UserUpdateRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to update profile. Request id: {}, user agent: {}",
                requestId, userAgent);
        UserEntity entity = userRepository.findByUsername(tokenService.getUsernameFromJwt(token, requestId))
                .orElseThrow(() -> new NotFoundException("User not found. Request id: " + requestId));
        if (request.getUsername() != null && !request.getUsername().equals(entity.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(username -> {
                throw new BadRequestException("Username must be unique. Request id: " + requestId);
            });
        }
        UserEntity user = userRepository.save(userMapper.updateProfile(entity, request));
        logger.info("Successfully updated user : {}", user.getId());
    }

    @Override
    public UserEntity findByUserName(String username, String requestId) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with this username: " + username + " not found" + ". Request id; " + requestId));
    }
}
