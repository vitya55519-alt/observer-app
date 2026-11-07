@echo off
REM Observer - Quick Start Menu

:menu
cls
echo.
echo ====================================
echo     OBSERVER - Quick Start Menu
echo ====================================
echo.
echo 1. Build APK (using Docker)
echo 2. Start Server (localhost)
echo 3. Create Device Code
echo 4. Open Web Panel
echo 5. Check Server Status
echo 6. View Server Logs
echo 7. Stop Server
echo 8. Exit
echo.
set /p choice="Select option (1-8): "

if "%choice%"=="1" goto build_apk
if "%choice%"=="2" goto start_server
if "%choice%"=="3" goto create_code
if "%choice%"=="4" goto open_panel
if "%choice%"=="5" goto check_status
if "%choice%"=="6" goto view_logs
if "%choice%"=="7" goto stop_server
if "%choice%"=="8" exit /b 0

echo Invalid choice
timeout /t 2
goto menu

:build_apk
cls
echo Building APK...
call build_apk_docker.bat
goto menu

:start_server
cls
echo Starting server...
call start_server.bat
goto menu

:create_code
cls
echo Creating enrollment code...
call create_code.bat
goto menu

:open_panel
cls
echo Opening web panel...
start http://localhost/panel/
timeout /t 2
goto menu

:check_status
cls
echo.
docker compose -f server/docker-compose.yml ps
echo.
pause
goto menu

:view_logs
cls
echo Showing last 50 lines of logs (press Ctrl+C to stop)...
echo.
docker compose -f server/docker-compose.yml logs -f --tail=50
goto menu

:stop_server
cls
echo Stopping server...
docker compose -f server/docker-compose.yml down
echo Server stopped
timeout /t 2
goto menu
