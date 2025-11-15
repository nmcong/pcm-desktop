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

echo [INFO] Cleaning old library directory...
if exist "lib" (
    rmdir /s /q lib
    echo [OK] Old library directory removed
)
echo.

echo [INFO] Creating library directories...
if not exist "lib\javafx" mkdir lib\javafx
if not exist "lib\others" mkdir lib\others
if not exist "lib\rag" mkdir lib\rag
if not exist "lib\text-component" mkdir lib\text-component
echo [OK] Directories created
echo.

REM Download core libraries
echo ========================================
echo    Downloading Core Libraries
echo ========================================
echo.

cd lib\others

echo [INFO] 1. Downloading Lombok
curl -L -o lombok-1.18.34.jar https://projectlombok.org/downloads/lombok.jar
if %ERRORLEVEL%==0 (
    echo [OK] Lombok downloaded
) else (
    echo [ERROR] Failed to download Lombok
)
echo.

echo [INFO] 2. Downloading Jackson
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.20.1/jackson-databind-2.20.1.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.20.1/jackson-core-2.20.1.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar
curl -O https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.20.1/jackson-datatype-jsr310-2.20.1.jar
if %ERRORLEVEL%==0 (
    echo [OK] Jackson downloaded
) else (
    echo [ERROR] Failed to download Jackson
)
echo.

echo [INFO] 3. Downloading SLF4J
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar
if %ERRORLEVEL%==0 (
    echo [OK] SLF4J downloaded
) else (
    echo [ERROR] Failed to download SLF4J
)
echo.

echo [INFO] 4. Downloading Logback
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.21/logback-classic-1.5.21.jar
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.21/logback-core-1.5.21.jar
if %ERRORLEVEL%==0 (
    echo [OK] Logback downloaded
) else (
    echo [ERROR] Failed to download Logback
)
echo.

echo [INFO] 5. Downloading SQLite JDBC
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.0.0/sqlite-jdbc-3.51.0.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] SQLite JDBC downloaded
) else (
    echo [ERROR] Failed to download SQLite JDBC
)
echo.

echo [INFO] 6. Downloading Oracle OJDBC
curl -L -o ojdbc11-23.26.0.0.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.0.0.0/ojdbc11-23.26.0.0.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] Oracle OJDBC downloaded
) else (
    echo [ERROR] Failed to download Oracle OJDBC
)
echo.

echo [INFO] 7. Downloading HikariCP
curl -O https://repo1.maven.org/maven2/com/zaxxer/HikariCP/7.0.2/HikariCP-7.0.2.jar
if %ERRORLEVEL%==0 (
    echo [OK] HikariCP downloaded
) else (
    echo [ERROR] Failed to download HikariCP
)
echo.

echo [INFO] 8. Downloading Oracle UCP
curl -L -o ucp-23.26.0.0.0.jar https://repo1.maven.org/maven2/com/oracle/database/jdbc/ucp/23.26.0.0.0/ucp-23.26.0.0.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] Oracle UCP downloaded
) else (
    echo [ERROR] Failed to download Oracle UCP
)
echo.

REM Download AtlantaFX
echo [INFO] 9. Downloading AtlantaFX
curl -L -o atlantafx-base-2.1.0.jar https://repo1.maven.org/maven2/io/github/mkpaz/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] AtlantaFX downloaded
) else (
    echo [ERROR] Failed to download AtlantaFX
)
echo.

REM Download Ikonli
echo [INFO] 10. Downloading Ikonli
curl -L -o ikonli-core-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-core/12.4.0/ikonli-core-12.4.0.jar
curl -L -o ikonli-javafx-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-javafx/12.4.0/ikonli-javafx-12.4.0.jar
curl -L -o ikonli-feather-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-feather-pack/12.4.0/ikonli-feather-pack-12.4.0.jar
curl -L -o ikonli-antdesignicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-antdesignicons-pack/12.4.0/ikonli-antdesignicons-pack-12.4.0.jar
curl -L -o ikonli-bpmn-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-bpmn-pack/12.4.0/ikonli-bpmn-pack-12.4.0.jar
curl -L -o ikonli-octicons-pack-12.4.0.jar https://repo1.maven.org/maven2/org/kordamp/ikonli/ikonli-octicons-pack/12.4.0/ikonli-octicons-pack-12.4.0.jar
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

echo [INFO] 1. Downloading Apache Lucene
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-core/10.3.1/lucene-core-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-analysis-common/10.3.1/lucene-analysis-common-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queryparser/10.3.1/lucene-queryparser-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-queries/10.3.1/lucene-queries-10.3.1.jar
curl -O https://repo1.maven.org/maven2/org/apache/lucene/lucene-highlighter/10.3.1/lucene-highlighter-10.3.1.jar
if %ERRORLEVEL%==0 (
    echo [OK] Apache Lucene downloaded
) else (
    echo [ERROR] Failed to download Apache Lucene
)
echo.

echo [INFO] 2. Downloading DJL ONNX Runtime
curl -O https://repo1.maven.org/maven2/ai/djl/onnxruntime/onnxruntime-engine/0.35.0/onnxruntime-engine-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/api/0.35.0/api-0.35.0.jar
curl -O https://repo1.maven.org/maven2/ai/djl/huggingface/tokenizers/0.35.0/tokenizers-0.35.0.jar
if %ERRORLEVEL%==0 (
    echo [OK] DJL ONNX Runtime downloaded
) else (
    echo [ERROR] Failed to download DJL ONNX Runtime
)
echo.

echo [INFO] 3. Downloading ONNX Runtime
curl -L -o onnxruntime-1.23.2.jar https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.23.2/onnxruntime-1.23.2.jar
if %ERRORLEVEL%==0 (
    echo [OK] ONNX Runtime downloaded
) else (
    echo [ERROR] Failed to download ONNX Runtime
)
echo.

echo [INFO] 4. Downloading JavaParser
curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-core/3.27.1/javaparser-core-3.27.1.jar
curl -O https://repo1.maven.org/maven2/com/github/javaparser/javaparser-symbol-solver-core/3.27.1/javaparser-symbol-solver-core-3.27.1.jar
if %ERRORLEVEL%==0 (
    echo [OK] JavaParser downloaded
) else (
    echo [ERROR] Failed to download JavaParser
)
echo.

cd ..\..

echo [OK] RAG libraries downloaded successfully!
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

echo [INFO] 1. Downloading RichTextFX
curl -O https://repo1.maven.org/maven2/org/fxmisc/richtext/richtextfx/0.11.6/richtextfx-0.11.6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar
curl -O https://repo1.maven.org/maven2/org/reactfx/reactfx/2.0-M6/reactfx-2.0-M6.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar
curl -O https://repo1.maven.org/maven2/org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar
if %ERRORLEVEL%==0 (
    echo [OK] RichTextFX and dependencies downloaded
) else (
    echo [ERROR] Failed to download RichTextFX
)
echo.

cd ..\..

echo [OK] Text Component libraries downloaded successfully!
echo.

REM Summary
echo ========================================
echo    Setup Completed Successfully!
echo ========================================
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
echo   Text Component Libraries (lib\text-component\):
dir /b lib\text-component\*.jar 2>nul
echo.

echo [INFO] Next Steps:
echo   1. Run: scripts\build.bat
echo   2. Run: scripts\run.bat
echo.

pause
