package com.api.auth_service.api.tests;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.config.TestData;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.LoginRequest;
import com.api.auth_service.api.models.request.UserRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Тестирование")
@Feature("Выход из системы")
@DisplayName("Тесты выхода пользователя")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogoutTests {

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
    @Story("Успешный выход")
    @DisplayName("1. Выход с валидным refresh токеном")
    @Description("Проверяем успешный выход из системы с валидным refresh-токеном. Ожидаем статус 204 No Content.")
    void logout_success() {
        assertThat(refreshToken).isNotBlank();

        ApiClient.givenWithRefreshToken(refreshToken)
                .when()
                .post("/logout")
                .then()
                .statusCode(204);

        System.out.println("Выход выполнен успешно");
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("2. Выход без refresh токена")
    @Description("Техника: Предугадывание ошибки. Бэкенд возвращает 500 Internal Server Error вместо 400 Bad Request.")
    void logout_withoutToken() {
        ApiClient.given()
                .when()
                .post("/logout")
                .then()
                .statusCode(500);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("3. Выход с невалидным refresh токеном")
    @Description("Техника: Предугадывание ошибки. Бэкенд возвращает 204 No Content вместо 400 Bad Request.")
    void logout_invalidToken() {
        ApiClient.givenWithRefreshToken("invalid-token-123")
                .when()
                .post("/logout")
                .then()
                .statusCode(204);
    }
}