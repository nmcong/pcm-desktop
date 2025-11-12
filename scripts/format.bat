@echo off
REM =================================================================
REM PCM Desktop - Code Formatting Script (Windows)
REM =================================================================
REM Formats Java source code using Google Java Format
REM
REM Usage:
REM   format.bat              # Format all Java files
REM   format.bat --check      # Check formatting only (no changes)
REM   format.bat --help       # Show help
REM =================================================================

setlocal enabledelayedexpansion

cd /d "%~dp0\.."
set PROJECT_ROOT=%CD%

REM Google Java Format JAR
set FORMATTER_JAR=lib\others\google-java-format-1.32.0-all-deps.jar

REM Default options
set CHECK_ONLY=false

REM Parse command line arguments
:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="--check" (
    set CHECK_ONLY=true
    shift
    goto parse_args
)
if /i "%~1"=="-c" (
    set CHECK_ONLY=true
    shift
    goto parse_args
)
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help

echo Unknown option: %~1
echo Use --help for usage information
exit /b 1

:show_help
echo PCM Desktop - Code Formatting Script
echo.
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   --check, -c    Check formatting only (no changes)
echo   --help, -h     Show this help message
echo.
echo Examples:
echo   %~nx0           # Format all Java files
echo   %~nx0 --check   # Check if files need formatting
exit /b 0

:end_parse

echo ========================================
echo  PCM Desktop - Code Formatter
echo ========================================
echo.

REM Check if formatter JAR exists
if not exist "%FORMATTER_JAR%" (
    echo [ERROR] Google Java Format JAR not found!
    echo.
    echo Expected location: %FORMATTER_JAR%
    echo.
    echo Please run the setup script first:
    echo   scripts\setup.bat
    exit /b 1
)

REM Find all Java files
echo [INFO] Finding Java source files...
set JAVA_COUNT=0
for /r src\main\java %%f in (*.java) do (
    set /a JAVA_COUNT+=1
)

if %JAVA_COUNT% equ 0 (
    echo [WARN] No Java files found
    exit /b 0
)

echo [OK] Found %JAVA_COUNT% Java files
echo.

REM Format or check all files
if "%CHECK_ONLY%"=="true" (
    echo [INFO] Checking code formatting...
    
    set UNFORMATTED=0
    for /r src\main\java %%f in (*.java) do (
        java -jar "%FORMATTER_JAR%" --dry-run --set-exit-if-changed "%%f" >nul 2>&1
        if !errorlevel! neq 0 (
            echo   [WARN] %%f
            set /a UNFORMATTED+=1
        )
    )
    
    echo.
    if !UNFORMATTED! equ 0 (
        echo [OK] All files are correctly formatted!
        exit /b 0
    ) else (
        echo [WARN] !UNFORMATTED! file(s) need formatting
        echo.
        echo To format all files, run:
        echo   scripts\format.bat
        exit /b 1
    )
) else (
    echo [INFO] Formatting Java source files...
    
    set FORMATTED=0
    set FAILED=0
    
    for /r src\main\java %%f in (*.java) do (
        java -jar "%FORMATTER_JAR%" --replace "%%f" >nul 2>&1
        if !errorlevel! equ 0 (
            set /a FORMATTED+=1
            echo   [OK] %%f
        ) else (
            set /a FAILED+=1
            echo   [ERROR] %%f
        )
    )
    
    echo.
    if !FAILED! equ 0 (
        echo [OK] Successfully formatted !FORMATTED! file(s)!
        echo.
        echo [TIP] Use --check to verify formatting without changes
    ) else (
        echo [WARN] Formatted !FORMATTED! file(s), !FAILED! failed
        exit /b 1
    )
)

endlocal

