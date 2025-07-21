package com.example.demo3.service.impl.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {
    protected static final String USERNAME = "username";
    protected static final String ANOTHER_USER_USERNAME = "another-user-username";
    protected static final String USER_AGENT = "testing";
    protected static final String TOKEN = "valid-token";
    protected static final String TOKEN_INVALID = "invalid-token";
    protected static final String REFRESH_TOKEN = "refreshToken";
    protected static final String ACCESS_TOKEN = "accessToken";
    protected static final int DEFAULT_PAGE = 0;
    protected static final int DEFAULT_PAGE_SIZE = 10;
    protected static final Long EXISTING_ENTITY_ID = 1L;
    protected static final Long ANOTHER_EXISTING_ENTITY_ID = 2L;
    protected static final Long NOT_EXISTING_ENTITY_ID = 3L;
    protected static String requestId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID().toString();
    }
}
