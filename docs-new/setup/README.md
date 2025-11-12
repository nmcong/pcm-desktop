# Setup & Configuration

This section contains guides for setting up your development environment for PCM Desktop.

## 📋 Setup Checklist

Follow these guides in order to get your development environment ready:

### 1. **[Development Environment](development-environment.md)**
Complete setup guide for development environment including Java, IDE, and tools.

### 2. **[IntelliJ IDEA Setup](intellij-setup.md)**
Configure IntelliJ IDEA for PCM Desktop development with proper library recognition.

### 3. **[Library Setup](library-setup.md)**
Manual download and setup of all required libraries (JavaFX, Lombok, AtlantaFX, etc.).

### 4. **[Run Configuration](run-configuration.md)**
Configure IntelliJ run settings to properly launch the JavaFX application.

## 🎯 Quick Start Path

For the fastest setup:

1. **Prerequisites**: Ensure Java 17+ is installed
2. **Download Libraries**: Run the library download script from [Library Setup](library-setup.md)
3. **Configure IntelliJ**: Follow [IntelliJ Setup](intellij-setup.md) to import libraries
4. **Run Application**: Configure and test with [Run Configuration](run-configuration.md)

## ⚡ Common Issues

### Library Recognition Problems
- **Solution**: Follow the cache invalidation steps in [IntelliJ Setup](intellij-setup.md)

### JavaFX Runtime Missing  
- **Solution**: Ensure VM options are properly set in [Run Configuration](run-configuration.md)

### Build Failures
- **Solution**: Verify all libraries are present using the verification script in [Library Setup](library-setup.md)

## 🛠️ Platform-Specific Notes

### Windows
- Use PowerShell for script execution
- Ensure proper path separators in configurations

### macOS
- Different JavaFX downloads for Intel vs M1/M2/M3 chips
- Use Terminal for bash scripts

### Linux
- Install packages via package manager where available
- Ensure proper file permissions for scripts

## 📚 Additional Resources

- [JavaFX Documentation](https://openjfx.io/)
- [AtlantaFX Theming Guide](https://github.com/mkpaz/atlantafx)
- [IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/)

---

Need help? Check the [troubleshooting section](../troubleshooting/) or review the specific setup guide for your issue.