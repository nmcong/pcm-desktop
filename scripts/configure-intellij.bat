@echo off
REM =================================================================
REM PCM Desktop - IntelliJ IDEA Library Configuration Script (Windows)
REM =================================================================
REM Automatically creates library configurations for IntelliJ IDEA
REM
REM Usage: configure-intellij.bat
REM =================================================================

setlocal enabledelayedexpansion

REM Change to project root directory
cd /d "%~dp0\.."

echo.
echo ========================================
echo    IntelliJ IDEA Library Configuration
echo ========================================
echo.

REM Create .idea directory if it doesn't exist
if not exist ".idea\libraries" mkdir .idea\libraries

echo [INFO] Creating IntelliJ IDEA library configurations...

REM Create JavaFX library
if exist "lib\javafx" (
    echo [INFO] Creating javafx.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="javafx"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/javafx" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<NATIVE^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/javafx" /^>
    echo     ^</NATIVE^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/javafx" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\javafx.xml
    echo [OK] javafx.xml created
)

REM Create Database library
if exist "lib\database" (
    echo [INFO] Creating database.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="database"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/database" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/database" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\database.xml
    echo [OK] database.xml created
)

REM Create Logs library
if exist "lib\logs" (
    echo [INFO] Creating logs.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="logs"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/logs" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/logs" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\logs.xml
    echo [OK] logs.xml created
)

REM Create Utils library
if exist "lib\utils" (
    echo [INFO] Creating utils.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="utils"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/utils" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/utils" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\utils.xml
    echo [OK] utils.xml created
)

REM Create UI library
if exist "lib\ui" (
    echo [INFO] Creating ui.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="ui"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/ui" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/ui" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\ui.xml
    echo [OK] ui.xml created
)

REM Create Icons library
if exist "lib\icons" (
    echo [INFO] Creating icons.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="icons"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/icons" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/icons" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\icons.xml
    echo [OK] icons.xml created
)

REM Create RAG library
if exist "lib\rag" (
    echo [INFO] Creating rag.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="rag"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/rag" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/rag" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\rag.xml
    echo [OK] rag.xml created
)

echo.
echo [SUCCESS] IntelliJ IDEA library configurations created successfully!
echo.
echo [INFO] Library files created in .idea\libraries\:
if exist ".idea\libraries\*.xml" (
    dir /b .idea\libraries\*.xml
) else (
    echo   No library files found
)
echo.
echo [INFO] To use these libraries in IntelliJ IDEA:
echo   1. Open/Reload project in IntelliJ IDEA
echo   2. Go to Project Structure (Ctrl+Alt+Shift+S)
echo   3. Libraries will be automatically detected
echo   4. Add them to your module dependencies as needed
echo.