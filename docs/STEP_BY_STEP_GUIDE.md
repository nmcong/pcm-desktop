# 🎓 Hướng Dẫn Từng Bước - PCM Desktop

## 📌 Tổng Quan

Bạn vừa tạo xong một ứng dụng **Java Desktop** hoàn chỉnh với **JavaFX**!

---

## ✅ Bước 1: Xác Nhận Project Đã Tạo

```bash
cd /Users/nguyencong/Workspace/noteflix/apps/pcm-desktop
ls -la
```

**Kết quả mong đợi:**
```
✅ pom.xml
✅ src/
✅ README.md
✅ QUICK_START.md
✅ .gitignore
```

---

## ✅ Bước 2: Kiểm Tra Java Version

```bash
java -version
```

**Yêu cầu:** Java 17 trở lên

**Nếu chưa có Java 17:**
```bash
# macOS
brew install openjdk@17

# Verify
java -version
```

---

## ✅ Bước 3: Build Project Lần Đầu

```bash
cd /Users/nguyencong/Workspace/noteflix/apps/pcm-desktop
mvn clean install
```

**Thời gian:** ~30 giây (lần đầu download dependencies)

**Kết quả mong đợi:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30 s
```

✅ **PASSED** - Build thành công!

---

## ✅ Bước 4: Chạy Application

### Cách 1: Maven (Recommended)

```bash
mvn javafx:run
```

**Thời gian:** ~3-5 giây

### Cách 2: IntelliJ IDEA

1. Mở IntelliJ IDEA
2. File → Open → Chọn thư mục `pcm-desktop`
3. Đợi Maven import xong (bottom right)
4. Navigate: `src/main/java/com/noteflix/pcm/PCMApplication.java`
5. Right-click → Run 'PCMApplication.main()'

### Cách 3: JAR File

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/pcm-desktop-1.0.0.jar
```

---

## 🎨 Bước 5: Xem Kết Quả

Khi application chạy, bạn sẽ thấy:

```
╔════════════════════════════════════════════════════════╗
║  PCM - Personal Content Manager                    [_][□][X] 
╠════════════════════════════════════════════════════════╣
║  File  Edit  View  Help                                ║
╟────────────────────────────────────────────────────────╢
║  [New] [Open] [Save] | [Cut] [Copy] [Paste]           ║
╠══════════════╦═══════════════════════════════════════════╣
║ Navigation   ║          Welcome                      [x] ║
║──────────────║─────────────────────────────────────────║
║ 📊 Dashboard ║                                          ║
║ 📁 Projects  ║        Welcome to PCM                     ║
║ 📝 Notes     ║   Personal Content Manager                ║
║ ✓ Tasks      ║                                          ║
║ ⚙️ Settings  ║        [Get Started]                      ║
║              ║                                          ║
║              ║                                          ║
╠══════════════╩═══════════════════════════════════════════╣
║  Ready                            PCM Desktop v1.0.0    ║
╚════════════════════════════════════════════════════════╝
```

**Features:**
- ✅ Menu bar (File, Edit, View, Help)
- ✅ Tool bar với buttons
- ✅ Navigation tree (left side)
- ✅ Tab-based content area
- ✅ Status bar (bottom)
- ✅ Welcome screen

---

## 🧪 Bước 6: Test Features

### Test 1: Click Menu Items

1. Click **File → About**
2. Xem dialog hiện lên
3. Click OK

### Test 2: Click Navigation Items

1. Click **📁 Projects** trong navigation tree
2. Tab mới "Projects" mở ra
3. Click **📝 Notes**
4. Tab mới "Notes" mở ra

### Test 3: Click Get Started

1. Click button **Get Started**
2. Dialog hiện thông tin welcome
3. Click OK

---

## 📝 Bước 7: Customize Application

### A. Thay Đổi Title

**File:** `src/main/java/com/noteflix/pcm/PCMApplication.java`

```java
// Line 17: Thay đổi title
private static final String APP_TITLE = "My Awesome App";
```

**Save và Run lại:**
```bash
mvn javafx:run
```

### B. Thay Đổi Colors

**File:** `src/main/resources/css/styles.css`

```css
/* Line 3: Thay đổi base color */
.root {
    -fx-base: #e3f2fd;  /* Light blue */
}

/* Welcome title color */
.welcome-title {
    -fx-text-fill: #00796b;  /* Teal */
}
```

**Save và Run lại.**

### C. Thêm Navigation Item

**File:** `src/main/java/com/noteflix/pcm/ui/MainController.java`

Tìm method `setupNavigationTree()` và thêm:

```java
TreeItem<String> reportsItem = new TreeItem<>("📊 Reports");

rootItem.getChildren().addAll(
    dashboardItem,
    projectsItem,
    notesItem,
    tasksItem,
    reportsItem,  // ← NEW
    settingsItem
);
```

**Save và Run lại.**

---

## 🎯 Bước 8: Add Your Code

### Example: Add User Model

**Create:** `src/main/java/com/noteflix/pcm/domain/User.java`

```java
package com.noteflix.pcm.domain;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private String role;
}
```

### Example: Add User Service

**Create:** `src/main/java/com/noteflix/pcm/application/UserService.java`

```java
package com.noteflix.pcm.application;

import com.noteflix.pcm.domain.User;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserService {
    private List<User> users = new ArrayList<>();
    
    public void addUser(User user) {
        log.info("Adding user: {}", user.getName());
        users.add(user);
    }
    
    public List<User> getAllUsers() {
        return users;
    }
}
```

### Example: Use in Controller

**Edit:** `src/main/java/com/noteflix/pcm/ui/MainController.java`

```java
// Add field
private UserService userService = new UserService();

// In initialize() method
@Override
public void initialize(URL location, ResourceBundle resources) {
    log.info("Initializing Main Controller...");
    
    // Test user service
    User testUser = User.builder()
        .id(1L)
        .name("John Doe")
        .email("john@example.com")
        .role("Admin")
        .build();
    
    userService.addUser(testUser);
    log.info("Created test user: {}", testUser);
    
    setupMenuBar();
    setupNavigationTree();
    setupTabPane();
    updateStatus("Ready - " + userService.getAllUsers().size() + " users");
}
```

---

## 📦 Bước 9: Build Distribution

### Create Executable JAR

```bash
mvn clean package
```

**Output:** `target/pcm-desktop-1.0.0.jar`

**Run:**
```bash
java -jar target/pcm-desktop-1.0.0.jar
```

### Create Native Application

```bash
mvn javafx:jlink
```

**Output:** `target/pcm-desktop/` (native app)

### Create Installer (Optional)

**macOS:**
```bash
jpackage --input target/ \
  --name PCM \
  --main-jar pcm-desktop-1.0.0.jar \
  --type dmg \
  --icon src/main/resources/images/app-icon.icns
```

**Windows:**
```bash
jpackage --input target/ --name PCM --main-jar pcm-desktop-1.0.0.jar --type exe
```

---

## 🐛 Troubleshooting

### Problem 1: "JavaFX runtime components are missing"

**Solution:**
```bash
mvn clean install
mvn javafx:run
```

### Problem 2: "JAVA_HOME not set"

**Solution:**
```bash
# Check current Java
java -version

# Set JAVA_HOME (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Add to ~/.zshrc or ~/.bash_profile
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
```

### Problem 3: "Cannot find symbol 'log'"

**Solution:** Enable annotation processing in IntelliJ:
1. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
2. ✅ Enable annotation processing
3. Rebuild project

---

## 📚 Learning Resources

### Tutorials

1. **JavaFX Basics:**
   - [Official JavaFX Tutorial](https://openjfx.io/openjfx-docs/)
   - [JavaFX Documentation](https://docs.oracle.com/javafx/2/)

2. **Scene Builder:**
   - Download: [Scene Builder](https://gluonhq.com/products/scene-builder/)
   - Visual FXML editor

3. **CSS Styling:**
   - [JavaFX CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)

### Examples

1. **More Examples:**
   ```bash
   # Clone JavaFX samples
   git clone https://github.com/openjfx/samples
   ```

2. **Community Projects:**
   - [Awesome JavaFX](https://github.com/mhrimaz/AwesomeJavaFX)

---

## ✅ Checklist

Before moving forward:

- [ ] Application runs successfully
- [ ] Can see main window
- [ ] Menu items work
- [ ] Navigation tree works
- [ ] Can add custom code
- [ ] Build creates JAR
- [ ] Understand project structure

---

## 🎉 Success!

Bạn đã tạo xong **Java Desktop Application**!

**Next Steps:**
1. ✅ Run application: `mvn javafx:run`
2. 📝 Add your business logic
3. 🎨 Customize UI
4. 🧪 Write tests
5. 📦 Create installer

---

## 💡 Pro Tips

1. **Hot Reload:** Use `mvn javafx:run` - quick restart cycle
2. **Debugging:** Run from IDE with breakpoints
3. **Scene Builder:** Visual FXML editing
4. **Logging:** Check `logs/pcm-desktop.log` for detailed logs
5. **Database:** SQLite file in `~/.pcm/pcm.db`

---

**🚀 Ready to code! Happy developing!**

