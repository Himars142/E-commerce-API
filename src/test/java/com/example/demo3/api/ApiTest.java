package com.example.demo3.api;

import com.example.demo3.api.testutil.TestTokenManager;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class ApiTest {

    private static final TestTokenManager testTokenManager = new TestTokenManager();

    private static final String BASE_URI = "http://localhost:9999";
    private static final String BASE_PATH = "/api";
    protected static final int DEFAULT_PAGE = 0;
    protected static final int DEFAULT_SIZE = 10;
    protected static final int MAX_ALLOWED_PAGE_SIZE = 50;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.basePath = BASE_PATH;
    }

    protected static String getAdminToken() {
        return testTokenManager.getAdminToken();
    }

    protected static String getUserToken() {
        return testTokenManager.getUserToken();
    }
}
