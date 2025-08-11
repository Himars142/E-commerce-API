package com.example.demo3.service.impl;

import com.example.demo3.exception.UnauthorizedException;
import com.example.demo3.security.JwtUtil;
import com.example.demo3.service.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;

    public TokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates a token for the currently authenticated user
     * For registration/login, use generateAccessTokenForAuth() instead.
     */
    @Override
    public String generateAccessToken(String username) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserAuthorities(username);
        return jwtUtil.generateAccessToken(username, authorities);
    }

    /**
     * Generates a token for authentication processes (registration/login)
     * where no authentication context exists yet.
     */
    @Override
    public String generateAccessTokenForAuth(String username) {
        Collection<GrantedAuthority> defaultAuthorities = getDefaultAuthorities();
        return jwtUtil.generateAccessToken(username, defaultAuthorities);
    }

    /**
     * Generates a token with explicitly passed authorities (for the login process)
     */
    @Override
    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return jwtUtil.generateAccessToken(username, authorities);
    }

    /**
     * Generates a token from UserDetails (convenient when logging in)
     */
    public String generateAccessToken(UserDetails userDetails) {
        return jwtUtil.generateAccessToken(userDetails.getUsername(), userDetails.getAuthorities());
    }

    @Override
    public String generateRefreshToken(String username) {
        return jwtUtil.generateRefreshToken(username);
    }

    @Override
    public String getUsernameFromJwt(String token, String requestId) {
        if (!jwtUtil.validateJwtToken(token)) {
            throw new UnauthorizedException("Invalid token. Request id: " + requestId);
        }
        return jwtUtil.getUsernameFromJwt(token);
    }

    /**
     * Gets the current user's authority from the Security Context
     * Only works when user is already authenticated.
     */
    private Collection<? extends GrantedAuthority> getCurrentUserAuthorities(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            if (userDetails.getUsername().equals(username)) {
                return userDetails.getAuthorities();
            }
        }

        throw new UnauthorizedException(
                "Cannot generate token: user not authenticated or username mismatch. Expected: " + username);
    }

    /**
     * Returns default authorities for new users
     */
    private Collection<GrantedAuthority> getDefaultAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}