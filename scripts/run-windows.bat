@echo off
REM PCM Desktop Application - Windows Run Script
REM Requires Java 21 and JavaFX 21.0.9

echo.
echo ========================================
echo    PCM Desktop Application
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found! Please install Java 21.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Checking Java version...
java -version

REM Check if JavaFX libraries exist
if not exist "lib\javafx\javafx.base.jar" (
    echo.
    echo [ERROR] JavaFX libraries not found!
    echo Please run: download-libs.ps1
    echo Or download manually from: https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
    pause
    exit /b 1
)

echo [INFO] JavaFX libraries found!
echo.

REM Copy resources to output directory
echo [INFO] Copying resources...
if not exist "out\fxml" mkdir out\fxml
if not exist "out\css" mkdir out\css
if not exist "out\images" mkdir out\images
xcopy /Y src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
xcopy /Y src\main\resources\css\*.css out\css\ >nul 2>&1
xcopy /Y src\main\resources\logback.xml out\ >nul 2>&1
echo [INFO] Resources copied!
echo.
echo [INFO] Starting PCM Application...
echo.

REM Run the application
java --module-path lib\javafx ^
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.media ^
  -cp "out;lib\others\*" ^
  com.noteflix.pcm.PCMApplication

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Failed to start application!
    echo Make sure you have compiled the project first.
    pause
    exit /b 1
)

echo.
echo [INFO] Application closed.
pause

