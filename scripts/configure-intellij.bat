@echo off
REM =================================================================
REM PCM Desktop - IntelliJ IDEA Library Configuration Script (Windows)
REM =================================================================
REM Adds library entries to existing .idea/pcm-desktop.iml file
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

echo [INFO] Configuring IntelliJ IDEA libraries...

REM Define libraries array
set LIBRARIES=
set LIB_COUNT=0

REM Check which library folders exist and add them to the list
if exist "lib\database" (
    set LIBRARIES=!LIBRARIES! database
    set /a LIB_COUNT+=1
)

if exist "lib\icons" (
    set LIBRARIES=!LIBRARIES! icons
    set /a LIB_COUNT+=1
)

if exist "lib\javafx" (
    set LIBRARIES=!LIBRARIES! javafx
    set /a LIB_COUNT+=1
)

if exist "lib\logs" (
    set LIBRARIES=!LIBRARIES! logs
    set /a LIB_COUNT+=1
)

if exist "lib\rag" (
    set LIBRARIES=!LIBRARIES! rag
    set /a LIB_COUNT+=1
)

if exist "lib\ui" (
    set LIBRARIES=!LIBRARIES! ui
    set /a LIB_COUNT+=1
)

if exist "lib\utils" (
    set LIBRARIES=!LIBRARIES! utils
    set /a LIB_COUNT+=1
)

REM Create library XML files
for %%L in (!LIBRARIES!) do (
    echo [INFO] Creating %%L.xml library configuration...
    (
    echo ^<component name="libraryTable"^>
    echo   ^<library name="%%L"^>
    echo     ^<CLASSES^>
    echo       ^<root url="file://$PROJECT_DIR$/lib/%%L" /^>
    echo     ^</CLASSES^>
    echo     ^<JAVADOC /^>
    if "%%L"=="javafx" (
        echo     ^<NATIVE^>
        echo       ^<root url="file://$PROJECT_DIR$/lib/%%L" /^>
        echo     ^</NATIVE^>
    )
    echo     ^<SOURCES /^>
    echo     ^<jarDirectory url="file://$PROJECT_DIR$/lib/%%L" recursive="false" /^>
    echo   ^</library^>
    echo ^</component^>
    ) > .idea\libraries\%%L.xml
    echo [OK] %%L.xml created
)

REM Check if .idea\pcm-desktop.iml exists
set IML_FILE=.idea\pcm-desktop.iml

if not exist "%IML_FILE%" (
    echo [WARNING] File %IML_FILE% not found!
    echo [INFO] Please ensure your project is opened in IntelliJ IDEA first.
    echo [INFO] Library configurations have been created in .idea\libraries\
) else (
    echo [INFO] Updating %IML_FILE% with library dependencies...
    
    REM Create temporary file for modification
    set TEMP_FILE=%IML_FILE%.temp
    
    REM Check which libraries need to be added (avoid duplicates)
    set LIBRARIES_TO_ADD=
    for %%L in (!LIBRARIES!) do (
        findstr /C:"name=\"%%L\"" "%IML_FILE%" >nul
        if !errorlevel! neq 0 (
            set LIBRARIES_TO_ADD=!LIBRARIES_TO_ADD! %%L
        )
    )
    
    REM Check if any libraries need to be added
    if "!LIBRARIES_TO_ADD!"=="" (
        echo [INFO] All libraries already present in %IML_FILE%
    ) else (
        REM Process the file line by line  
        (
        for /f "usebackq delims=" %%A in ("%IML_FILE%") do (
            echo %%A
            
            REM If we find the sourceFolder line, add library entries after it
            echo %%A | findstr /C:"orderEntry type=\"sourceFolder\"" >nul
            if !errorlevel! equ 0 (
                for %%L in (!LIBRARIES_TO_ADD!) do (
                    echo     ^<orderEntry type="library" name="%%L" level="project" /^>
                )
            )
        )
        ) > "%TEMP_FILE%"
        
        REM Replace original file with modified one
        move "%TEMP_FILE%" "%IML_FILE%" >nul
        echo [OK] %IML_FILE% updated with library dependencies
    )
)

echo.
echo [SUCCESS] IntelliJ IDEA configuration completed!
echo.
echo [INFO] Files created/updated:
for %%L in (!LIBRARIES!) do (
    echo   - .idea\libraries\%%L.xml
)
if exist "%IML_FILE%" (
    echo   - %IML_FILE% (updated with library dependencies)
)
echo.
echo [INFO] To apply changes in IntelliJ IDEA:
echo   1. Refresh project (Ctrl+Shift+F5 or File -^> Reload Gradle Project)
echo   2. Or restart IntelliJ IDEA
echo   3. Libraries will be automatically available
echo.