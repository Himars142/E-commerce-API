package com.example.demo3.api.testutil;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class RestAssuredClient {

    public static Response makeAuthorizedPostRequest(String token, Object body, String endpoint) {
        return makeAuthorizedRequest(token, body, endpoint, Method.POST);
    }

    public static Response makeAuthorizedRequest(String token, Object body, String endpoint, Method method) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .request(method, endpoint);
    }

    public static Response makePageableGetRequest(String endpoint, Integer page, Integer size) {
        var requestSpec = given();

        if (page != null) {
            requestSpec.queryParam("page", page);
        }
        if (size != null) {
            requestSpec.queryParam("size", size);
        }

        return requestSpec
                .when()
                .get(endpoint);
    }
}
