# 🚀 Quick Start Guide - PCM Desktop

## Bước 1: Cài Đặt Prerequisites

### Install Java 17

**macOS:**

```bash
brew install openjdk@17
```

**Windows:**
Download from [Adoptium](https://adoptium.net/) hoặc [Oracle](https://www.oracle.com/java/technologies/downloads/)

**Linux:**

```bash
sudo apt install openjdk-17-jdk
```

### Verify Installation

```bash
java -version
# Output: openjdk version "17.0.x"
```

### Install Maven (if not installed)

**macOS:**

```bash
brew install maven
```

**Windows:**
Download from [Maven](https://maven.apache.org/download.cgi)

**Linux:**

```bash
sudo apt install maven
```

---

## Bước 2: Build Project

```bash
cd apps/pcm-desktop
mvn clean install
```

**Expected Output:**

```
[INFO] BUILD SUCCESS
[INFO] Total time: 30 s
```

---

## Bước 3: Run Application

### Option 1: Maven (Recommended for Development)

```bash
mvn javafx:run
```

### Option 2: IDE (IntelliJ IDEA)

1. Open `pcm-desktop` in IntelliJ
2. Wait for Maven import
3. Right-click `PCMApplication.java` → Run

### Option 3: Executable JAR

```bash
mvn clean package
java -jar target/pcm-desktop-1.0.0.jar
```

---

## Bước 4: Verify Application

✅ Window opens with title "PCM - Personal Content Manager"  
✅ Menu bar visible (File, Edit, View, Help)  
✅ Navigation tree on left side  
✅ Welcome tab in center  
✅ Status bar at bottom

---

## 📝 Development Workflow

### 1. Make Changes

Edit Java files in `src/main/java/` or FXML in `src/main/resources/fxml/`

### 2. Run with Hot Reload

```bash
mvn javafx:run
```

Stop (Ctrl+C) and restart to see changes.

### 3. Run Tests

```bash
mvn test
```

### 4. Check Code Style

```bash
mvn checkstyle:check
```

---

## 🎨 Customize UI

### Edit Layout

Edit `src/main/resources/fxml/MainView.fxml`

### Edit Styles

Edit `src/main/resources/css/styles.css`

### Add New View

1. Create FXML: `src/main/resources/fxml/MyView.fxml`
2. Create Controller: `src/main/java/com/noteflix/pcm/ui/MyViewController.java`
3. Link in FXML: `fx:controller="com.noteflix.pcm.ui.MyViewController"`

---

## 🐛 Troubleshooting

### Error: "JavaFX runtime components are missing"

**Solution:**

```bash
mvn clean install
mvn javafx:run
```

### Error: "No compiler is provided"

**Solution:** Ensure JAVA_HOME points to JDK (not JRE)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Error: "Cannot resolve symbol 'log'"

**Solution:** Enable annotation processing in IntelliJ:

1. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. Check "Enable annotation processing"

---

## 📦 Build Distributable

### Create Native Executable

```bash
mvn javafx:jlink
```

Output: `target/pcm-desktop/` (native app)

### Create Installer

**Windows (.exe):**

```bash
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type exe
```

**macOS (.dmg):**

```bash
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type dmg
```

**Linux (.deb):**

```bash
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type deb
```

---

## 🎯 Next Steps

1. ✅ Application runs successfully
2. 📝 Add your business logic in `domain/` and `application/`
3. 🎨 Customize UI in FXML and CSS
4. 🧪 Write unit tests
5. 📦 Create distributable package

---

## 📚 Useful Commands

| Command            | Description           |
|--------------------|-----------------------|
| `mvn clean`        | Clean build artifacts |
| `mvn compile`      | Compile source code   |
| `mvn test`         | Run tests             |
| `mvn package`      | Create JAR file       |
| `mvn javafx:run`   | Run application       |
| `mvn javafx:jlink` | Create native image   |

---

## 🤝 Need Help?

- Check `README.md` for detailed documentation
- Review JavaFX docs: https://openjfx.io/
- Check Maven docs: https://maven.apache.org/

---

**Happy Coding! 🎉**

