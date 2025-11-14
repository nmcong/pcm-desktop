@echo off
REM PCM Desktop API Integration Demo Runner for Windows
REM ===================================================

echo 🚀 PCM Desktop API Integration Demo
echo ====================================

REM Check if OPENAI_API_KEY is set
if not defined OPENAI_API_KEY (
    echo.
    echo ⚠️  OPENAI_API_KEY environment variable not found!
    echo.
    echo Please set your OpenAI API key first:
    echo   set OPENAI_API_KEY=your-api-key-here
    echo.
    echo Or create a .env file in the project root and run:
    echo   for /f "tokens=*" %%i in (.env) do set %%i
    echo.
    pause
    exit /b 1
)

echo ✅ API Key found
echo 📁 Compiling Java sources...

REM Get the current directory (project root)
cd /d "%~dp0.."

REM Compile the demo
javac -cp "out;lib/others/*;lib/javafx/*" --module-path lib/javafx --add-modules javafx.controls,javafx.fxml -d out src/main/java/com/noteflix/pcm/llm/examples/APIDemo.java

if %errorlevel% neq 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo ✅ Compilation successful
echo 🎯 Running demo...
echo.

REM Run the demo
java -cp "out;lib/others/*" com.noteflix.pcm.llm.examples.APIDemo

echo.
echo 👋 Demo completed. Thanks for trying PCM Desktop API Integration!
pause