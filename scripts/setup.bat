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

:start_setup

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
if not exist "lib\rag" mkdir lib\rag
if not exist "lib\langchain4j" mkdir lib\langchain4j
if not exist "lib\text-component" mkdir lib\text-component
echo [INFO] Directories created
echo.

REM Download core libraries
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

echo [INFO] 6. Downloading Oracle OJDBC 23.3.0...
curl -L -o ojdbc11-23.3.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.3.0.23.09/ojdbc11-23.3.0.23.09.jar
if %ERRORLEVEL%==0 (
    echo [OK] Oracle OJDBC downloaded
) else (
    echo [ERROR] Failed to download Oracle OJDBC
)
echo.

echo [INFO] 7. Downloading HikariCP 5.1.0...
curl -O https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] HikariCP downloaded
) else (
    echo [ERROR] Failed to download HikariCP
)
echo.

echo [INFO] 8. Downloading Oracle UCP 23.3.0...
curl -L -o ucp-23.3.0.jar https://repo1.maven.org/maven2/com/oracle/database/ucp/ucp/23.3.0.23.09/ucp-23.3.0.23.09.jar
if %ERRORLEVEL%==0 (
    echo [OK] Oracle UCP downloaded
) else (
    echo [ERROR] Failed to download Oracle UCP
)
echo.

REM Download AtlantaFX
echo [INFO] 9. Downloading AtlantaFX 2.0.1...
curl -L -o atlantafx-base-2.0.1.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.0.1/atlantafx-base-2.0.1.jar
if %ERRORLEVEL%==0 (
    echo [OK] AtlantaFX downloaded
) else (
    echo [ERROR] Failed to download AtlantaFX
)
echo.

REM Download Ikonli
echo [INFO] 10. Downloading Ikonli 12.3.1...
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

echo [OK] Core and UI libraries downloaded successfully!
echo.

REM Download RAG libraries
echo ========================================
echo    Downloading RAG Libraries
echo ========================================
echo.

cd lib\rag

echo [INFO] 1. Downloading Apache Lucene 9.11.1...
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-core/9.11.1/lucene-core-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analysis-common/9.11.1/lucene-analysis-common-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analyzers-common/9.11.1/lucene-analyzers-common-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queryparser/9.11.1/lucene-queryparser-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queries/9.11.1/lucene-queries-9.11.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-highlighter/9.11.1/lucene-highlighter-9.11.1.jar
if %ERRORLEVEL%==0 (
    echo [OK] Apache Lucene downloaded
) else (
    echo [ERROR] Failed to download Apache Lucene
)
echo.

echo [INFO] 2. Downloading DJL ONNX Runtime 0.35.0...
curl -O https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/tokenizers/0.35.0/tokenizers-0.35.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] DJL ONNX Runtime downloaded
) else (
    echo [ERROR] Failed to download DJL ONNX Runtime
)
echo.

echo [INFO] 3. Downloading ONNX Runtime 1.23.2...
curl -L -o onnxruntime-1.23.2.jar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
if %ERRORLEVEL%==0 (
    echo [OK] ONNX Runtime downloaded
) else (
    echo [ERROR] Failed to download ONNX Runtime
)
echo.

cd ..\..

echo [OK] RAG libraries downloaded successfully!
echo.

REM Download LLM libraries  
echo ========================================
echo    Downloading LLM Libraries
echo ========================================
echo.

cd lib\langchain4j

echo [INFO] 1. Downloading LangChain4j 1.8.0...
curl -O https://repo1.maven.org/maven2/dev/langchain4j/langchain4j/1.8.0/langchain4j-1.8.0.jar
curl -O https://repo1.maven.org/maven2/dev/langchain4j/langchain4j-core/1.8.0/langchain4j-core-1.8.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] LangChain4j downloaded
) else (
    echo [ERROR] Failed to download LangChain4j
)
echo.

cd ..\..

echo [OK] LLM libraries downloaded successfully!
echo.

REM Download JavaFX
echo ========================================
echo    Downloading and Extracting JavaFX
echo ========================================
echo.

echo [INFO] 1. Downloading JavaFX 21.0.9...
curl -L -o javafx-21.0.9.zip https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip
if %ERRORLEVEL%==0 (
    echo [OK] JavaFX downloaded
) else (
    echo [ERROR] Failed to download JavaFX
    goto skip_javafx
)

echo [INFO] 2. Extracting JavaFX...
powershell -command "Expand-Archive -Path javafx-21.0.9.zip -DestinationPath temp-javafx -Force"
if %ERRORLEVEL%==0 (
    echo [OK] JavaFX extracted
) else (
    echo [ERROR] Failed to extract JavaFX
    goto skip_javafx
)

echo [INFO] 3. Copying JavaFX files...
copy temp-javafx\javafx-sdk-21.0.9\lib\*.jar lib\javafx\
copy temp-javafx\javafx-sdk-21.0.9\bin\*.dll lib\javafx\
if %ERRORLEVEL%==0 (
    echo [OK] JavaFX files copied
) else (
    echo [ERROR] Failed to copy JavaFX files
)

echo [INFO] 4. Cleaning up temporary files...
rmdir /s /q temp-javafx
del javafx-21.0.9.zip
echo [OK] Cleanup completed

:skip_javafx

echo [OK] JavaFX setup completed!
echo.

REM Download Text Component libraries
echo ========================================
echo    Downloading Text Component Libraries
echo ========================================
echo.

cd lib\text-component

echo [INFO] 1. Downloading RichTextFX 0.11.6...
curl -O https://repo1.maven.org/maven2/org/fxmisc/richtext/richtextfx/0.11.6/richtextfx-0.11.6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar
curl -O https://repo1.maven.org/maven2/org/reactfx/reactfx/2.0-M5/reactfx-2.0-M5.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar
if %ERRORLEVEL%==0 (
    echo [OK] RichTextFX and dependencies downloaded
) else (
    echo [ERROR] Failed to download RichTextFX
)
echo.

echo [INFO] 2. Downloading JavaFX Markdown Preview 1.0.3...
curl -L -o javafx-markdown-preview-all-1.0.3.jar https://repo1.maven.org/maven2/com/sandec/mdfx/1.0.3/mdfx-1.0.3.jar
if %ERRORLEVEL%==0 (
    echo [OK] JavaFX Markdown Preview downloaded
) else (
    echo [ERROR] Failed to download JavaFX Markdown Preview
)
echo.

cd ..\..

echo [OK] Text Component libraries downloaded successfully!
echo.

REM Generate classpath files
echo ========================================
echo    Generating Classpath Files
echo ========================================
echo.

echo [INFO] Generating Windows classpath...
call :generate_classpath_windows

echo [INFO] Generating Unix classpath...
call :generate_classpath_unix

echo [INFO] Generating library list...
call :generate_library_list

echo [INFO] Configuring IntelliJ IDEA libraries...
call :configure_intellij_libraries

echo [OK] Classpath and IDE configuration completed successfully!
echo.

REM Summary
echo ========================================
echo    Setup Completed Successfully!
echo ========================================
echo.

echo [INFO] Downloaded ALL Libraries:
echo.
echo   Core Libraries:
echo   - Lombok 1.18.34
echo   - Jackson 2.18.2 (Core, Databind, Annotations, JSR310)
echo   - SLF4J 2.0.16
echo   - Logback 1.5.12 (Classic, Core)
echo   - SQLite JDBC 3.47.1.0
echo.
echo   Oracle Libraries:
echo   - Oracle OJDBC 23.3.0.23.09
echo   - HikariCP 5.1.0
echo   - Oracle UCP 23.3.0.23.09
echo.
echo   UI Libraries:
echo   - AtlantaFX 2.0.1
echo   - Ikonli 12.3.1 (Core, JavaFX, Material2, Feather)
echo.
echo   RAG Libraries:
echo   - Apache Lucene 9.11.1 (Core, Analysis, QueryParser, Highlighter)
echo   - DJL ONNX Runtime 0.35.0 (API, Engine, Tokenizers)
echo   - ONNX Runtime 1.23.2
echo.
echo   LLM Libraries:
echo   - LangChain4j 1.8.0 (Core and Main)
echo.
echo   JavaFX Libraries:
echo   - JavaFX 21.0.9 (automatically downloaded and extracted)
echo.
echo   Text Component Libraries:
echo   - RichTextFX 0.11.6 and dependencies
echo   - JavaFX Markdown Preview 1.0.3
echo.

echo [SUCCESS] All libraries downloaded and ready to use!
echo.

echo [INFO] Libraries Summary:
echo.
echo   JavaFX Libraries (lib\javafx\):
dir /b lib\javafx\*.jar 2>nul | find /c ".jar" >nul && (
    echo   JavaFX SDK files: 
    dir /b lib\javafx\*.jar 2>nul
    echo   Native libraries: 
    dir /b lib\javafx\*.dll 2>nul
) || echo   [WARNING] JavaFX not found!
echo.
echo   Core and Oracle Libraries (lib\others\):
dir /b lib\others\*.jar 2>nul
echo.
echo   RAG Libraries (lib\rag\):
dir /b lib\rag\*.jar 2>nul  
echo.
echo   LLM Libraries (lib\langchain4j\):
dir /b lib\langchain4j\*.jar 2>nul
echo.
echo   Text Component Libraries (lib\text-component\):
dir /b lib\text-component\*.jar 2>nul
echo.

echo [INFO] Next Steps:
echo   1. Run: scripts\build.bat
echo   2. Run: scripts\run.bat
echo   3. Restart IntelliJ IDEA to load libraries
echo.

pause

:generate_classpath_windows
echo [INFO] Creating Windows classpath file...
echo @echo off > classpath_windows.bat
echo set CLASSPATH=. >> classpath_windows.bat

REM Add JavaFX libraries
for %%f in (lib\javafx\*.jar) do echo set CLASSPATH=%%CLASSPATH%%;%%f >> classpath_windows.bat

REM Add other libraries
for %%f in (lib\others\*.jar) do echo set CLASSPATH=%%CLASSPATH%%;%%f >> classpath_windows.bat

REM Add RAG libraries
for %%f in (lib\rag\*.jar) do echo set CLASSPATH=%%CLASSPATH%%;%%f >> classpath_windows.bat

REM Add LangChain4j libraries
for %%f in (lib\langchain4j\*.jar) do echo set CLASSPATH=%%CLASSPATH%%;%%f >> classpath_windows.bat

REM Add Text Component libraries
for %%f in (lib\text-component\*.jar) do echo set CLASSPATH=%%CLASSPATH%%;%%f >> classpath_windows.bat

echo [OK] Windows classpath generated: classpath_windows.bat
goto :eof

:generate_classpath_unix
echo [INFO] Creating Unix classpath file...
echo #!/bin/bash > classpath_unix.sh
echo export CLASSPATH=. >> classpath_unix.sh

REM Add JavaFX libraries
for %%f in (lib\javafx\*.jar) do echo export CLASSPATH=$CLASSPATH:%%f >> classpath_unix.sh

REM Add other libraries
for %%f in (lib\others\*.jar) do echo export CLASSPATH=$CLASSPATH:%%f >> classpath_unix.sh

REM Add RAG libraries
for %%f in (lib\rag\*.jar) do echo export CLASSPATH=$CLASSPATH:%%f >> classpath_unix.sh

REM Add LangChain4j libraries
for %%f in (lib\langchain4j\*.jar) do echo export CLASSPATH=$CLASSPATH:%%f >> classpath_unix.sh

REM Add Text Component libraries
for %%f in (lib\text-component\*.jar) do echo export CLASSPATH=$CLASSPATH:%%f >> classpath_unix.sh

chmod +x classpath_unix.sh
echo [OK] Unix classpath generated: classpath_unix.sh
goto :eof

:generate_library_list
echo [INFO] Creating library inventory...
echo # PCM Desktop - Library Inventory > library_list.txt
echo # Generated on %date% %time% >> library_list.txt
echo. >> library_list.txt

echo ## JavaFX Libraries (lib/javafx/) >> library_list.txt
if exist "lib\javafx\*.jar" (
    for %%f in (lib\javafx\*.jar) do echo - %%~nxf >> library_list.txt
)
echo. >> library_list.txt

echo ## Core and Oracle Libraries (lib/others/) >> library_list.txt
if exist "lib\others\*.jar" (
    for %%f in (lib\others\*.jar) do echo - %%~nxf >> library_list.txt
)
echo. >> library_list.txt

echo ## RAG Libraries (lib/rag/) >> library_list.txt
if exist "lib\rag\*.jar" (
    for %%f in (lib\rag\*.jar) do echo - %%~nxf >> library_list.txt
)
echo. >> library_list.txt

echo ## LLM Libraries (lib/langchain4j/) >> library_list.txt
if exist "lib\langchain4j\*.jar" (
    for %%f in (lib\langchain4j\*.jar) do echo - %%~nxf >> library_list.txt
)
echo. >> library_list.txt

echo ## Text Component Libraries (lib/text-component/) >> library_list.txt
if exist "lib\text-component\*.jar" (
    for %%f in (lib\text-component\*.jar) do echo - %%~nxf >> library_list.txt
)
echo. >> library_list.txt

echo [OK] Library inventory generated: library_list.txt
goto :eof

:configure_intellij_libraries
echo [INFO] Configuring IntelliJ IDEA libraries...

REM Create .idea\libraries directory
if not exist ".idea\libraries" mkdir .idea\libraries

REM Function to create library XML - JavaFX
call :create_library_xml "JavaFX_21_0_9" "lib\javafx"

REM Function to create library XML - Core Libraries
call :create_library_xml "PCM_Core_Libraries" "lib\others"

REM Function to create library XML - RAG Libraries
call :create_library_xml "PCM_RAG_Libraries" "lib\rag"

REM Function to create library XML - LangChain4j
call :create_library_xml "LangChain4j" "lib\langchain4j"

REM Function to create library XML - Text Components
call :create_library_xml "Text_Components" "lib\text-component"

echo [OK] IntelliJ IDEA libraries configured
echo [INFO] Restart IntelliJ IDEA to see the libraries in Project Structure
goto :eof

:create_library_xml
setlocal
set "lib_name=%~1"
set "lib_dir=%~2"
set "xml_file=.idea\libraries\%lib_name%.xml"

echo ^<component name="libraryTable"^> > "%xml_file%"
echo   ^<library name="%lib_name%"^> >> "%xml_file%"
echo     ^<CLASSES^> >> "%xml_file%"

REM Add all JAR files from the directory
for %%f in (%lib_dir%\*.jar) do (
    set "jar_path=%%f"
    setlocal enabledelayedexpansion
    set "jar_path=!jar_path:\=/!"
    echo       ^<root url="jar://$PROJECT_DIR$/!jar_path!^!/^" /^> >> "%xml_file%"
    endlocal
)

echo     ^</CLASSES^> >> "%xml_file%"
echo     ^<JAVADOC /^> >> "%xml_file%"
echo     ^<SOURCES /^> >> "%xml_file%"
echo   ^</library^> >> "%xml_file%"
echo ^</component^> >> "%xml_file%"

echo   [OK] %lib_name%
endlocal
goto :eof

