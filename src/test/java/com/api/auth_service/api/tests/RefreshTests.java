package com.api.auth_service.api.tests;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.config.TestData;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.LoginRequest;
import com.api.auth_service.api.models.request.RefreshRequest;
import com.api.auth_service.api.models.request.UserRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Тестирование")
@Feature("Обновление токенов")
@DisplayName("Тесты обновления токенов")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RefreshTests {

    private static String testLogin;
    private static String testPassword;
    private static Long testUserId;
    private static String refreshToken;

    @BeforeAll
    static void createAndLogin() {
        testLogin = TestData.generateUniqueLogin();
        testPassword = TestData.NEW_PASSWORD;
        String email = TestData.generateUniqueEmail();

        UserRequest registerRequest = UserRequest.builder()
                .login(testLogin)
                .email(email)
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_ADMIN_ID)
                .positionId(TestData.POSITION_ADMIN_ID)
                .build();

        Response registerResponse = ApiClient.given()
                .body(registerRequest)
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .extract()
                .response();

        testUserId = registerResponse.jsonPath().getLong("user.userId");
        System.out.println("Создан тестовый пользователь: " + testLogin);

        LoginRequest loginRequest = LoginRequest.builder()
                .loginOrEmail(testLogin)
                .password(testPassword)
                .build();

        Response loginResponse = ApiClient.given()
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        refreshToken = loginResponse.jsonPath().getString("refreshToken");
        System.out.println("Получен refresh-токен");
    }

    @AfterAll
    static void deleteTestUser() {
        if (testUserId != null && DatabaseClient.userExists(testUserId)) {
            DatabaseClient.deleteUser(testUserId);
            System.out.println("Удален тестовый пользователь: " + testLogin);
        }
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Успешное обновление")
    @DisplayName("1. Обновление токенов с валидным refresh токеном")
    @Description("Проверяем успешное обновление токенов с валидным refresh-токеном. Ожидаем получение новой пары токенов.")
    void refresh_success() {
        assertThat(refreshToken).isNotBlank();

        RefreshRequest request = RefreshRequest.builder()
                .refreshToken(refreshToken)
                .build();

        Response response = ApiClient.given()
                .body(request)
                .when()
                .post("/refresh")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getString("accessToken")).isNotBlank();
        assertThat(response.jsonPath().getString("refreshToken")).isNotBlank();

        System.out.println("Токены обновлены");
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("2. Невалидный refresh токен")
    @Description("Техника: Предугадывание ошибки. Проверяем, что невалидный refresh-токен возвращает 400 Bad Request.")
    void refresh_invalidToken() {
        RefreshRequest request = RefreshRequest.builder()
                .refreshToken("invalid-refresh-token-123")
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/refresh")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.MINOR)
    @Story("Негативные сценарии")
    @DisplayName("3. Пустой refresh токен")
    @Description("Техника: Граничные значения. Проверяем, что пустой refresh-токен возвращает 400 Bad Request.")
    void refresh_emptyToken() {
        RefreshRequest request = RefreshRequest.builder()
                .refreshToken("")
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/refresh")
                .then()
                .statusCode(400);
    }
}