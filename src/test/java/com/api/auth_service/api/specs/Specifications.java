package com.api.auth_service.api.specs;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class Specifications {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String BASE_PATH = "/accrefull/v1";

    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(BASE_PATH)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    public static RequestSpecification authSpec(String accessToken) {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(BASE_PATH)
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + accessToken)
                .log(LogDetail.ALL)
                .build();
    }

    public static RequestSpecification refreshSpec(String refreshToken) {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(BASE_PATH)
                .setContentType(ContentType.JSON)
                .addHeader("X-Refresh-Token", refreshToken)
                .log(LogDetail.ALL)
                .build();
    }

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}