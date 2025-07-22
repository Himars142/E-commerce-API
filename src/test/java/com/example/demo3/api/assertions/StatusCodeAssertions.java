package com.example.demo3.api.assertions;

import io.restassured.response.Response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StatusCodeAssertions {

    public static void assertCreated(Response response) {
        assertThat(response.statusCode()).isEqualTo(201);
    }

    public static void assertOk(Response response) {
        assertThat(response.statusCode()).isEqualTo(200);
    }

    public static void assertForbidden(Response response) {
        assertThat(response.getStatusCode()).isEqualTo(403);
    }

    public static void assertBadRequest(Response response) {
        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    public static void assertNotFound(Response response) {
        assertThat(response.getStatusCode()).isEqualTo(404);
    }
}
