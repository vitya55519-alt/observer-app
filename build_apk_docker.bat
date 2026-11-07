@echo off
REM Build Observer APK using Docker
REM No Android Studio required!

echo.
echo ====================================
echo Building Observer APK with Docker
echo ====================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running
    echo Please start Docker Desktop first
    pause
    exit /b 1
)

echo Step 1: Building Docker image...
docker build -t observer-builder android/

if %errorlevel% neq 0 (
    echo ERROR: Docker build failed
    pause
    exit /b 1
)

echo.
echo Step 2: Extracting APK from container...
docker create --name observer-temp observer-builder
docker cp observer-temp:/output/app-release.apk android/app-release.apk
docker rm observer-temp

if %errorlevel% neq 0 (
    echo ERROR: Failed to extract APK
    pause
    exit /b 1
)

echo.
echo ====================================
echo Build successful!
echo ====================================
echo.
echo APK location:
echo   android\app-release.apk
echo.
echo File size:
for %%A in (android\app-release.apk) do echo   %%~zA bytes
echo.
echo To copy to server dist folder:
echo   copy android\app-release.apk server\dist\app.apk
echo.
pause
