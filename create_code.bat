@echo off
REM Generate device enrollment code

echo.
echo ====================================
echo Generate Device Enrollment Code
echo ====================================
echo.

REM Get admin password from .env
for /f "tokens=2 delims==" %%A in ('findstr "ADMIN_PASSWORD" server\.env') do set ADMIN_PASS=%%A

if "%ADMIN_PASS%"=="" (
    echo ERROR: Could not read ADMIN_PASSWORD from server\.env
    echo Please make sure the server is set up first
    pause
    exit /b 1
)

echo Creating new enrollment code...
echo.

REM Use curl to create code
curl -X POST ^
  -H "X-Admin-Token: %ADMIN_PASS%" ^
  http://localhost:8000/api/codes

echo.
echo.
echo Use this code in the Android app when enrolling the device
echo.
pause
