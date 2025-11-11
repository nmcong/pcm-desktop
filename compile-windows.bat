@echo off
REM PCM Desktop Application - Windows Compile Script
REM Compiles the Java source code

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
    echo Please run: download-libs.ps1
    pause
    exit /b 1
)

if not exist "lib\others\lombok-1.18.34.jar" (
    echo [ERROR] Other libraries not found!
    echo Please run: download-libs.ps1
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
if not exist "out\resources" mkdir out\resources
xcopy /E /I /Y src\main\resources out\resources >nul

echo.
echo ========================================
echo    Compilation Successful!
echo ========================================
echo.
echo You can now run: run-windows.bat
echo.
pause

