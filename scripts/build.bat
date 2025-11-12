@echo off
REM =================================================================
REM PCM Desktop - Unified Build Script (Windows)
REM =================================================================
REM Compiles Java source code with optional text component support
REM
REM Usage:
REM   build.bat              - Standard build
REM   build.bat --text       - Build with text component
REM   build.bat --clean      - Clean and build
REM   build.bat --help       - Show help
REM =================================================================

setlocal enabledelayedexpansion

REM Change to project root directory
cd /d "%~dp0\.."

REM Default options
set WITH_TEXT_COMPONENT=0
set CLEAN_BUILD=0

REM Parse command line arguments
:parse_args
if "%~1"=="" goto start_build
if /i "%~1"=="--text" set WITH_TEXT_COMPONENT=1
if /i "%~1"=="--text-component" set WITH_TEXT_COMPONENT=1
if /i "%~1"=="--clean" set CLEAN_BUILD=1
if /i "%~1"=="-c" set CLEAN_BUILD=1
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="-h" goto show_help
shift
goto parse_args

:show_help
echo PCM Desktop - Build Script
echo.
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   --text, --text-component    Build with Universal Text Component support
echo   --clean, -c                 Clean build directory before compilation
echo   --help, -h                  Show this help message
echo.
echo Examples:
echo   %~nx0                       # Standard build
echo   %~nx0 --text                # Build with text component
echo   %~nx0 --clean               # Clean and build
echo   %~nx0 --clean --text        # Clean build with text component
exit /b 0

:start_build
echo.
echo ========================================
echo    PCM Desktop - Build Script
echo ========================================
echo.

REM Check if Java is installed
javac -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java compiler (javac) not found!
    echo Please install JDK 21.
    pause
    exit /b 1
)

echo [INFO] Java compiler found!
javac -version
echo.

REM Verify libraries
echo [INFO] Verifying required libraries...
set LIB_ERRORS=0

REM Check JavaFX libraries
set JAVAFX_LIBS=javafx.base.jar javafx.controls.jar javafx.fxml.jar javafx.graphics.jar javafx.media.jar javafx.web.jar
for %%L in (%JAVAFX_LIBS%) do (
    if not exist "lib\javafx\%%L" (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

REM Check other libraries
set OTHER_LIBS=lombok-1.18.34.jar jackson-databind-2.18.2.jar jackson-core-2.18.2.jar slf4j-api-2.0.16.jar logback-classic-1.5.12.jar sqlite-jdbc-3.47.1.0.jar
for %%L in (%OTHER_LIBS%) do (
    if not exist "lib\others\%%L" (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

if %LIB_ERRORS% GTR 0 (
    echo.
    echo [ERROR] %LIB_ERRORS% library/libraries missing!
    echo.
    echo Run the setup script to download libraries:
    echo   scripts\setup.bat
    echo.
    pause
    exit /b 1
)

echo [OK] All required libraries present
echo.

REM Clean build directory if requested
if %CLEAN_BUILD%==1 (
    echo [INFO] Cleaning build directory...
    if exist "out" (
        rmdir /s /q out
        echo [INFO] Build directory cleaned
    )
    echo.
)

REM Create output directory
if not exist "out" mkdir out

REM Build classpath
echo [INFO] Building classpath...
set CLASSPATH=lib\javafx\*;lib\others\*

if %WITH_TEXT_COMPONENT%==1 (
    echo [INFO] Including Universal Text Component libraries...
    set CLASSPATH=!CLASSPATH!;lib\text-component\*
)

echo [INFO] Classpath configured
echo.

REM Compile Java source files
echo [INFO] Compiling Java source files...
echo.

REM Find all Java files recursively and compile
dir /s /b src\main\java\*.java > sources.txt

REM Compile
javac -cp "%CLASSPATH%" ^
  -d out ^
  -encoding UTF-8 ^
  -Xlint:unchecked ^
  @sources.txt

del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [INFO] Compilation successful!
echo.

REM Copy resources
echo [INFO] Copying resources...

REM Create resource directories
if not exist "out\fxml\components" mkdir out\fxml\components
if not exist "out\css" mkdir out\css
if not exist "out\images\icons" mkdir out\images\icons
if not exist "out\db\migration" mkdir out\db\migration

REM Copy FXML files including subdirectories
xcopy /Y /E /I src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
xcopy /Y /E /I src\main\resources\fxml\components\*.fxml out\fxml\components\ >nul 2>&1

REM Copy CSS files
xcopy /Y src\main\resources\css\*.css out\css\ >nul 2>&1

REM Copy image files
xcopy /Y src\main\resources\images\icons\*.png out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\icons\*.svg out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\*.png out\images\ >nul 2>&1

REM Copy database migration files
xcopy /Y src\main\resources\db\migration\*.sql out\db\migration\ >nul 2>&1

REM Copy other resources
xcopy /Y src\main\resources\logback.xml out\ >nul 2>&1

echo [INFO] Resources copied
echo.

echo ========================================
echo    Build Completed Successfully!
echo ========================================
echo.

if %WITH_TEXT_COMPONENT%==1 (
    echo To run with text component support:
    echo   scripts\run.bat --text
) else (
    echo To run the application:
    echo   scripts\run.bat
)

echo.
pause

