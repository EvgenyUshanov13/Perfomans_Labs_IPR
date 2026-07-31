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
@Feature("Валидация токена")
@DisplayName("Тесты валидации токена")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidateTests {

    private static String testLogin;
    private static String testPassword;
    private static Long testUserId;
    private static String accessToken;

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

        accessToken = loginResponse.jsonPath().getString("accessToken");
        System.out.println("Получен access-токен");
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
    @Story("Успешная валидация")
    @DisplayName("1. Валидация валидного access токена")
    @Description("Проверяем валидацию корректного access-токена. Ожидаем статус 200 OK и данные пользователя.")
    void validate_success() {
        assertThat(accessToken).isNotBlank();

        Response response = ApiClient.given()
                .body("{\"accessToken\": \"" + accessToken + "\"}")
                .when()
                .post("/validate")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getBoolean("valid")).isTrue();
        assertThat(response.jsonPath().getLong("userId")).isEqualTo(testUserId);
        assertThat(response.jsonPath().getString("login")).isEqualTo(testLogin);

        System.out.println("Токен валиден");
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("2. Валидация невалидного токена")
    @Description("Техника: Предугадывание ошибки. Проверяем, что невалидный токен возвращает valid=false.")
    void validate_invalidToken() {
        Response response = ApiClient.given()
                .body("{\"accessToken\": \"invalid-token-123\"}")
                .when()
                .post("/validate")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getBoolean("valid")).isFalse();

        String error = response.jsonPath().getString("error");
        if (error != null) {
            assertThat(error).isNotBlank();
        }

        System.out.println("Невалидный токен распознан");
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.MINOR)
    @Story("Негативные сценарии")
    @DisplayName("3. Валидация с пустым токеном")
    @Description("Техника: Граничные значения. Проверяем, что при пустом токене возвращается 400 Bad Request.")
    void validate_emptyToken() {
        ApiClient.given()
                .body("{\"accessToken\": \"\"}")
                .when()
                .post("/validate")
                .then()
                .statusCode(400);
    }
}