@echo off
REM =================================================================
REM PCM Desktop - Build Script (Windows)
REM =================================================================

setlocal enabledelayedexpansion

REM Change to project root
cd /d "%~dp0\.."

REM Default options
set WITH_TEXT_COMPONENT=0
set CLEAN_BUILD=0

REM Parse arguments
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
echo   --text              Build with Universal Text Component
echo   --clean, -c         Clean build directory first
echo   --help, -h          Show this help
echo.
exit /b 0

:start_build
echo.
echo ========================================
echo    PCM Desktop - Build Script
echo ========================================
echo.

REM Check Java
echo [INFO] Checking Java version...
javac -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java compiler not found!
    echo Download Java 21 from: https://adoptium.net/
    pause
    exit /b 1
)

REM Get Java version
set JAVA_MAJOR=0
for /f "tokens=2" %%v in ('javac -version 2^>^&1') do (
    for /f "tokens=1 delims=." %%m in ("%%v") do set JAVA_MAJOR=%%m
)

REM Validate Java version
if %JAVA_MAJOR% NEQ 21 (
    echo [ERROR] Java 21 required, found Java %JAVA_MAJOR%
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo [OK] Using Java 21
javac -version
echo.

REM Verify libraries
echo [INFO] Verifying required libraries...
set LIB_ERRORS=0

REM Check JavaFX libraries
for %%L in (javafx.base javafx.controls javafx.fxml javafx.graphics javafx.media javafx.web) do (
    set FOUND=0
    for %%F in (lib\javafx\%%L*.jar) do set FOUND=1
    if !FOUND!==0 (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

REM Check database libraries
for %%L in (sqlite-jdbc HikariCP) do (
    set FOUND=0
    for %%F in (lib\database\%%L*.jar) do set FOUND=1
    if !FOUND!==0 (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

REM Check logging libraries
for %%L in (slf4j-api logback-classic) do (
    set FOUND=0
    for %%F in (lib\logs\%%L*.jar) do set FOUND=1
    if !FOUND!==0 (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

REM Check utils libraries
for %%L in (lombok jackson-databind jackson-core) do (
    set FOUND=0
    for %%F in (lib\utils\%%L*.jar) do set FOUND=1
    if !FOUND!==0 (
        echo [ERROR] Missing: %%L
        set /a LIB_ERRORS+=1
    )
)

if %LIB_ERRORS% GTR 0 (
    echo.
    echo [ERROR] %LIB_ERRORS% library/libraries missing!
    echo Run: scripts\setup.bat
    pause
    exit /b 1
)

echo [OK] All required libraries present
echo.

REM Clean if requested
if %CLEAN_BUILD% EQU 1 (
    echo [INFO] Cleaning build directory...
    if exist "out" rmdir /s /q out
    echo [OK] Cleaned
    echo.
)

REM Create output directory
if not exist "out" mkdir "out"

REM Build classpath
echo [INFO] Building classpath...
set CLASSPATH=lib\javafx\*;lib\database\*;lib\logs\*;lib\utils\*;lib\ui\*;lib\icons\*;lib\rag\*

echo [OK] Classpath configured
echo.

REM Compile
echo [INFO] Compiling Java source files...
echo.

dir /s /b src\main\java\*.java > sources.txt

javac -cp "%CLASSPATH%" -d out -encoding UTF-8 -Xlint:unchecked @sources.txt
set COMPILE_RESULT=%ERRORLEVEL%

del sources.txt

if %COMPILE_RESULT% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [OK] Compilation successful!
echo.

REM Copy resources
echo [INFO] Copying resources...

REM Create resource directories
if not exist "out\fxml\components" mkdir "out\fxml\components"
if not exist "out\css" mkdir "out\css"
if not exist "out\images\icons" mkdir "out\images\icons"
if not exist "out\db\migration" mkdir "out\db\migration"
if not exist "out\i18n" mkdir "out\i18n"

REM Copy files
if exist "src\main\resources\i18n\*.properties" xcopy /Y src\main\resources\i18n\*.properties out\i18n\ >nul 2>&1
if exist "src\main\resources\fxml\*.fxml" xcopy /Y /E /I src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
if exist "src\main\resources\fxml\components\*.fxml" xcopy /Y /E /I src\main\resources\fxml\components\*.fxml out\fxml\components\ >nul 2>&1
if exist "src\main\resources\css\*.css" xcopy /Y src\main\resources\css\*.css out\css\ >nul 2>&1
if exist "src\main\resources\images\icons\*.png" xcopy /Y src\main\resources\images\icons\*.png out\images\icons\ >nul 2>&1
if exist "src\main\resources\images\icons\*.svg" xcopy /Y src\main\resources\images\icons\*.svg out\images\icons\ >nul 2>&1
if exist "src\main\resources\images\*.png" xcopy /Y src\main\resources\images\*.png out\images\ >nul 2>&1
if exist "src\main\resources\db\migration\*.sql" xcopy /Y src\main\resources\db\migration\*.sql out\db\migration\ >nul 2>&1
if exist "src\main\resources\logback.xml" xcopy /Y src\main\resources\logback.xml out\ >nul 2>&1

echo [OK] Resources copied
echo.

echo ========================================
echo    Build Completed Successfully!
echo ========================================
echo.

if %WITH_TEXT_COMPONENT% EQU 1 (
    echo To run: scripts\run.bat --text
) else (
    echo To run: scripts\run.bat
)

echo.
pause
