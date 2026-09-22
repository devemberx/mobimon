@echo off
setlocal
title CSTDe AVD Setup
chcp 65001 >nul

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_avd.ps1"

if errorlevel 1 (
    echo.
    echo [ERROR] Setup failed.
    echo.
)

pause
exit /b %ERRORLEVEL%
