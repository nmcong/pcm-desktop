@echo off
REM PCM Desktop SSO Integration Demo Runner for Windows
REM ====================================================

echo 🔐 PCM Desktop SSO Integration Demo
echo ====================================

REM Get the current directory (project root)
cd /d "%~dp0.."

echo 📁 Compiling SSO components...

REM Compile SSO classes
javac -cp "out;lib/others/*" -d out src/main/java/com/noteflix/pcm/core/auth/*.java src/main/java/com/noteflix/pcm/examples/SSOIntegrationDemo.java

if %errorlevel% neq 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo ✅ Compilation successful
echo.
echo 🎯 Running SSO Integration Demo...
echo.
echo 📝 This demo will show you how to:
echo   • Extract tokens from browser cookies, localStorage, registry, and files
echo   • Manage token caching and expiration  
echo   • Integrate with SSO systems for automatic authentication
echo.

REM Create logs directory if it doesn't exist
if not exist "logs" mkdir logs

REM Run the demo
java -cp "out;lib/others/*" com.noteflix.pcm.examples.SSOIntegrationDemo

echo.
echo 👋 SSO Integration Demo completed!
echo.
echo 📚 For more information, see:
echo   • docs\SSO_INTEGRATION_GUIDE.md - Complete SSO integration guide
echo   • logs\security-audit.log - Security audit events
pause