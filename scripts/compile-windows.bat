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

REM Compile all Java files
javac -cp "lib\javafx\*;lib\others\*" ^
  -d out ^
  -encoding UTF-8 ^
  -Xlint:unchecked ^
  src\main\java\com\noteflix\pcm\*.java ^
  src\main\java\com\noteflix\pcm\ui\*.java ^
  src\main\java\com\noteflix\pcm\domain\*.java ^
  src\main\java\com\noteflix\pcm\application\*.java ^
  src\main\java\com\noteflix\pcm\infrastructure\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

REM Copy resources
echo.
echo [INFO] Copying resources...
if not exist "out\fxml" mkdir out\fxml
if not exist "out\css" mkdir out\css
if not exist "out\images\icons" mkdir out\images\icons
xcopy /Y src\main\resources\fxml\*.fxml out\fxml\ >nul 2>&1
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

