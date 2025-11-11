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
Write-Host "6️⃣ Downloading JavaFX 21.0.9 for Windows..." -ForegroundColor Yellow
$javafxUrl = "https://download2.gluonhq.com/openjfx/21.0.9/openjfx-21.0.9_windows-x64_bin-sdk.zip"
$javafxZip = "javafx-21.zip"

try {
    Invoke-WebRequest -Uri $javafxUrl -OutFile $javafxZip
    
    Write-Host "7️⃣ Extracting JavaFX..." -ForegroundColor Yellow
    Expand-Archive -Path $javafxZip -DestinationPath "." -Force
    
    Write-Host "8️⃣ Installing JavaFX JARs..." -ForegroundColor Yellow
    Copy-Item "javafx-sdk-21.0.9\lib\*.jar" -Destination "lib\javafx\" -Force
    
    Write-Host "9️⃣ Cleaning up..." -ForegroundColor Yellow
    Remove-Item $javafxZip
    Remove-Item "javafx-sdk-21.0.9" -Recurse -Force
    
    Write-Host ""
    Write-Host "✅ JavaFX 21.0.9 installed successfully!" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "⚠️  Failed to download JavaFX automatically!" -ForegroundColor Red
    Write-Host "   Please download manually from:"
    Write-Host "   $javafxUrl"
    Write-Host ""
    Write-Host "   Steps:"
    Write-Host "   1. Download: openjfx-21.0.9_windows-x64_bin-sdk.zip"
    Write-Host "   2. Extract the ZIP file"
    Write-Host "   3. Copy all .jar files from 'javafx-sdk-21.0.9\lib\' to: .\lib\javafx\"
}

Write-Host ""
Write-Host "📋 Installed JavaFX libraries:" -ForegroundColor Cyan
Get-ChildItem "lib\javafx\" | Format-Table Name, Length -AutoSize

Write-Host ""
Write-Host "🚀 All libraries downloaded! You can now run the application!" -ForegroundColor Green

