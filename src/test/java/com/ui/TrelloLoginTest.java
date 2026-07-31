package com.ui;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Epic("UI Автоматизация")
@Feature("Trello Авторизация")
@DisplayName("Тесты страницы логина Trello")
public class TrelloLoginTest extends BaseUITest {

    private static final String TRELLO_URL = "https://trello.com";
    private static final String LOGIN_URL = "https://trello.com/login";

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Загрузка страницы логина")
    @DisplayName("1. Проверка загрузки страницы логина")
    @Description("Открываем страницу логина Trello и проверяем, что она загружена корректно")
    public void testTrelloLoginPageLoad() {
        System.out.println("Тест: Проверка загрузки страницы логина");
        driver.get(LOGIN_URL);

        String pageTitle = driver.getTitle();
        System.out.println("Заголовок страницы: " + pageTitle);
        assertTrue(pageTitle.contains("Atlassian") || pageTitle.contains("Log in"),
                "Заголовок должен содержать 'Atlassian' или 'Log in'");

        WebElement body = driver.findElement(By.tagName("body"));
        assertTrue(body.isDisplayed(), "Страница должна быть загружена");

        boolean formFound = false;

        try {
            WebElement loginForm = driver.findElement(By.id("login"));
            if (loginForm.isDisplayed()) {
                formFound = true;
                System.out.println("Форма логина найдена по id 'login'");
            }
        } catch (Exception e) {
            // ignored
        }

        if (!formFound) {
            try {
                WebElement loginForm = driver.findElement(By.cssSelector("[data-testid='login-form']"));
                if (loginForm.isDisplayed()) {
                    formFound = true;
                    System.out.println("Форма логина найдена по data-testid");
                }
            } catch (Exception e) {
                // ignored
            }
        }

        if (!formFound) {
            try {
                WebElement loginForm = driver.findElement(By.className("login-form"));
                if (loginForm.isDisplayed()) {
                    formFound = true;
                    System.out.println("Форма логина найдена по классу");
                }
            } catch (Exception e) {
                // ignored
            }
        }

        if (!formFound) {
            try {
                WebElement emailField = driver.findElement(By.id("username"));
                if (emailField.isDisplayed()) {
                    formFound = true;
                    System.out.println("Поле для email найдено");
                }
            } catch (Exception e) {
                // ignored
            }
        }

        if (!formFound) {
            try {
                WebElement input = driver.findElement(By.tagName("input"));
                if (input.isDisplayed()) {
                    formFound = true;
                    System.out.println("Найден input элемент на странице");
                }
            } catch (Exception e) {
                // ignored
            }
        }

        assertTrue(formFound, "Форма логина или поле ввода должны быть найдены");
        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Заголовок главной страницы")
    @DisplayName("2. Проверка заголовка главной страницы Trello")
    @Description("Открываем главную страницу Trello и проверяем, что заголовок не пустой")
    public void testTrelloTitle() {
        System.out.println("Тест: Проверка заголовка главной страницы");
        driver.get(TRELLO_URL);
        String title = driver.getTitle();
        System.out.println("Заголовок страницы: " + title);
        assertFalse(title.isEmpty(), "Заголовок не должен быть пустым");
        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Кнопка входа")
    @DisplayName("3. Проверка наличия кнопки входа на главной странице")
    @Description("Проверяем, что на главной странице есть кнопка или ссылка Log in")
    public void testTrelloSignUpButton() {
        System.out.println("Тест: Проверка наличия кнопки входа");
        driver.get(TRELLO_URL);

        boolean buttonFound = false;

        try {
            WebElement loginButton = driver.findElement(By.xpath("//a[contains(text(), 'Log in')]"));
            if (loginButton.isDisplayed()) {
                buttonFound = true;
                System.out.println("Кнопка входа найдена: " + loginButton.getText());
            }
        } catch (Exception e) {
            // ignored
        }

        if (!buttonFound) {
            try {
                WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Log in')]"));
                if (loginButton.isDisplayed()) {
                    buttonFound = true;
                    System.out.println("Кнопка входа найдена: " + loginButton.getText());
                }
            } catch (Exception e) {
                // ignored
            }
        }

        if (!buttonFound) {
            try {
                WebElement loginButton = driver.findElement(By.xpath("//a[contains(@href, 'login')]"));
                if (loginButton.isDisplayed()) {
                    buttonFound = true;
                    System.out.println("Кнопка входа найдена по href: " + loginButton.getText());
                }
            } catch (Exception e) {
                // ignored
            }
        }

        assertTrue(buttonFound, "Кнопка 'Log in' должна быть найдена");
        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Элементы главной страницы")
    @DisplayName("4. Проверка основных элементов главной страницы")
    @Description("Проверяем наличие body, заголовка и ссылок на главной странице")
    public void testTrelloHomePageElements() {
        System.out.println("Тест: Проверка основных элементов главной страницы");
        driver.get(TRELLO_URL);

        WebElement body = driver.findElement(By.tagName("body"));
        assertTrue(body.isDisplayed(), "Body должен отображаться");
        System.out.println("Body найден");

        try {
            WebElement heading = driver.findElement(By.tagName("h1"));
            assertTrue(heading.isDisplayed(), "Заголовок H1 должен отображаться");
            System.out.println("Заголовок H1 найден: " + heading.getText());
        } catch (Exception e) {
            System.out.println("Заголовок H1 не найден, ищем альтернативу");
            WebElement heading = driver.findElement(By.xpath("//h1 | //h2 | //h3"));
            assertTrue(heading.isDisplayed(), "Какой-либо заголовок должен отображаться");
            System.out.println("Заголовок найден");
        }

        WebElement link = driver.findElement(By.tagName("a"));
        assertTrue(link.isDisplayed(), "На странице есть ссылки");
        System.out.println("Ссылки найдены");

        System.out.println("Тест успешно завершен");
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Загрузка главной страницы")
    @DisplayName("5. Проверка загрузки главной страницы")
    @Description("Открываем главную страницу и проверяем, что она загружена")
    public void testMainPageLoad() {
        System.out.println("Тест: Проверка загрузки главной страницы");
        driver.get(TRELLO_URL);

        String title = driver.getTitle();
        System.out.println("Заголовок: " + title);
        assertTrue(title != null && !title.isEmpty(), "Страница загружена");

        WebElement body = driver.findElement(By.tagName("body"));
        assertTrue(body.isDisplayed(), "Body отображается");

        System.out.println("Тест успешно завершен");
    }
}