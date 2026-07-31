<<<<<<< HEAD
client/ApiClient.java - Базовый HTTP-клиент на RestAssured. Содержит методы given(), givenWithAuth(), givenWithRefreshToken().

config/TestData.java - Хранит тестовые данные: логины, пароли, email, ID команд, константы для негативных тестов.

models/request/LoginRequest.java - DTO для запроса логина (login, password).

models/request/RefreshRequest.java - DTO для запроса обновления токена (refreshToken).

models/request/UserRequest.java - DTO для регистрации/создания пользователя (login, email, password, teamId).

models/response/LoginResponse.java - DTO для ответа логина/регистрации (userId, login, email, teamId, accessToken, refreshToken).

models/response/RefreshResponse.java - DTO для ответа обновления токенов (accessToken, refreshToken).

models/response/ValidateTokenResponse.java - DTO для ответа валидации токена (valid, error, userId, login, teamId).

specs/Specifications.java - Общие спецификации для запросов (базовый URL, basePath, Content-Type, логирование). Можно расширить для разных ролей (admin, manager, user).

tests/AuthTests.java - Тесты для AuthController (регистрация, логин, рефреш, валидация, логаут).

tests/UserTests.java - Тесты для UserController (CRUD пользователей с проверкой прав доступа).

tests/TeamTests.java - Тесты для TeamController (CRUD команд с проверкой прав).

tests/PositionTests.java - Тесты для PositionController (CRUD должностей с проверкой прав).

resources/allure.properties - Настройки Allure (путь к отчетам, кодировка).

pom.xml - Все зависимости: RestAssured, Jackson, Lombok, Allure, AssertJ, JUnit 5.

db/DatabaseClient.java - Файл для работы с бд
=======
# Perfomans_Labs_IPR
ИПР по автотестированию Api и Ui кейсы + загрузка результата в гит после получения успешного аллюра
>>>>>>> 0c34c6a5eb236797718db55e502e91adadfe686d
