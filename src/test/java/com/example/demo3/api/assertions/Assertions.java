package com.example.demo3.api.assertions;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.example.demo3.api.assertions.StatusCodeAssertions.assertBadRequest;
import static com.example.demo3.api.assertions.StatusCodeAssertions.assertForbidden;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Assertions {
    public static void assertValidationError(Response response, String field, String expectedMessage) {
        assertBadRequest(response);
        response.then()
                .contentType(ContentType.JSON);
        String responseBody = response.asString();
        assertThat(responseBody).containsIgnoringCase(field);
        if (expectedMessage != null) {
            assertThat(responseBody).containsIgnoringCase(expectedMessage);
        }
    }

    public static void assertUserIsNotAdmin(Response response) {
        assertForbidden(response);
        assertThat(response.asString()).contains("You must be admin! Request id:");
    }
}
