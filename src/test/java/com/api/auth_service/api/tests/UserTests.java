package com.api.auth_service.api.tests;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.config.TestData;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.LoginRequest;
import com.api.auth_service.api.models.request.UserRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Тестирование")
@Feature("UserController")
@DisplayName("Тесты CRUD для пользователей")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {

    private static String adminToken;
    private static String adminLogin;
    private static String adminPassword;
    private static Long adminUserId;
    private static String testLogin;
    private static String testEmail;
    private static String testPassword;
    private static Long testUserId;

    @BeforeAll
    static void createAdminAndLogin() {
        adminLogin = TestData.generateUniqueLogin();
        adminPassword = TestData.NEW_PASSWORD;
        String email = TestData.generateUniqueEmail();

        UserRequest registerRequest = UserRequest.builder()
                .login(adminLogin)
                .email(email)
                .password(adminPassword)
                .firstName("Admin")
                .lastName("Test")
                .middleName("Auto")
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

        adminUserId = registerResponse.jsonPath().getLong("user.userId");
        System.out.println("Создан админ: " + adminLogin);

        LoginRequest loginRequest = LoginRequest.builder()
                .loginOrEmail(adminLogin)
                .password(adminPassword)
                .build();

        Response loginResponse = ApiClient.given()
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        adminToken = loginResponse.jsonPath().getString("accessToken");
        System.out.println("Администратор авторизован");
    }

    @AfterAll
    static void deleteAdmin() {
        if (adminUserId != null && DatabaseClient.userExists(adminUserId)) {
            DatabaseClient.deleteUser(adminUserId);
            System.out.println("Админ удален: " + adminLogin);
        }
    }

    @AfterEach
    void cleanUpUser() {
        if (testUserId != null && DatabaseClient.userExists(testUserId)) {
            DatabaseClient.deleteUser(testUserId);
            System.out.println("Пользователь " + testUserId + " удален из БД");
            testUserId = null;
        }
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Создание пользователя")
    @DisplayName("1. Создание пользователя администратором")
    @Description("Проверяем создание нового пользователя администратором. Ожидаем статус 201 Created и сохранение данных в БД.")
    void createUser_success() {
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

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        testUserId = response.jsonPath().getLong("userId");
        assertThat(testUserId).isNotNull();
        assertThat(response.jsonPath().getString("login")).isEqualTo(testLogin);

        Map<String, Object> dbUser = DatabaseClient.getUser(testUserId);
        assertThat(dbUser.get("login")).isEqualTo(testLogin);

        System.out.println("Пользователь создан: ID=" + testUserId);
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение пользователя")
    @DisplayName("2. Получение пользователя по ID")
    @Description("Проверяем получение существующего пользователя по ID. Ожидаем статус 200 OK и корректные данные.")
    void getUserById_success() {
        if (testUserId == null) {
            createUser_success();
        }

        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/users/" + testUserId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getLong("userId")).isEqualTo(testUserId);
        assertThat(response.jsonPath().getString("login")).isEqualTo(testLogin);

        System.out.println("Пользователь получен: " + testLogin);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение пользователя")
    @DisplayName("3. Получение несуществующего пользователя")
    @Description("Техника: Граничные значения. Бэкенд возвращает 400 Bad Request вместо 404 Not Found.")
    void getUserById_notFound() {
        ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/users/99999")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.MINOR)
    @Story("Получение всех пользователей")
    @DisplayName("4. Получение списка всех пользователей")
    @Description("Проверяем получение списка всех пользователей. Ожидаем статус 200 OK и непустой список.")
    void getAllUsers_success() {
        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<?> users = response.jsonPath().getList("$");
        assertThat(users).isNotEmpty();

        System.out.println("Получен список пользователей: " + users.size() + " шт.");
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обновление пользователя")
    @DisplayName("5. Обновление пользователя")
    @Description("Проверяем обновление данных существующего пользователя. Ожидаем статус 200 OK и проверяем изменения в БД.")
    void updateUser_success() {
        if (testUserId == null) {
            createUser_success();
        }

        String newLogin = "updated_" + testLogin;
        String newEmail = "updated_" + testEmail;

        UserRequest request = UserRequest.builder()
                .login(newLogin)
                .email(newEmail)
                .password(testPassword)
                .firstName("UpdatedName")
                .lastName("UpdatedSurname")
                .middleName("UpdatedMiddle")
                .teamId(TestData.TEAM_ADMIN_ID)
                .positionId(TestData.POSITION_ADMIN_ID)
                .build();

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(request)
                .when()
                .put("/users/" + testUserId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getString("login")).isEqualTo(newLogin);

        Map<String, Object> dbUser = DatabaseClient.getUser(testUserId);
        assertThat(dbUser.get("login")).isEqualTo(newLogin);

        testLogin = newLogin;
        testEmail = newEmail;

        System.out.println("Пользователь обновлен: " + newLogin);
    }

    @Test
    @Order(6)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Удаление пользователя")
    @DisplayName("6. Удаление пользователя")
    @Description("Проверяем удаление пользователя. Ожидаем статус 204 No Content и проверяем, что запись исчезла из БД.")
    void deleteUser_success() {
        String tempLogin = TestData.generateUniqueLogin();
        String tempEmail = TestData.generateUniqueEmail();

        UserRequest request = UserRequest.builder()
                .login(tempLogin)
                .email(tempEmail)
                .password(TestData.NEW_PASSWORD)
                .firstName(TestData.NEW_FIRST_NAME)
                .lastName(TestData.NEW_LAST_NAME)
                .middleName(TestData.NEW_MIDDLE_NAME)
                .teamId(TestData.TEAM_USER_ID)
                .positionId(TestData.POSITION_USER_ID)
                .build();

        Response createResponse = ApiClient.givenWithAuth(adminToken)
                .body(request)
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long tempUserId = createResponse.jsonPath().getLong("userId");
        assertThat(tempUserId).isNotNull();
        assertThat(DatabaseClient.userExists(tempUserId)).isTrue();

        ApiClient.givenWithAuth(adminToken)
                .when()
                .delete("/users/" + tempUserId)
                .then()
                .statusCode(204);

        assertThat(DatabaseClient.userExists(tempUserId)).isFalse();
        System.out.println("Пользователь удален: ID=" + tempUserId);
    }
}