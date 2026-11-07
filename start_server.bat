@echo off
REM Start Observer server locally with Docker

echo.
echo ====================================
echo Starting Observer Server
echo ====================================
echo.

REM Check Docker
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running
    echo Please start Docker Desktop first
    pause
    exit /b 1
)

REM Check if .env exists
if not exist "server\.env" (
    echo Creating .env file...
    copy server\.env.example server\.env
    
    REM Generate random passwords (using PowerShell)
    powershell -Command "^
        $pass = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 12 | %{[char]$_}); ^
        $secret = -join ((65..90) + (97..122) + (48..57) | Get-Random -Count 32 | %{[char]$_}); ^
        (Get-Content 'server\.env') -replace 'ADMIN_PASSWORD=.*', \"ADMIN_PASSWORD=$pass\" | Set-Content 'server\.env'; ^
        (Get-Content 'server\.env') -replace 'SECRET=.*', \"SECRET=$secret\" | Set-Content 'server\.env'; ^
        (Get-Content 'server\.env') -replace 'DB_PASS=.*', \"DB_PASS=$pass\" | Set-Content 'server\.env'; ^
        (Get-Content 'server\.env') -replace 'DATABASE_URL=.*', \"DATABASE_URL=postgresql://obs:$pass@db:5432/obs\" | Set-Content 'server\.env'; ^
        Write-Host \"Credentials generated: ADMIN_PASSWORD=$pass\"" || (
        echo Could not generate credentials automatically
        echo Please edit server\.env manually and set passwords
    )
)

cd server

echo.
echo Step 1: Creating directories...
if not exist "dist" mkdir dist
if not exist "panel" mkdir panel

echo Step 2: Starting Docker Compose...
docker compose up -d --build

if %errorlevel% neq 0 (
    echo ERROR: Docker Compose failed
    pause
    exit /b 1
)

echo.
echo Waiting for services to start...
timeout /t 5 /nobreak

echo.
echo ====================================
echo Server is running!
echo ====================================
echo.
echo Access points:
echo   API:      http://localhost:8000
echo   Panel:    http://localhost/panel/
echo   Caddy:    http://localhost
echo.
echo To check status:
echo   docker compose ps
echo.
echo To view logs:
echo   docker compose logs -f
echo.
echo To stop:
echo   docker compose down
echo.
echo Admin password (from .env):
for /f "tokens=2 delims==" %%A in ('findstr "ADMIN_PASSWORD" .env') do echo   %%A
echo.
pause
