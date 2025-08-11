package com.example.demo3.service.impl;

import com.example.demo3.security.CustomUserDetails;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailServiceImpl.class);

    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        logger.info("Loading user by username: {}", username);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User with this username not found"));
        return new CustomUserDetails(user);
    }
}
