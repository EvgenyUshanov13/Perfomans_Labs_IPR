@echo off
title IPR - Запуск UI автотестов
chcp 65001 >nul
echo ==========================================
echo   Запуск UI автотестов
echo ==========================================
echo.
echo [1/3] Очистка и запуск UI тестов...
mvn clean test -Dtest="SimpleTest,TrelloLoginTest,TrelloTest"
echo.
echo [2/3] Генерация Allure отчета...
mvn allure:report
echo.
echo [3/3] Открытие Allure отчета в браузере...
if exist "target\allure-report\index.html" (
    start "" "target\allure-report\index.html"
    echo Отчет открыт!
) else (
    echo Отчет не найден. Проверь папку target/allure-report
)
echo.
echo ==========================================
echo   UI тесты выполнены! Отчет открыт!
echo ==========================================
pause