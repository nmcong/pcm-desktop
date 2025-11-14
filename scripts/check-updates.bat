@echo off
REM =================================================================
REM PCM Desktop - Check for Library Updates (Windows)
REM =================================================================

setlocal enabledelayedexpansion

cd /d "%~dp0\.."

echo.
echo ========================================
echo    Check for Library Updates
echo ========================================
echo.

REM Check for Python
where python >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Python not found!
    echo Please install Python 3.x from: https://www.python.org/
    pause
    exit /b 1
)

REM Run Python script to check updates
python "%~dp0check_updates.py"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Update check failed!
    pause
    exit /b 1
)

echo.
pause

