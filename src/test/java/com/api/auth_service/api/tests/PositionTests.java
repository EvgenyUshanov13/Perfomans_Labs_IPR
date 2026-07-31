package com.api.auth_service.api.tests;

import com.api.auth_service.api.client.ApiClient;
import com.api.auth_service.api.config.TestData;
import com.api.auth_service.api.db.DatabaseClient;
import com.api.auth_service.api.models.request.LoginRequest;
import com.api.auth_service.api.models.request.UserRequest;
import io.qameta.allure.*;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Тестирование")
@Feature("PositionController")
@DisplayName("Тесты CRUD для должностей")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PositionTests {

    private static String adminToken;
    private static String adminLogin;
    private static String adminPassword;
    private static Long adminUserId;
    private static Long positionId;
    private static String positionName;

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
    void cleanUpPosition() {
        if (positionId != null && DatabaseClient.positionExists(positionId)) {
            DatabaseClient.deletePosition(positionId);
            System.out.println("Должность " + positionId + " удалена из БД");
            positionId = null;
        }
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Создание должности")
    @DisplayName("1. Создание должности администратором")
    @Description("Проверяем создание новой должности администратором.")
    void createPosition_success() {
        positionName = "QA Engineer_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + positionName + "\", \"description\": \"Test position\"}";

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/positions")
                .then()
                .statusCode(201)
                .extract()
                .response();

        positionId = response.jsonPath().getLong("positionId");
        assertThat(positionId).isNotNull();
        assertThat(response.jsonPath().getString("name")).isEqualTo(positionName);

        System.out.println("Должность создана: ID=" + positionId);
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение должности")
    @DisplayName("2. Получение должности по ID")
    @Description("Проверяем получение существующей должности по ID.")
    void getPositionById_success() {
        if (positionId == null) {
            createPosition_success();
        }

        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/positions/" + positionId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getLong("positionId")).isEqualTo(positionId);
        assertThat(response.jsonPath().getString("name")).isEqualTo(positionName);

        System.out.println("Должность получена: " + positionName);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение должности")
    @DisplayName("3. Получение несуществующей должности")
    @Description("Техника: Граничные значения. Бэкенд возвращает 400 вместо 404.")
    void getPositionById_notFound() {
        ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/positions/99999")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.MINOR)
    @Story("Получение всех должностей")
    @DisplayName("4. Получение списка всех должностей")
    @Description("Проверяем получение списка всех должностей. Ожидаем статус 200 OK и непустой список.")
    void getAllPositions_success() {
        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/positions")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<?> positions = response.jsonPath().getList("$");
        assertThat(positions).isNotEmpty();

        System.out.println("Получен список должностей: " + positions.size() + " шт.");
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обновление должности")
    @DisplayName("5. Обновление должности")
    @Description("Проверяем обновление данных существующей должности. Ожидаем статус 200 OK и проверяем изменения в БД.")
    void updatePosition_success() {
        if (positionId == null) {
            createPosition_success();
        }

        String newName = "Senior QA_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + newName + "\", \"description\": \"Updated position\"}";

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .put("/positions/" + positionId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getString("name")).isEqualTo(newName);

        Map<String, Object> dbPosition = DatabaseClient.getPosition(positionId);
        assertThat(dbPosition.get("name")).isEqualTo(newName);

        positionName = newName;
        System.out.println("Должность обновлена: " + newName);
    }

    @Test
    @Order(6)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Удаление должности")
    @DisplayName("6. Удаление должности")
    @Description("Проверяем удаление должности. Ожидаем статус 204 No Content и проверяем, что запись исчезла из БД.")
    void deletePosition_success() {
        String tempName = "TempDelete_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + tempName + "\", \"description\": \"To be deleted\"}";

        Response createResponse = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/positions")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long tempId = createResponse.jsonPath().getLong("positionId");
        assertThat(tempId).isNotNull();
        assertThat(DatabaseClient.positionExists(tempId)).isTrue();

        ApiClient.givenWithAuth(adminToken)
                .when()
                .delete("/positions/" + tempId)
                .then()
                .statusCode(204);

        assertThat(DatabaseClient.positionExists(tempId)).isFalse();
        System.out.println("Должность удалена: ID=" + tempId);
    }
}