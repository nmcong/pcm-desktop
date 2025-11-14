@echo off
REM =================================================================
REM PCM Desktop - Create Library Archive (Windows)
REM Creates multi-part archives with max 45MB per part
REM =================================================================

setlocal enabledelayedexpansion

cd /d "%~dp0\.."

echo.
echo ========================================
echo    Create Library Archive
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

REM Run Python script to create archive
python "%~dp0create_archive.py"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Archive creation failed!
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Library archives created successfully!
echo.
pause

