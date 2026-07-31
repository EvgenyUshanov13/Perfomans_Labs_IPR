package com.ui;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("UI Автоматизация")
@Feature("Проверка инфраструктуры")
@DisplayName("Простые проверочные тесты")
public class SimpleTest {

    @Test
    @Severity(SeverityLevel.TRIVIAL)
    @DisplayName("Проверка работы JUnit")
    @Description("Простой тест для проверки, что JUnit работает корректно")
    public void testSimple() {
        System.out.println("Проверка работы JUnit");
        assertTrue(true);
        System.out.println("Тест успешно завершен");
    }
}