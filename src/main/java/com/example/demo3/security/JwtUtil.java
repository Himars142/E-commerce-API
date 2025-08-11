package com.example.demo3.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for JWT token operations.
 */
@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String ROLES_CLAIM = "roles";
    private static final int MIN_SECRET_LENGTH = 64;

    private final String jwtSecret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private Key signingKey;

    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Initializes the signing key and validates its length.
     */
    @PostConstruct
    private void init() {
        if (jwtSecret == null || jwtSecret.length() < MIN_SECRET_LENGTH) {
            logger.error("JWT secret must be at least {} characters", MIN_SECRET_LENGTH);
            throw new IllegalArgumentException("JWT secret too short");
        }
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates an access token with roles.
     */
    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES_CLAIM, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generates a refresh token.
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extracts username from JWT token.
     */
    public String getUsernameFromJwt(String token) {
        return getClaims(token).map(Claims::getSubject).orElse(null);
    }

    /**
     * Extracts authorities from JWT token.
     */
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        return getClaims(token)
                .map(claims -> {
                    Object roles = claims.get(ROLES_CLAIM);
                    if (roles instanceof List<?> rolesList) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        for (Object role : rolesList) {
                            if (role != null) {
                                authorities.add(new SimpleGrantedAuthority(role.toString()));
                            }
                        }
                        return authorities;
                    }
                    return Collections.<GrantedAuthority>emptyList();
                })
                .orElse(Collections.emptyList());
    }

    /**
     * Validates JWT token and logs errors.
     */
    public boolean validateJwtToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("JWT token malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("JWT signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token illegal argument: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Removes Bearer prefix and parses claims.
     */
    private Optional<Claims> getClaims(String token) {
        String cleanedToken = removeBearerPrefix(token);
        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(cleanedToken)
                    .getBody());
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Failed to parse JWT claims: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Removes "Bearer " prefix from token if present.
     */
    public static String removeBearerPrefix(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
