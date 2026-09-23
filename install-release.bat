@echo off
setlocal

cd /d "%~dp0"

if not exist "gradlew.bat" (
    echo gradlew.bat was not found in:
    echo   %CD%
    pause
    exit /b 1
)

echo Building release APK...
call gradlew.bat :app:assembleRelease

if errorlevel 1 (
    echo.
    echo Release build failed.
    pause
    exit /b 1
)

set "UNSIGNED_APK=app\build\outputs\apk\release\app-release-unsigned.apk"
set "ALIGNED_APK=app\build\outputs\apk\release\app-release-local-aligned.apk"
set "SIGNED_APK=app\build\outputs\apk\release\app-release-local-signed.apk"

if not exist "%UNSIGNED_APK%" (
    echo.
    echo Release APK was not found:
    echo   %UNSIGNED_APK%
    pause
    exit /b 1
)

if not defined ANDROID_HOME (
    set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
)

if not exist "%ANDROID_HOME%\build-tools" (
    echo.
    echo Android SDK build-tools were not found:
    echo   %ANDROID_HOME%\build-tools
    pause
    exit /b 1
)

for /f "delims=" %%D in ('dir /b /ad "%ANDROID_HOME%\build-tools" 2^>nul ^| sort /r') do (
    if not defined BUILD_TOOLS set "BUILD_TOOLS=%ANDROID_HOME%\build-tools\%%D"
)

if not exist "%BUILD_TOOLS%\zipalign.exe" (
    echo.
    echo zipalign.exe was not found in:
    echo   %BUILD_TOOLS%
    pause
    exit /b 1
)

if not exist "%BUILD_TOOLS%\apksigner.bat" (
    echo.
    echo apksigner.bat was not found in:
    echo   %BUILD_TOOLS%
    pause
    exit /b 1
)

set "DEBUG_KEYSTORE=%USERPROFILE%\.android\debug.keystore"

if not exist "%USERPROFILE%\.android" (
    mkdir "%USERPROFILE%\.android"
)

if not exist "%DEBUG_KEYSTORE%" (
    echo Creating debug keystore for local release installation...
    keytool -genkeypair -v -keystore "%DEBUG_KEYSTORE%" -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"

    if errorlevel 1 (
        echo.
        echo Debug keystore creation failed.
        pause
        exit /b 1
    )
)

echo Aligning release APK...
"%BUILD_TOOLS%\zipalign.exe" -f -p 4 "%UNSIGNED_APK%" "%ALIGNED_APK%"

if errorlevel 1 (
    echo.
    echo Release APK alignment failed.
    pause
    exit /b 1
)

echo Signing release APK for local installation...
call "%BUILD_TOOLS%\apksigner.bat" sign --ks "%DEBUG_KEYSTORE%" --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out "%SIGNED_APK%" "%ALIGNED_APK%"

if errorlevel 1 (
    echo.
    echo Release APK signing failed.
    pause
    exit /b 1
)

echo Installing signed release APK...
if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    "%ANDROID_HOME%\platform-tools\adb.exe" install -r "%SIGNED_APK%"
) else (
    adb install -r "%SIGNED_APK%"
)

if errorlevel 1 (
    echo.
    echo Release APK install failed.
    echo Make sure an emulator is running and C: has enough free disk space.
    pause
    exit /b 1
)

echo.
echo Release APK installed successfully.
pause
endlocal
