@echo off
REM Setup and run Observer server without Docker
REM Requires: Python 3.11+, PostgreSQL

echo.
echo ====================================
echo Observer Server - Local Setup
echo ====================================
echo.

REM Check Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python is not installed
    echo Please install Python 3.11+ from https://www.python.org
    pause
    exit /b 1
)

echo Python found:
python --version

REM Check PostgreSQL
where psql >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo WARNING: PostgreSQL not found in PATH
    echo Please install PostgreSQL from https://www.postgresql.org/download/windows/
    echo.
    echo After installation, add to PATH:
    echo   C:\Program Files\PostgreSQL\15\bin
    echo.
    pause
    exit /b 1
)

echo PostgreSQL found

REM Create .env if not exists
if not exist "server\.env" (
    echo.
    echo Creating .env file...
    copy server\.env.example server\.env
    
    echo.
    echo IMPORTANT: Edit server\.env manually:
    echo   - Set ADMIN_PASSWORD to a strong password
    echo   - Set SECRET to a random string
    echo   - Set DB_PASS to your PostgreSQL password
    echo   - Ensure DATABASE_URL matches your PostgreSQL connection
    echo.
    pause
    notepad server\.env
)

REM Install Python dependencies
echo.
echo Installing Python dependencies...
cd server\app
python -m pip install -q -r req.txt

if %errorlevel% neq 0 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

cd ..\..

REM Start server
echo.
echo ====================================
echo Starting FastAPI server...
echo ====================================
echo.
echo API: http://localhost:8000
echo Panel: http://localhost:8080 (separate terminal)
echo.
echo Press Ctrl+C to stop
echo.

cd server\app
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
