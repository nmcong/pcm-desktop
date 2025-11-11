# PCM Desktop Application

Personal Content Manager - Desktop Application built with JavaFX

## 📋 Overview

PCM Desktop is a cross-platform desktop application for personal content management. Built with JavaFX, it provides a modern, intuitive interface for organizing projects, notes, tasks, and more.

## ✨ Features

- 📊 **Dashboard** - Overview of your content
- 📁 **Projects** - Manage your projects
- 📝 **Notes** - Quick note-taking
- ✓ **Tasks** - Task management
- ⚙️ **Settings** - Customize your experience

## 🛠️ Tech Stack

- **Java 17** - Programming language
- **JavaFX 21** - UI framework
- **Maven** - Build tool
- **Lombok** - Reduce boilerplate code
- **Jackson** - JSON processing
- **SQLite** - Local database
- **Logback** - Logging

## 📦 Project Structure

```
pcm-desktop/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/noteflix/pcm/
│   │   │       ├── PCMApplication.java          # Main entry point
│   │   │       ├── ui/                          # UI Controllers
│   │   │       │   └── MainController.java
│   │   │       ├── domain/                      # Domain models
│   │   │       ├── application/                 # Application services
│   │   │       └── infrastructure/              # Infrastructure layer
│   │   └── resources/
│   │       ├── fxml/                            # FXML layouts
│   │       │   └── MainView.fxml
│   │       ├── css/                             # Stylesheets
│   │       │   └── styles.css
│   │       ├── images/                          # Images & icons
│   │       └── logback.xml                      # Logging config
│   └── test/
│       └── java/                                # Unit tests
├── pom.xml                                      # Maven configuration
└── README.md                                    # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **IDE** (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository:**
   ```bash
   cd apps/pcm-desktop
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn javafx:run
   ```

## 📖 Usage

### Running from IDE

1. Open project in IntelliJ IDEA
2. Run `PCMApplication.java`

### Running from Maven

```bash
mvn javafx:run
```

### Building Executable JAR

```bash
mvn clean package
java -jar target/pcm-desktop-1.0.0.jar
```

### Creating Native Installer

```bash
mvn javafx:jlink
```

This creates a native executable in `target/pcm-desktop/`

## 🧪 Testing

Run tests:
```bash
mvn test
```

## 📝 Development

### Adding New Features

1. Create model in `domain/` package
2. Create service in `application/` package
3. Create controller in `ui/` package
4. Create FXML layout in `resources/fxml/`
5. Add styles in `resources/css/`

### Code Style

- Use Lombok for reducing boilerplate
- Follow Clean Architecture principles
- Write unit tests for business logic
- Use meaningful variable names
- Add JavaDoc for public methods

### Logging

Use SLF4J with Logback:
```java
@Slf4j
public class MyClass {
    public void myMethod() {
        log.info("Log message");
        log.debug("Debug message");
        log.error("Error message", exception);
    }
}
```

## 🔧 Configuration

### Application Properties

Edit `src/main/resources/application.properties` for configuration.

### Database

SQLite database is created automatically at:
- Windows: `%USERPROFILE%/.pcm/pcm.db`
- macOS/Linux: `~/.pcm/pcm.db`

## 📦 Building for Production

### Windows

```bash
mvn clean package
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type exe
```

### macOS

```bash
mvn clean package
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type dmg
```

### Linux

```bash
mvn clean package
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type deb
```

## 🐛 Troubleshooting

### JavaFX not found

Make sure you're using Java 17+. JavaFX is included in the build.

### Can't run application

Check Java version:
```bash
java -version
# Should show Java 17 or higher
```

### IDE doesn't recognize JavaFX classes

Enable annotation processing in IDE settings for Lombok.

## 📚 Resources

- [JavaFX Documentation](https://openjfx.io/)
- [Maven JavaFX Plugin](https://github.com/openjfx/javafx-maven-plugin)
- [Lombok Documentation](https://projectlombok.org/)

## 🤝 Contributing

1. Create feature branch
2. Make changes
3. Write tests
4. Submit pull request

## 📄 License

Private project - All rights reserved

## 👤 Author

Noteflix Team

---

**Built with ❤️ using JavaFX**

