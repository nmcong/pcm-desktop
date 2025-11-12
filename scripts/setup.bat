@echo off
REM =================================================================
REM PCM Desktop - Unified Setup Script (Windows)
REM =================================================================
REM Downloads all required libraries for PCM Desktop
REM
REM Usage:
REM   setup.bat              - Download all libraries
REM   setup.bat --javafx     - Show JavaFX download instructions
REM   setup.bat --core       - Download only core libraries
REM   setup.bat --ui         - Download only UI libraries
REM   setup.bat --help       - Show help
REM =================================================================

setlocal enabledelayedexpansion

REM Change to project root directory
cd /d "%~dp0\.."

REM Default options
set DOWNLOAD_CORE=0
set DOWNLOAD_UI=0
set DOWNLOAD_ALL=0

REM Parse command line arguments
if "%~1"=="" (
    set DOWNLOAD_ALL=1
) else (
    :parse_args
    if "%~1"=="" goto start_setup
    if /i "%~1"=="--javafx" goto show_javafx_help
    if /i "%~1"=="--core" set DOWNLOAD_CORE=1
    if /i "%~1"=="--ui" set DOWNLOAD_UI=1
    if /i "%~1"=="--all" set DOWNLOAD_ALL=1
    if /i "%~1"=="--help" goto show_help
    if /i "%~1"=="-h" goto show_help
    shift
    goto parse_args
)

:show_help
echo PCM Desktop - Setup Script
echo.
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   --all              Download all libraries (default)
echo   --core             Download only core libraries (Lombok, Jackson, SLF4J, SQLite)
echo   --ui               Download only UI libraries (AtlantaFX, Ikonli)
echo   --javafx           Show JavaFX download instructions
echo   --help, -h         Show this help message
echo.
echo Examples:
echo   %~nx0              # Download all libraries
echo   %~nx0 --core       # Download core libraries only
echo   %~nx0 --ui         # Download UI libraries only
echo   %~nx0 --javafx     # Show JavaFX instructions
exit /b 0

:show_javafx_help
echo.
echo ========================================
echo    JavaFX Download Instructions
echo ========================================
echo.
echo JavaFX 21.0.9 must be downloaded manually from:
echo https://gluonhq.com/products/javafx/
echo.
echo Download for your platform:
echo.
echo Windows (x64):
echo   https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
echo.
echo Installation Steps:
echo   1. Download the ZIP file above
echo   2. Extract the ZIP file
echo   3. Copy all .jar files from 'javafx-sdk-21.0.9\lib\' to: .\lib\javafx\
echo   4. Copy all .dll files from 'javafx-sdk-21.0.9\bin\' to: .\lib\javafx\
echo.
pause
exit /b 0

:start_setup
REM If --all is set, download everything
if %DOWNLOAD_ALL%==1 (
    set DOWNLOAD_CORE=1
    set DOWNLOAD_UI=1
)

echo.
echo ========================================
echo    PCM Desktop - Setup Script
echo ========================================
echo.

REM Check if curl is available
where curl >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] curl not found!
    echo Please install curl or download libraries manually.
    pause
    exit /b 1
)

echo [INFO] Creating library directories...
if not exist "lib\javafx" mkdir lib\javafx
if not exist "lib\others" mkdir lib\others
echo [INFO] Directories created
echo.

REM Download core libraries
if %DOWNLOAD_CORE%==1 (
    echo ========================================
    echo    Downloading Core Libraries
    echo ========================================
    echo.
    
    cd lib\others
    
    echo [INFO] 1. Downloading Lombok 1.18.34...
    curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar
    if %ERRORLEVEL%==0 (
        echo [OK] Lombok downloaded
    ) else (
        echo [ERROR] Failed to download Lombok
    )
    echo.
    
    echo [INFO] 2. Downloading Jackson 2.18.2...
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.2/jackson-core-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.2/jackson-annotations-2.18.2.jar
    curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.18.2/jackson-datatype-jsr310-2.18.2.jar
    if %ERRORLEVEL%==0 (
        echo [OK] Jackson downloaded
    ) else (
        echo [ERROR] Failed to download Jackson
    )
    echo.
    
    echo [INFO] 3. Downloading SLF4J 2.0.16...
    curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar
    if %ERRORLEVEL%==0 (
        echo [OK] SLF4J downloaded
    ) else (
        echo [ERROR] Failed to download SLF4J
    )
    echo.
    
    echo [INFO] 4. Downloading Logback 1.5.12...
    curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar
    curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar
    if %ERRORLEVEL%==0 (
        echo [OK] Logback downloaded
    ) else (
        echo [ERROR] Failed to download Logback
    )
    echo.
    
    echo [INFO] 5. Downloading SQLite JDBC 3.47.1.0...
    curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar
    if %ERRORLEVEL%==0 (
        echo [OK] SQLite JDBC downloaded
    ) else (
        echo [ERROR] Failed to download SQLite JDBC
    )
    echo.
    
    cd ..\..
    
    echo [OK] Core libraries downloaded successfully!
    echo.
)

REM Download UI libraries
if %DOWNLOAD_UI%==1 (
    echo ========================================
    echo    Downloading UI Libraries
    echo ========================================
    echo.
    
    cd lib\others
    
    REM Download AtlantaFX
    echo [INFO] 1. Downloading AtlantaFX 2.0.1...
    curl -L -o atlantafx-base-2.0.1.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.0.1/atlantafx-base-2.0.1.jar
    if %ERRORLEVEL%==0 (
        echo [OK] AtlantaFX downloaded
    ) else (
        echo [ERROR] Failed to download AtlantaFX
    )
    echo.
    
    REM Download Ikonli
    echo [INFO] 2. Downloading Ikonli 12.3.1...
    
    curl -L -o ikonli-core-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.3.1/ikonli-core-12.3.1.jar
    curl -L -o ikonli-javafx-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.3.1/ikonli-javafx-12.3.1.jar
    curl -L -o ikonli-material2-pack-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-material2-pack/12.3.1/ikonli-material2-pack-12.3.1.jar
    curl -L -o ikonli-feather-pack-12.3.1.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.3.1/ikonli-feather-pack-12.3.1.jar
    
    if %ERRORLEVEL%==0 (
        echo [OK] Ikonli downloaded
    ) else (
        echo [ERROR] Failed to download Ikonli
    )
    echo.
    
    cd ..\..
    
    echo [OK] UI libraries downloaded successfully!
    echo.
)

REM Summary
echo ========================================
echo    Setup Completed Successfully!
echo ========================================
echo.

if %DOWNLOAD_CORE%==1 (
    echo [INFO] Downloaded Core Libraries:
    echo   - Lombok 1.18.34
    echo   - Jackson 2.18.2
    echo   - SLF4J 2.0.16
    echo   - Logback 1.5.12
    echo   - SQLite JDBC 3.47.1.0
    echo.
)

if %DOWNLOAD_UI%==1 (
    echo [INFO] Downloaded UI Libraries:
    echo   - AtlantaFX 2.0.1
    echo   - Ikonli 12.3.1 (Core, JavaFX, Material2, Feather)
    echo.
)

echo [IMPORTANT] Manual Step Required:
echo.
echo JavaFX 21.0.9 must be downloaded manually.
echo Run this command for instructions:
echo.
echo   scripts\setup.bat --javafx
echo.

echo [INFO] Libraries in lib\others\:
dir /b lib\others\*.jar 2>nul
echo.

echo [INFO] Next Steps:
echo   1. Download JavaFX (see instructions above)
echo   2. Run: scripts\build.bat
echo   3. Run: scripts\run.bat
echo.

pause

