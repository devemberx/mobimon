@echo off
setlocal

cd /d "%~dp0"

if not exist "gradlew.bat" (
    echo gradlew.bat was not found in:
    echo   %CD%
    pause
    exit /b 1
)

echo Building and installing debug APK...
call gradlew.bat :app:assembleDebug :app:installDebug

if errorlevel 1 (
    echo.
    echo Debug build or install failed.
    echo Make sure an emulator is running and C: has enough free disk space.
    pause
    exit /b 1
)

echo.
echo Debug APK installed successfully.
pause
endlocal
