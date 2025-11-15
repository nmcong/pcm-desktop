@echo off
REM =================================================================
REM PCM Desktop - Unified Run Script (Windows)
REM =================================================================
REM Runs the PCM Desktop application with various modes
REM
REM Usage:
REM   run.bat              - Run main application
REM   run.bat --text       - Run with text component support
REM   run.bat --api-demo   - Run API integration demo
REM   run.bat --sso-demo   - Run SSO integration demo
REM   run.bat --help       - Show help
REM =================================================================

setlocal enabledelayedexpansion

REM Change to project root directory
cd /d "%~dp0\.."

REM Default options
set RUN_MODE=normal
set WITH_TEXT_COMPONENT=0
set AUTO_COMPILE=1

REM Parse command line arguments
:parse_args
if "%~1"=="" goto start_run
if /i "%~1"=="--text" set WITH_TEXT_COMPONENT=1
if /i "%~1"=="--text-component" set WITH_TEXT_COMPONENT=1
if /i "%~1"=="--api-demo" set RUN_MODE=api-demo
if /i "%~1"=="--sso-demo" set RUN_MODE=sso-demo
if /i "%~1"=="--no-compile" set AUTO_COMPILE=0
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
shift
goto parse_args

:show_help
echo PCM Desktop - Run Script
echo.
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   --text, --text-component    Run with Universal Text Component support
echo   --api-demo                  Run API integration demo
echo   --sso-demo                  Run SSO integration demo
echo   --no-compile                Skip auto-compilation check
echo   --help, -h                  Show this help message
echo.
echo Examples:
echo   %~nx0                       # Run main application
echo   %~nx0 --text                # Run with text component
echo   %~nx0 --api-demo            # Run API demo
echo   %~nx0 --sso-demo            # Run SSO demo
exit /b 0

:start_run
echo.
echo ========================================
echo    PCM Desktop - Run Script
echo ========================================
echo.

REM ============================================================
REM Ensure Java 21 is used
REM ============================================================
echo [INFO] Checking Java version...

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found!
    echo.
    echo This project requires Java 21.
    echo Download from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Get Java version
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VERSION=%%v

REM Remove quotes
set JAVA_VERSION=%JAVA_VERSION:"=%

REM Extract major version
for /f "tokens=1 delims=." %%m in ("%JAVA_VERSION%") do set JAVA_MAJOR=%%m

REM Check if Java 21
if not "%JAVA_MAJOR%"=="21" (
    echo [ERROR] Java 21 required, but found Java %JAVA_MAJOR%!
    echo.
    echo This project requires Java 21.
    echo Download from: https://adoptium.net/
    echo.
    echo Set JAVA_HOME to Java 21 installation.
    echo.
    pause
    exit /b 1
)

echo [OK] Using Java 21
java -version
echo.

REM Check if JavaFX libraries exist
if not exist "lib\javafx\javafx.base.jar" (
    echo [ERROR] JavaFX libraries not found!
    echo Please run: scripts\setup.bat
    pause
    exit /b 1
)

REM Auto-compile project
if %AUTO_COMPILE%==1 (
    echo [INFO] Building project...
    
    if %WITH_TEXT_COMPONENT%==1 (
        call scripts\build.bat --text
    ) else (
        call scripts\build.bat
    )
    
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
    echo.
)

REM Copy resources
echo [INFO] Copying resources...

if not exist "out\fxml\components" mkdir out\fxml\components
if not exist "out\css" mkdir out\css
if not exist "out\images\icons" mkdir out\images\icons
if not exist "out\db\migration" mkdir out\db\migration
if not exist "out\i18n" mkdir out\i18n

REM Copy i18n files
xcopy /Y src\main\resources\i18n\*.properties out\i18n\ >nul 2>&1

xcopy /Y /E /I src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
xcopy /Y /E /I src\main\resources\fxml\components\*.fxml out\fxml\components\ >nul 2>&1
xcopy /Y src\main\resources\css\*.css out\css\ >nul 2>&1
xcopy /Y src\main\resources\images\icons\*.png out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\icons\*.svg out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\*.png out\images\ >nul 2>&1
xcopy /Y src\main\resources\db\migration\*.sql out\db\migration\ >nul 2>&1
xcopy /Y src\main\resources\logback.xml out\ >nul 2>&1

echo [INFO] Resources copied
echo.

REM Build classpath
set CLASSPATH=out;lib\javafx\*;lib\database\*;lib\logs\*;lib\utils\*;lib\ui\*;lib\icons\*;lib\rag\*

REM Run based on mode
if "%RUN_MODE%"=="api-demo" goto run_api_demo
if "%RUN_MODE%"=="sso-demo" goto run_sso_demo
goto run_normal

:run_api_demo
echo [INFO] Running API Integration Demo
echo ================================
echo.

REM Check if OPENAI_API_KEY is set
if "%OPENAI_API_KEY%"=="" (
    echo [ERROR] OPENAI_API_KEY environment variable not found!
    echo.
    echo Please set your OpenAI API key first:
    echo   set OPENAI_API_KEY=your-api-key-here
    echo.
    pause
    exit /b 1
)

echo [INFO] API Key found
echo.

java -cp "%CLASSPATH%" com.noteflix.pcm.llm.examples.APIDemo

echo.
echo [INFO] Demo completed!
pause
exit /b 0

:run_sso_demo
echo [INFO] Running SSO Integration Demo
echo ================================
echo.

if not exist "logs" mkdir logs

java -cp "%CLASSPATH%" com.noteflix.pcm.examples.SSOIntegrationDemo

echo.
echo [INFO] SSO Integration Demo completed!
echo.
echo For more information, see:
echo   - docs\SSO_INTEGRATION_GUIDE.md
echo   - logs\security-audit.log
pause
exit /b 0

:run_normal
if %WITH_TEXT_COMPONENT%==1 (
    echo [INFO] Starting PCM Desktop with Text Component...
    echo.
    
    REM Run with text component support
    java -Djava.library.path=lib\javafx ^
        --module-path lib\javafx ^
        --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
        --add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED ^
        --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED ^
        --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
        --add-opens javafx.base/javafx.beans.binding=ALL-UNNAMED ^
        --add-opens javafx.base/javafx.beans.property=ALL-UNNAMED ^
        --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
        -cp "%CLASSPATH%" ^
        com.noteflix.pcm.PCMApplication
) else (
    echo [INFO] Starting PCM Desktop...
    echo.
    
    REM Standard run
    java -Djava.library.path=lib\javafx ^
        --module-path lib\javafx ^
        --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
        -cp "%CLASSPATH%" ^
        com.noteflix.pcm.PCMApplication
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with error!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [INFO] Application closed.
pause

