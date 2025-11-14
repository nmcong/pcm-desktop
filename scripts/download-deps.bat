@echo off
REM =================================================================
REM PCM Desktop - Download Dependencies from pom.xml (Windows)
REM =================================================================

setlocal enabledelayedexpansion

cd /d "%~dp0\.."

echo.
echo ========================================
echo    Download Dependencies from pom.xml
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

REM Run Python script to download dependencies
python "%~dp0download_deps.py"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Download failed!
    pause
    exit /b 1
)

echo.
echo [SUCCESS] All dependencies downloaded successfully!
echo.
pause

