package com.api.auth_service.api.tests;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.config.TestContext;
import com.api.auth_service.api.config.TestData;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.UserRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Тестирование")
@Feature("Регистрация")
@DisplayName("Тесты регистрации пользователя")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterTests {

    private static String testLogin;
    private static String testEmail;
    private static String testPassword;
    private static Long createdUserId;

    @BeforeAll
    static void generateTestData() {
        testLogin = TestData.generateUniqueLogin();
        testEmail = TestData.generateUniqueEmail();
        testPassword = TestData.NEW_PASSWORD;
    }

    @AfterEach
    void cleanUp() {
        if (createdUserId != null && DatabaseClient.userExists(createdUserId)) {
            DatabaseClient.deleteUser(createdUserId);
            System.out.println("Пользователь " + createdUserId + " удален из БД");
            createdUserId = null;
        }
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.BLOCKER)
    @Story("Успешная регистрация")
    @DisplayName("1. Регистрация с валидными данными")
    @Description("Проверяем успешную регистрацию с валидными данными. Ожидаем получение токенов и сохранение данных в БД.")
    void register_success() {
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

        String accessToken = response.jsonPath().getString("accessToken");
        String refreshToken = response.jsonPath().getString("refreshToken");
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        Long userId = response.jsonPath().getLong("user.userId");
        assertThat(response.jsonPath().getString("user.login")).isEqualTo(testLogin);
        assertThat(response.jsonPath().getString("user.email")).isEqualTo(testEmail);

        createdUserId = userId;

        Map<String, Object> dbUser = DatabaseClient.getUser(userId);
        assertThat(dbUser.get("login")).isEqualTo(testLogin);
        assertThat(dbUser.get("email")).isEqualTo(testEmail);

        TestContext.testLogin = testLogin;
        TestContext.testEmail = testEmail;
        TestContext.testPassword = testPassword;
        TestContext.testUserId = userId;
        TestContext.accessToken = accessToken;
        TestContext.refreshToken = refreshToken;

        System.out.println("Регистрация успешна: ID=" + userId);
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("2. Пустой логин")
    @Description("Техника: Классы эквивалентности. Проверяем, что при пустом логине возвращается 400 Bad Request.")
    void register_emptyLogin() {
        UserRequest request = UserRequest.builder()
                .login("")
                .email(TestData.generateUniqueEmail())
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("3. Невалидный email")
    @Description("Техника: Классы эквивалентности. Проверяем, что при невалидном email возвращается 400 Bad Request.")
    void register_invalidEmail() {
        UserRequest request = UserRequest.builder()
                .login(TestData.generateUniqueLogin())
                .email(TestData.INVALID_EMAIL)
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("4. Пароль менее 8 символов")
    @Description("Техника: Граничные значения. Проверяем, что при пароле менее 8 символов возвращается 400 Bad Request.")
    void register_shortPassword() {
        UserRequest request = UserRequest.builder()
                .login(TestData.generateUniqueLogin())
                .email(TestData.generateUniqueEmail())
                .password(TestData.SHORT_PASSWORD)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("5. Дубликат логина")
    @Description("Техника: Предугадывание ошибки. Проверяем, что при попытке регистрации с существующим логином возвращается 400 Bad Request.")
    void register_duplicateLogin() {
        if (createdUserId == null) {
            register_success();
        }

        UserRequest request = UserRequest.builder()
                .login(testLogin)
                .email(TestData.generateUniqueEmail())
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(6)
    @Severity(SeverityLevel.NORMAL)
    @Story("Негативные сценарии")
    @DisplayName("6. Дубликат email")
    @Description("Техника: Предугадывание ошибки. Проверяем, что при попытке регистрации с существующим email возвращается 400 Bad Request.")
    void register_duplicateEmail() {
        if (createdUserId == null) {
            register_success();
        }

        UserRequest request = UserRequest.builder()
                .login(TestData.generateUniqueLogin())
                .email(testEmail)
                .password(testPassword)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        ApiClient.given()
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }
}