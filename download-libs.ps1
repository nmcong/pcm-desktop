# PCM Desktop - Library Download Script (PowerShell)
# Downloads all required libraries (latest versions)

Write-Host "📦 Downloading PCM Desktop Libraries..." -ForegroundColor Cyan
Write-Host ""

# Create directories
New-Item -ItemType Directory -Force -Path "lib\javafx" | Out-Null
New-Item -ItemType Directory -Force -Path "lib\others" | Out-Null

Set-Location "lib\others"

Write-Host "1️⃣ Downloading Lombok 1.18.34..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://projectlombok.org/downloads/lombok.jar" -OutFile "lombok-1.18.34.jar"

Write-Host "2️⃣ Downloading Jackson 2.17.2..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar" -OutFile "jackson-databind-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar" -OutFile "jackson-core-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar" -OutFile "jackson-annotations-2.17.2.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.17.2/jackson-datatype-jsr310-2.17.2.jar" -OutFile "jackson-datatype-jsr310-2.17.2.jar"

Write-Host "3️⃣ Downloading SLF4J 2.0.13..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar" -OutFile "slf4j-api-2.0.13.jar"

Write-Host "4️⃣ Downloading Logback 1.5.6..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.5.6/logback-classic-1.5.6.jar" -OutFile "logback-classic-1.5.6.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.5.6/logback-core-1.5.6.jar" -OutFile "logback-core-1.5.6.jar"

Write-Host "5️⃣ Downloading SQLite JDBC 3.46.1.0..." -ForegroundColor Yellow
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar" -OutFile "sqlite-jdbc-3.46.1.0.jar"

Set-Location "..\..\"

Write-Host ""
Write-Host "✅ Library download complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Downloaded libraries in lib\others\:" -ForegroundColor Cyan
Get-ChildItem "lib\others\" | Format-Table Name, Length -AutoSize

Write-Host ""
Write-Host "⚠️  Manual Step Required:" -ForegroundColor Red
Write-Host "   JavaFX 23 must be downloaded manually from:"
Write-Host "   https://gluonhq.com/products/javafx/"
Write-Host ""
Write-Host "   Download: openjfx-23_windows-x64_bin-sdk.zip"
Write-Host ""
Write-Host "   Then extract and copy all .jar files from 'lib\' folder to:"
Write-Host "   .\lib\javafx\"
Write-Host ""
Write-Host "🚀 After adding JavaFX JARs, you can run the application!" -ForegroundColor Green

