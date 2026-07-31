package com.api.auth_service.api.config;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.LoginRequest;
import com.api.auth_service.api.models.request.UserRequest;
import io.restassured.response.Response;

public class TestContext {

    public static String testLogin;
    public static String testEmail;
    public static String testPassword;
    public static Long testUserId;
    public static String accessToken;
    public static String refreshToken;

    /**
     * Создает пользователя и сохраняет данные в контексте
     */
    public static void createUser() {
        if (testUserId != null) {
            return; // уже создан
        }

        testLogin = TestData.generateUniqueLogin();
        testEmail = TestData.generateUniqueEmail();
        testPassword = TestData.NEW_PASSWORD;

        UserRequest request = UserRequest.builder()
                .login(testLogin)
                .email(testEmail)
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        Response response = ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .extract()
                .response();

        testUserId = response.jsonPath().getLong("user.userId");
        accessToken = response.jsonPath().getString("accessToken");
        refreshToken = response.jsonPath().getString("refreshToken");

        System.out.println("✅ Тестовый пользователь создан: ID=" + testUserId);
    }

    /**
     * Логинится и получает токены
     */
    public static void login() {
        if (accessToken != null && refreshToken != null) {
            return;
        }

        if (testLogin == null) {
            createUser();
        }

        LoginRequest request = LoginRequest.builder()
                .loginOrEmail(testLogin)
                .password(testPassword)
                .build();

        Response response = ApiClient.given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        accessToken = response.jsonPath().getString("accessToken");
        refreshToken = response.jsonPath().getString("refreshToken");

        System.out.println("✅ Токены получены");
    }

    /**
     * Очистка — удаляет пользователя из БД
     */
    public static void cleanup() {
        if (testUserId != null && DatabaseClient.userExists(testUserId)) {
            DatabaseClient.deleteUser(testUserId);
            System.out.println("🧹 Тестовый пользователь удален: ID=" + testUserId);
        }
        clear();
    }

    public static void clear() {
        testLogin = null;
        testEmail = null;
        testPassword = null;
        testUserId = null;
        accessToken = null;
        refreshToken = null;
    }
}