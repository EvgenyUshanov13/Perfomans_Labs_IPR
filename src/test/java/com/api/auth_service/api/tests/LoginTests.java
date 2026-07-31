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
@Feature("Авторизация")
@DisplayName("Тесты логина пользователя")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTests {

    private static String testLogin;
    private static String testPassword;
    private static Long testUserId;

    @BeforeAll
    static void createTestUser() {
        testLogin = TestData.generateUniqueLogin();
        testPassword = TestData.NEW_PASSWORD;
        String email = TestData.generateUniqueEmail();

        UserRequest request = UserRequest.builder()
                .login(testLogin)
                .email(email)
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_ADMIN_ID)
                .positionId(TestData.POSITION_ADMIN_ID)
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
        System.out.println("Создан тестовый пользователь: " + testLogin);
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
    @Severity(SeverityLevel.BLOCKER)
    @Story("Успешный вход")
    @DisplayName("1. Логин с валидными данными")
    @Description("Проверяем успешный вход с валидными логином и паролем. Ожидаем получение токенов и данных пользователя.")
    void login_success() {
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

        assertThat(response.jsonPath().getString("accessToken")).isNotBlank();
        assertThat(response.jsonPath().getString("refreshToken")).isNotBlank();
        assertThat(response.jsonPath().getString("user.login")).isEqualTo(testLogin);

        System.out.println("Логин успешен");
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("2. Неверный пароль")
    @Description("Техника: Классы эквивалентности. Проверяем, что при неверном пароле возвращается 400.")
    void login_wrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .loginOrEmail(testLogin)
                .password(TestData.WRONG_PASSWORD)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("3. Несуществующий логин")
    @Description("Техника: Классы эквивалентности. Проверяем, что при несуществующем логине возвращается 400.")
    void login_userNotFound() {
        LoginRequest request = LoginRequest.builder()
                .loginOrEmail(TestData.NON_EXISTENT_LOGIN)
                .password(TestData.NEW_PASSWORD)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.MINOR)
    @Story("Негативные сценарии")
    @DisplayName("4. Пустой логин")
    @Description("Техника: Граничные значения. Проверяем, что при пустом логине возвращается 400.")
    void login_emptyLogin() {
        LoginRequest request = LoginRequest.builder()
                .loginOrEmail("")
                .password(testPassword)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.MINOR)
    @Story("Негативные сценарии")
    @DisplayName("5. Пустой пароль")
    @Description("Техника: Граничные значения. Проверяем, что при пустом пароле возвращается 400.")
    void login_emptyPassword() {
        LoginRequest request = LoginRequest.builder()
                .loginOrEmail(testLogin)
                .password("")
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/login")
                .then()
                .statusCode(400);
    }
}