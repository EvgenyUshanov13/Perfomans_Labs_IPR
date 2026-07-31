@echo off
title IPR - Запуск тестов и Allure отчета
chcp 65001 >nul
echo ==========================================
echo   Запуск API и UI автотестов
echo ==========================================
echo.
echo [1/3] Очистка и запуск тестов...
call mvn clean test
echo.
echo [2/3] Генерация Allure отчета...
call mvn allure:report
echo.
echo [3/3] Запуск Allure сервера и открытие отчета...
start "" cmd /k "mvn allure:serve"
echo.
echo ==========================================
echo   Все тесты выполнены! Отчет открывается!
echo ==========================================
pause