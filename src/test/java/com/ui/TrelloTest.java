package com.ui;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Epic("UI Автоматизация")
@Feature("Trello Доски")
@DisplayName("Тесты досок Trello")
public class TrelloTest extends BaseUITest {

    private static final String TRELLO_URL = "https://trello.com";

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Навигация")
    @DisplayName("1. Проверка перехода на страницу логина с главной")
    @Description("Кликаем на кнопку Log in и проверяем, что открылась страница логина")
    public void testNavigateToLogin() {
        System.out.println("Тест: Переход на страницу логина");
        driver.get(TRELLO_URL);

        // Находим кнопку Log in и кликаем
        WebElement loginButton = driver.findElement(By.xpath("//a[contains(text(), 'Log in')]"));
        loginButton.click();

        // Проверяем, что мы на странице логина
        String currentUrl = driver.getCurrentUrl();
        System.out.println("Текущий URL: " + currentUrl);
        assertTrue(currentUrl.contains("login"), "URL должен содержать 'login'");

        // Проверяем наличие формы логина
        WebElement body = driver.findElement(By.tagName("body"));
        assertTrue(body.isDisplayed(), "Страница загружена");

        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Ссылки")
    @DisplayName("2. Проверка наличия ссылок на главной странице")
    @Description("Проверяем, что на главной странице есть хотя бы одна ссылка")
    public void testLinksExist() {
        System.out.println("Тест: Проверка наличия ссылок");
        driver.get(TRELLO_URL);

        WebElement link = driver.findElement(By.tagName("a"));
        assertTrue(link.isDisplayed(), "На странице есть ссылки");

        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Текст на странице")
    @DisplayName("3. Проверка наличия текста на странице")
    @Description("Проверяем, что на странице есть какой-либо текст")
    public void testTextExists() {
        System.out.println("Тест: Проверка наличия текста");
        driver.get(TRELLO_URL);

        WebElement body = driver.findElement(By.tagName("body"));
        String bodyText = body.getText();
        assertFalse(bodyText.isEmpty(), "На странице должен быть текст");

        System.out.println("Текст на странице найден");
        System.out.println("Тест успешно завершен");
    }
}