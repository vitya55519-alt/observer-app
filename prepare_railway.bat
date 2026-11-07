@echo off
REM Prepare project for Railway deployment

echo.
echo ====================================
echo Preparing for Railway Deployment
echo ====================================
echo.

REM Copy panel to app directory
echo Step 1: Copying panel files...
if not exist "server\app\panel" mkdir server\app\panel
copy server\panel\* server\app\panel\ >nul 2>&1
echo Done!

REM Create dist directory
echo Step 2: Creating dist directory...
if not exist "server\app\dist" mkdir server\app\dist
echo Done!

REM Copy APK if exists
if exist "android\app\build\outputs\apk\release\app-release.apk" (
    echo Step 3: Copying APK...
    copy android\app\build\outputs\apk\release\app-release.apk server\app\dist\app.apk
    echo Done!
) else (
    echo Step 3: No APK found - build it first with Android Studio
)

echo.
echo ====================================
echo Ready for Railway!
echo ====================================
echo.
echo Next steps:
echo 1. Push to GitHub:
echo    git add .
echo    git commit -m "Prepare for Railway"
echo    git push
echo.
echo 2. Deploy on Railway.app:
echo    - Connect GitHub repo
echo    - Add PostgreSQL
echo    - Set environment variables
echo.
echo See РАЗВЕРТЫВАНИЕ_RAILWAY_ПОЛНОЕ.md for details
echo.
pause
