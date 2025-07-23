package com.example.demo3.api.testutil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo3.dto.UserLoginDTO;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.Instant;

import static io.restassured.RestAssured.given;

public class TestTokenManager {

    private record CachedToken(String value, Instant expiry) {

        boolean isExpired() {
                return Instant.now().isAfter(expiry.minusSeconds(30));
            }
        }

    private CachedToken jwtAdminToken;
    private CachedToken jwtUserToken;

    public synchronized String getAdminToken() {
        if (jwtAdminToken == null || jwtAdminToken.isExpired()) {
            String newAccessToken = authenticateUser(createAdminUser());
            Instant expiry = decodeTokenAndGetExpiry(newAccessToken);
            this.jwtAdminToken = new CachedToken(newAccessToken, expiry);
        }
        return this.jwtAdminToken.value;
    }

    public synchronized String getUserToken() {
        if (jwtUserToken == null || jwtUserToken.isExpired()) {
            String newAccessToken = authenticateUser(createRegularUser());
            Instant expiry = decodeTokenAndGetExpiry(newAccessToken);
            this.jwtUserToken = new CachedToken(newAccessToken, expiry);
        }
        return this.jwtUserToken.value;
    }

    private String authenticateUser(UserLoginDTO userCredentials) {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(userCredentials)
                .when()
                .post("/users/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String token = response.path("accessToken");

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Failed to authenticate user: " + userCredentials.getUsername());
        }

        return token;
    }

    private Instant decodeTokenAndGetExpiry(String token) {
        DecodedJWT jwt = JWT.decode(token);

        if (jwt.getExpiresAt() == null) {
            throw new JWTDecodeException("JWT does not contain an expiration time ('exp') claim.");
        }

        return jwt.getExpiresAt().toInstant();
    }

    private UserLoginDTO createAdminUser() {
        UserLoginDTO admin = new UserLoginDTO();
        admin.setUsername("Griffith");
        admin.setPassword("Griffith");
        return admin;
    }

    private UserLoginDTO createRegularUser() {
        UserLoginDTO user = new UserLoginDTO();
        user.setUsername("User");
        user.setPassword("User");
        return user;
    }
}
