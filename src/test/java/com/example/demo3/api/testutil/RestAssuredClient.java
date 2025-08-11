package com.example.demo3.api.testutil;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

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

    public static Response makeAuthorizedRequest(String token, String endpoint, Method method) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .request(method, endpoint);
    }

    public static Response makeRequestWithoutAuthorizationHeader(Object body, String endpoint, Method method) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .request(method, endpoint);
    }

    public static Response makePageableGetRequest(String endpoint, Integer page, Integer size) {
        RequestSpecification requestSpec = given();

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

    public static Response makeGetRequestWithIdParam(String endpoint, Long id) {
        return makeRequestWithId(endpoint, id, Method.GET);
    }

    public static Response makePostRequestWithIdParam(String token, String endpoint, Long id) {
        endpoint = endpoint + "/" + id.toString();
        return makeAuthorizedRequest(token, endpoint, Method.POST);
    }

    public static Response makeDeleteRequestWithIdParam(String token, String endpoint, Long id) {
        endpoint = endpoint + "/" + id.toString();
        return makeAuthorizedRequest(token, endpoint, Method.DELETE);
    }

    public static Response makeRequestWithId(String endpoint, Long id, Method method) {
        endpoint = endpoint + "/" + id.toString();
        return given()
                .when()
                .request(method, endpoint);
    }

}
