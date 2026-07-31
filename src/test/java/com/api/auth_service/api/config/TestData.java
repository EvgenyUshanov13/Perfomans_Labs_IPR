package com.api.auth_service.api.config;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TestData {

    // ========== БАЗОВЫЙ URL ==========
    public static final String BASE_URL = "http://localhost:8081";
    public static final String BASE_PATH = "/accrefull/v1";

    // ========== СУЩЕСТВУЮЩИЕ ПОЛЬЗОВАТЕЛИ (для тестов) ==========
    public static final String ADMIN_LOGIN = "administrator";
    public static final String ADMIN_PASSWORD = "administrator";
    public static final String ADMIN_EMAIL = "administrator@accrefull.com";

    public static final String MANAGER_LOGIN = "manager";
    public static final String MANAGER_PASSWORD = "manager123";
    public static final String MANAGER_EMAIL = "manager@example.com";

    public static final String USER_LOGIN = "tester";
    public static final String USER_PASSWORD = "Tester123";
    public static final String USER_EMAIL = "tester@example.com";

    // ========== ТЕСТОВЫЕ ДАННЫЕ ДЛЯ РЕГИСТРАЦИИ ==========
    public static final String NEW_LOGIN_PREFIX = "autotest_user_";
    public static final String NEW_EMAIL_PREFIX = "autotest_";
    public static final String NEW_EMAIL_DOMAIN = "@test.accrefull.com";
    public static final String NEW_PASSWORD = "TestPass123!";
    public static final String NEW_FIRST_NAME = "Test";
    public static final String NEW_LAST_NAME = "User";
    public static final String NEW_MIDDLE_NAME = "Auto";

    // ========== НЕГАТИВНЫЕ ДАННЫЕ ==========
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String SHORT_PASSWORD = "123";
    public static final String NON_EXISTENT_LOGIN = "nonexistent_user_999";
    public static final String WRONG_PASSWORD = "WrongPass123!";

    // ========== ДАННЫЕ ДЛЯ СМЕНЫ ПАРОЛЯ ==========
    public static final String NEW_PASSWORD_FOR_CHANGE = "NewSecurePass456!";
    public static final String RESET_CODE = "123456";

    // ========== ID КОМАНД (из БД) ==========
    public static final Long TEAM_ADMIN_ID = 1L;
    public static final Long TEAM_MANAGER_ID = 2L;
    public static final Long TEAM_USER_ID = 3L;

    // ========== ID ДОЛЖНОСТЕЙ (из БД) - ИСПОЛЬЗУЕМ ТОЛЬКО СУЩЕСТВУЮЩИЕ ==========
    public static final Long POSITION_ADMIN_ID = 1L;
    public static final Long POSITION_MANAGER_ID = 2L;
    public static final Long POSITION_USER_ID = 3L;
    public static final Long POSITION_TEST_ID = 3L;  // Используем существующий ID

    // ========== УТИЛИТНЫЕ МЕТОДЫ ==========
    public static synchronized String generateUniqueLogin() {
        // Добавляем случайное число для гарантии уникальности
        return NEW_LOGIN_PREFIX + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    public static synchronized String generateUniqueEmail() {
        return NEW_EMAIL_PREFIX + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000, 9999) + NEW_EMAIL_DOMAIN;
    }
}