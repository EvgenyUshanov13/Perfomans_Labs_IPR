package com.api.auth_service.api.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8081";
    private static final String BASE_PATH = "/accrefull/v1";

    // Настройка ObjectMapper для игнорирования неизвестных полей
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final RestAssuredConfig config = RestAssuredConfig.config()
            .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                    (cls, charset) -> objectMapper
            ));

    public static RequestSpecification given() {
        return RestAssured.given()
                .config(config)
                .baseUri(BASE_URL)
                .basePath(BASE_PATH)
                .contentType(ContentType.JSON)
                .log().uri()
                .log().body()
                .log().headers();
    }

    public static RequestSpecification givenWithAuth(String accessToken) {
        return given()
                .header("Authorization", "Bearer " + accessToken);
    }

    public static RequestSpecification givenWithRefreshToken(String refreshToken) {
        return given()
                .header("X-Refresh-Token", refreshToken);
    }
}