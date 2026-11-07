@echo off
REM Build APK script for Observer Android app
REM Requires: Android Studio, Java 17+, Android SDK

echo.
echo ====================================
echo Building Observer Android APK
echo ====================================
echo.

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found
    echo Please open this project in Android Studio first
    echo File ^> Settings ^> About ^> Check SDK/Gradle status
    exit /b 1
)

REM Set Java version (adjust path if needed)
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
if not exist "%JAVA_HOME%" (
    echo WARNING: Could not find Android Studio JDK
    echo Using system Java...
)

echo Building release APK...
echo.

REM Build release APK
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo ERROR: Clean failed
    exit /b 1
)

call gradlew.bat assembleRelease
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    exit /b 1
)

echo.
echo ====================================
echo Build successful!
echo ====================================
echo.
echo APK location:
echo   app\build\outputs\apk\release\app-release.apk
echo.
echo To install on device:
echo   adb install -r app\build\outputs\apk\release\app-release.apk
echo.
pause
