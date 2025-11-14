# PCM Desktop Scripts

This directory contains utility scripts for building, running, and managing the PCM Desktop application.

## 📁 Available Scripts

### Build & Run Scripts

#### macOS
- **`compile-macos.command`** - Compiles the Java source code on macOS
- **`run-macos.command`** - Runs the PCM Desktop application on macOS

#### Windows
- **`compile-windows.bat`** - Compiles the Java source code on Windows
- **`run-windows.bat`** - Runs the PCM Desktop application on Windows

### Library Management

- **`download-libs.sh`** - Downloads required Java libraries (macOS/Linux)
- **`download-libs.ps1`** - Downloads required Java libraries (Windows PowerShell)
- **`verify-libs.sh`** - Verifies that all required libraries are present

### Resource Management

- **`copy-icons-to-build.sh`** - Copies icon files from src to build directory
- **`copy-resources-to-build.sh`** - Copies all resource files (CSS, FXML, images) to build directory

### Icon Management

- **`download-lucide-icon.sh`** - Downloads Lucide icons for the application

## 🚀 Quick Start

### macOS/Linux

```bash
# From project root:

# 1. Download libraries (first time only)
./scripts/download-libs.sh

# 2. Compile the application
./scripts/compile-macos.command

# 3. Run the application
./scripts/run-macos.command

# Or simply double-click the .command files in Finder
```

### Windows

```cmd
# From project root:

# 1. Download libraries (first time only)
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1

# 2. Compile the application
scripts\compile-windows.bat

# 3. Run the application
scripts\run-windows.bat

# Or simply double-click the .bat files in Explorer
```

## 📦 Resource & Library Management

### Copy Resources to Build

After modifying resources (CSS, FXML, images), copy them to the build directory:

```bash
# Copy all resources
./scripts/copy-resources-to-build.sh

# Or copy only icons
./scripts/copy-icons-to-build.sh
```

Note: The compile and run scripts already handle resource copying automatically.

### Verify Libraries

Check if all required libraries are present:

```bash
./scripts/verify-libs.sh
```

### Download Icons

Download Lucide icons for the UI:

```bash
# Download a single icon (24px, black)
./scripts/download-lucide-icon.sh home

# Download with custom size and color
./scripts/download-lucide-icon.sh -s 32 -c ff0000 heart

# List available icons
./scripts/download-lucide-icon.sh --list

# Show help
./scripts/download-lucide-icon.sh --help
```

## 🔧 Requirements

- **Java 21** (JDK for compilation, JRE for running)
- **JavaFX 21.0.9** (downloaded automatically by download-libs scripts)
- **macOS**: bash shell (pre-installed)
- **Windows**: PowerShell (for download-libs.ps1)

## 📝 Notes

- All scripts automatically change to the project root directory
- Scripts can be run from any location
- The `out/` directory contains compiled classes and resources
- Libraries are stored in the `lib/` directory

## 🐛 Troubleshooting

### "Permission denied" on macOS

Make scripts executable:

```bash
chmod +x scripts/*.sh scripts/*.command
```

### "Cannot be opened because it is from an unidentified developer" on macOS

Right-click the .command file and select "Open" the first time.

### JavaFX not found

Run the download-libs script:

```bash
./scripts/download-libs.sh  # macOS/Linux
```

or

```powershell
powershell -ExecutionPolicy Bypass -File scripts\download-libs.ps1  # Windows
```

## 📚 More Information

See the main [README.md](../README.md) and documentation in the [docs/](../docs/) directory.

