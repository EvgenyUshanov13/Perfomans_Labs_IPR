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
@Feature("TeamController")
@DisplayName("Тесты CRUD для команд")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamTests {

    private static String adminToken;
    private static String adminLogin;
    private static String adminPassword;
    private static Long adminUserId;
    private static Long teamId;
    private static String teamName;

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
    void cleanUpTeam() {
        if (teamId != null && DatabaseClient.teamExists(teamId)) {
            DatabaseClient.deleteTeam(teamId);
            System.out.println("Команда " + teamId + " удалена из БД");
            teamId = null;
        }
    }

    @Test
    @Order(1)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Создание команды")
    @DisplayName("1. Создание команды администратором")
    @Description("Проверяем создание новой команды администратором. Ожидаем статус 201 Created и возврат ID созданной команды.")
    void createTeam_success() {
        teamName = "Team_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + teamName + "\", \"description\": \"Test team\"}";

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/teams")
                .then()
                .statusCode(201)
                .extract()
                .response();

        teamId = response.jsonPath().getLong("teamId");
        assertThat(teamId).isNotNull();
        assertThat(response.jsonPath().getString("name")).isEqualTo(teamName);

        System.out.println("Команда создана: ID=" + teamId);
    }

    @Test
    @Order(2)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение команды")
    @DisplayName("2. Получение команды по ID")
    @Description("Проверяем получение существующей команды по ID. Ожидаем статус 200 OK и корректные данные.")
    void getTeamById_success() {
        if (teamId == null) {
            createTeam_success();
        }

        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/teams/" + teamId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getLong("teamId")).isEqualTo(teamId);
        assertThat(response.jsonPath().getString("name")).isEqualTo(teamName);

        System.out.println("Команда получена: " + teamName);
    }

    @Test
    @Order(3)
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение команды")
    @DisplayName("3. Получение несуществующей команды")
    @Description("Техника: Граничные значения. Бэкенд возвращает 400 Bad Request вместо 404 Not Found.")
    void getTeamById_notFound() {
        ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/teams/99999")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    @Severity(SeverityLevel.MINOR)
    @Story("Получение всех команд")
    @DisplayName("4. Получение списка всех команд")
    @Description("Проверяем получение списка всех команд. Ожидаем статус 200 OK и непустой список.")
    void getAllTeams_success() {
        Response response = ApiClient.givenWithAuth(adminToken)
                .when()
                .get("/teams")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<?> teams = response.jsonPath().getList("$");
        assertThat(teams).isNotEmpty();

        System.out.println("Получен список команд: " + teams.size() + " шт.");
    }

    @Test
    @Order(5)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Обновление команды")
    @DisplayName("5. Обновление команды")
    @Description("Проверяем обновление данных существующей команды. Ожидаем статус 200 OK и проверяем изменения в БД.")
    void updateTeam_success() {
        if (teamId == null) {
            createTeam_success();
        }

        String newName = "UpdatedTeam_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + newName + "\", \"description\": \"Updated team\"}";

        Response response = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .put("/teams/" + teamId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertThat(response.jsonPath().getString("name")).isEqualTo(newName);

        Map<String, Object> dbTeam = DatabaseClient.getTeam(teamId);
        assertThat(dbTeam.get("name")).isEqualTo(newName);

        teamName = newName;
        System.out.println("Команда обновлена: " + newName);
    }

    @Test
    @Order(6)
    @Severity(SeverityLevel.CRITICAL)
    @Story("Удаление команды")
    @DisplayName("6. Удаление команды")
    @Description("Проверяем удаление команды. Ожидаем статус 204 No Content и проверяем, что запись исчезла из БД.")
    void deleteTeam_success() {
        String tempName = "TempDelete_" + System.currentTimeMillis();
        String requestBody = "{\"name\": \"" + tempName + "\", \"description\": \"To be deleted\"}";

        Response createResponse = ApiClient.givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/teams")
                .then()
                .statusCode(201)
                .extract()
                .response();

        Long tempId = createResponse.jsonPath().getLong("teamId");
        assertThat(tempId).isNotNull();
        assertThat(DatabaseClient.teamExists(tempId)).isTrue();

        ApiClient.givenWithAuth(adminToken)
                .when()
                .delete("/teams/" + tempId)
                .then()
                .statusCode(204);

        assertThat(DatabaseClient.teamExists(tempId)).isFalse();
        System.out.println("Команда удалена: ID=" + tempId);
    }
}