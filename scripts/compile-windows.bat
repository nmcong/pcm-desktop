@echo off
REM PCM Desktop Application - Windows Compile Script
REM Compiles the Java source code

REM Change to project root directory (parent of scripts folder)
cd /d "%~dp0\.."

echo.
echo ========================================
echo    Compiling PCM Desktop
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

REM Check if libraries exist
if not exist "lib\javafx\javafx.base.jar" (
    echo [ERROR] JavaFX libraries not found!
    echo Please run: scripts\download-libs.ps1
    pause
    exit /b 1
)

if not exist "lib\others\lombok-1.18.34.jar" (
    echo [ERROR] Other libraries not found!
    echo Please run: scripts\download-libs.ps1
    pause
    exit /b 1
)

REM Create output directory
if not exist "out" mkdir out

echo [INFO] Compiling Java source files...
echo.

REM Find all Java files recursively and compile
dir /s /b src\main\java\*.java > sources.txt
javac -cp "lib\javafx\*;lib\others\*" ^
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

REM Copy resources
echo.
echo [INFO] Copying resources...
if not exist "out\fxml\components" mkdir out\fxml\components
if not exist "out\css" mkdir out\css
if not exist "out\images\icons" mkdir out\images\icons
REM Copy all FXML files including subdirectories
xcopy /Y /E /I src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
xcopy /Y /E /I src\main\resources\fxml\components\*.fxml out\fxml\components\ >nul 2>&1
xcopy /Y src\main\resources\css\*.css out\css\ >nul 2>&1
xcopy /Y src\main\resources\images\icons\*.png out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\icons\*.svg out\images\icons\ >nul 2>&1
xcopy /Y src\main\resources\images\*.png out\images\ >nul 2>&1
xcopy /Y src\main\resources\logback.xml out\ >nul 2>&1

echo.
echo ========================================
echo    Compilation Successful!
echo ========================================
echo.
echo You can now run: scripts\run-windows.bat
echo.
pause

