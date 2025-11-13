# System Tray Integration và Auto-startup Guide

## Table of Contents
1. [Overview](#overview)
2. [System Tray Implementation](#system-tray-implementation)
3. [Auto-startup Configuration](#auto-startup-configuration)
4. [Cross-Platform Considerations](#cross-platform-considerations)
5. [Configuration Management](#configuration-management)
6. [User Experience Guidelines](#user-experience-guidelines)
7. [Testing & Validation](#testing--validation)

## Overview

### System Requirements
- **Java**: 17+ with JavaFX support
- **Platforms**: Windows 10/11, macOS 10.14+, Linux (GNOME/KDE)
- **Dependencies**: AWT System Tray support

### Features to Implement
- **System Tray Icon**: Ứng dụng thu nhỏ xuống system tray
- **Auto-startup**: Tự động khởi động khi boot system
- **Context Menu**: Menu chuột phải trên tray icon
- **Notifications**: Hiển thị thông báo từ system tray
- **Double-click Restore**: Double-click để khôi phục cửa sổ

## System Tray Implementation

### 1. Dependencies Required

#### Maven Dependencies
```xml
<dependencies>
    <!-- JavaFX for UI -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- AWT Tray Support -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-swing</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- Auto-startup utilities -->
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna</artifactId>
        <version>5.13.0</version>
    </dependency>
    
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna-platform</artifactId>
        <version>5.13.0</version>
    </dependency>
</dependencies>
```

#### Module Info Updates
```java
// module-info.java
module com.noteflix.pcm {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    
    exports com.noteflix.pcm;
    exports com.noteflix.pcm.tray;
}
```

### 2. System Tray Manager Implementation

#### Core Tray Manager Class
```java
package com.noteflix.pcm.tray;

import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

@Slf4j
public class SystemTrayManager {
    
    private static SystemTrayManager instance;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private Stage primaryStage;
    private boolean isMinimizedToTray = false;
    
    // Singleton pattern
    public static SystemTrayManager getInstance() {
        if (instance == null) {
            synchronized (SystemTrayManager.class) {
                if (instance == null) {
                    instance = new SystemTrayManager();
                }
            }
        }
        return instance;
    }
    
    private SystemTrayManager() {
        // Private constructor for singleton
    }
    
    /**
     * Initialize system tray functionality
     * @param stage Primary JavaFX stage
     * @return true if initialization successful
     */
    public boolean initialize(Stage stage) {
        this.primaryStage = stage;
        
        // Check if system tray is supported
        if (!SystemTray.isSupported()) {
            log.warn("System tray is not supported on this platform");
            return false;
        }
        
        try {
            // Get system tray instance
            systemTray = SystemTray.getSystemTray();
            
            // Create tray icon
            createTrayIcon();
            
            // Add to system tray
            systemTray.add(trayIcon);
            
            // Setup stage event handlers
            setupStageEventHandlers();
            
            log.info("System tray initialized successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Failed to initialize system tray", e);
            return false;
        }
    }
    
    /**
     * Create and configure tray icon
     */
    private void createTrayIcon() throws IOException {
        // Load tray icon image
        BufferedImage trayIconImage = loadTrayIcon();
        
        // Create popup menu
        PopupMenu popup = createPopupMenu();
        
        // Create tray icon
        trayIcon = new TrayIcon(trayIconImage, "PCM Desktop", popup);
        trayIcon.setImageAutoSize(true);
        
        // Add double-click listener to restore window
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Platform.runLater(() -> restoreWindow());
                }
            }
        });
        
        // Add action listener for single click (alternative restore)
        trayIcon.addActionListener(e -> Platform.runLater(() -> restoreWindow()));
    }
    
    /**
     * Load appropriate tray icon based on platform and DPI
     */
    private BufferedImage loadTrayIcon() throws IOException {
        // Determine appropriate icon size
        Dimension trayIconSize = systemTray.getTrayIconSize();
        String iconPath = determineIconPath(trayIconSize);
        
        // Load icon from resources
        try (InputStream iconStream = getClass().getResourceAsStream(iconPath)) {
            if (iconStream == null) {
                throw new IOException("Tray icon not found: " + iconPath);
            }
            
            BufferedImage image = ImageIO.read(iconStream);
            
            // Scale image if necessary
            if (image.getWidth() != trayIconSize.width || image.getHeight() != trayIconSize.height) {
                Image scaledImage = image.getScaledInstance(
                    trayIconSize.width, 
                    trayIconSize.height, 
                    Image.SCALE_SMOOTH
                );
                
                BufferedImage scaledBufferedImage = new BufferedImage(
                    trayIconSize.width,
                    trayIconSize.height,
                    BufferedImage.TYPE_INT_ARGB
                );
                
                Graphics2D g2d = scaledBufferedImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
                
                return scaledBufferedImage;
            }
            
            return image;
        }
    }
    
    /**
     * Determine appropriate icon path based on platform and size
     */
    private String determineIconPath(Dimension size) {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("mac")) {
            // macOS prefers template images (black and white)
            return "/icons/tray/macos/icon-template.png";
        } else if (os.contains("win")) {
            // Windows supports color icons
            if (size.width <= 16) {
                return "/icons/tray/windows/icon-16.png";
            } else {
                return "/icons/tray/windows/icon-32.png";
            }
        } else {
            // Linux/Unix
            if (size.width <= 22) {
                return "/icons/tray/linux/icon-22.png";
            } else {
                return "/icons/tray/linux/icon-32.png";
            }
        }
    }
    
    /**
     * Create popup menu for tray icon
     */
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        
        // Show/Hide menu item
        MenuItem showHideItem = new MenuItem("Show PCM");
        showHideItem.addActionListener(e -> Platform.runLater(() -> {
            if (isMinimizedToTray) {
                restoreWindow();
            } else {
                minimizeToTray();
            }
        }));
        popup.add(showHideItem);
        
        popup.addSeparator();
        
        // Settings menu item
        MenuItem settingsItem = new MenuItem("Settings");
        settingsItem.addActionListener(e -> Platform.runLater(() -> {
            restoreWindow();
            // Open settings window
            openSettings();
        }));
        popup.add(settingsItem);
        
        // About menu item
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(e -> Platform.runLater(() -> {
            showAboutDialog();
        }));
        popup.add(aboutItem);
        
        popup.addSeparator();
        
        // Exit menu item
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> Platform.runLater(() -> {
            exitApplication();
        }));
        popup.add(exitItem);
        
        return popup;
    }
    
    /**
     * Setup stage event handlers for minimize/close behavior
     */
    private void setupStageEventHandlers() {
        // Handle window state changes
        primaryStage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
            if (isIconified && shouldMinimizeToTray()) {
                Platform.runLater(() -> minimizeToTray());
            }
        });
        
        // Handle close request
        primaryStage.setOnCloseRequest(event -> {
            if (shouldMinimizeOnClose()) {
                event.consume(); // Prevent actual close
                minimizeToTray();
            } else {
                exitApplication();
            }
        });
    }
    
    /**
     * Minimize application to system tray
     */
    public void minimizeToTray() {
        if (systemTray != null && trayIcon != null) {
            primaryStage.hide();
            isMinimizedToTray = true;
            
            // Update tray icon tooltip and menu
            updateTrayIconState();
            
            // Show notification (first time only)
            showMinimizeNotification();
            
            log.debug("Application minimized to system tray");
        }
    }
    
    /**
     * Restore application from system tray
     */
    public void restoreWindow() {
        if (isMinimizedToTray) {
            primaryStage.show();
            primaryStage.setIconified(false);
            primaryStage.toFront();
            primaryStage.requestFocus();
            
            isMinimizedToTray = false;
            updateTrayIconState();
            
            log.debug("Application restored from system tray");
        }
    }
    
    /**
     * Update tray icon state and menu items
     */
    private void updateTrayIconState() {
        if (trayIcon != null) {
            PopupMenu popup = trayIcon.getPopupMenu();
            MenuItem showHideItem = popup.getItem(0);
            
            if (isMinimizedToTray) {
                showHideItem.setLabel("Show PCM");
                trayIcon.setToolTip("PCM Desktop - Click to show");
            } else {
                showHideItem.setLabel("Hide PCM");
                trayIcon.setToolTip("PCM Desktop - Click to hide");
            }
        }
    }
    
    /**
     * Show notification when minimized to tray (first time)
     */
    private void showMinimizeNotification() {
        if (!hasShownMinimizeNotification()) {
            showTrayNotification(
                "PCM Desktop",
                "Application minimized to system tray. Double-click to restore.",
                TrayIcon.MessageType.INFO
            );
            setMinimizeNotificationShown(true);
        }
    }
    
    /**
     * Show tray notification
     */
    public void showTrayNotification(String title, String message, TrayIcon.MessageType messageType) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, messageType);
        }
    }
    
    /**
     * Clean up system tray resources
     */
    public void cleanup() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            trayIcon = null;
            log.info("System tray cleaned up");
        }
    }
    
    // Configuration methods (to be implemented with your config system)
    private boolean shouldMinimizeToTray() {
        // Read from application configuration
        return ConfigurationManager.getInstance().getBoolean("ui.minimizeToTray", true);
    }
    
    private boolean shouldMinimizeOnClose() {
        return ConfigurationManager.getInstance().getBoolean("ui.minimizeOnClose", true);
    }
    
    private boolean hasShownMinimizeNotification() {
        return ConfigurationManager.getInstance().getBoolean("ui.hasShownTrayNotification", false);
    }
    
    private void setMinimizeNotificationShown(boolean shown) {
        ConfigurationManager.getInstance().setBoolean("ui.hasShownTrayNotification", shown);
    }
    
    // Placeholder methods for UI actions
    private void openSettings() {
        // Implementation depends on your settings UI
        log.info("Opening settings from tray menu");
    }
    
    private void showAboutDialog() {
        // Implementation for about dialog
        log.info("Showing about dialog from tray menu");
    }
    
    private void exitApplication() {
        cleanup();
        Platform.exit();
        System.exit(0);
    }
}
```

### 3. Integration with JavaFX Application

#### Main Application Class Updates
```java
package com.noteflix.pcm;

import com.noteflix.pcm.tray.SystemTrayManager;
import com.noteflix.pcm.startup.AutoStartupManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PCMDesktopApplication extends Application {
    
    private SystemTrayManager trayManager;
    private AutoStartupManager startupManager;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Initialize managers
        trayManager = SystemTrayManager.getInstance();
        startupManager = AutoStartupManager.getInstance();
        
        log.info("PCM Desktop Application initializing...");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Configure primary stage
        configurePrimaryStage(primaryStage);
        
        // Initialize system tray
        boolean traySupported = trayManager.initialize(primaryStage);
        if (!traySupported) {
            log.warn("System tray not supported - application will not minimize to tray");
        }
        
        // Check and configure auto-startup
        configureAutoStartup();
        
        // Load and show main scene
        loadMainScene(primaryStage);
        
        // Show stage
        primaryStage.show();
        
        log.info("PCM Desktop Application started successfully");
    }
    
    private void configurePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle("PCM Desktop");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Set application icon
        primaryStage.getIcons().addAll(loadApplicationIcons());
        
        // Configure close behavior
        primaryStage.setOnCloseRequest(event -> {
            if (trayManager != null && SystemTray.isSupported()) {
                // Let tray manager handle close request
                return;
            }
            
            // Default close behavior
            Platform.exit();
        });
    }
    
    private void configureAutoStartup() {
        try {
            // Check if auto-startup is enabled in settings
            boolean autoStartupEnabled = ConfigurationManager.getInstance()
                .getBoolean("system.autoStartup", false);
                
            if (autoStartupEnabled && !startupManager.isAutoStartupEnabled()) {
                startupManager.enableAutoStartup();
                log.info("Auto-startup enabled");
            } else if (!autoStartupEnabled && startupManager.isAutoStartupEnabled()) {
                startupManager.disableAutoStartup();
                log.info("Auto-startup disabled");
            }
        } catch (Exception e) {
            log.error("Failed to configure auto-startup", e);
        }
    }
    
    private List<Image> loadApplicationIcons() {
        List<Image> icons = new ArrayList<>();
        String[] iconSizes = {"16", "24", "32", "48", "64", "128", "256"};
        
        for (String size : iconSizes) {
            try {
                String iconPath = "/icons/app/icon-" + size + ".png";
                InputStream iconStream = getClass().getResourceAsStream(iconPath);
                if (iconStream != null) {
                    icons.add(new Image(iconStream));
                    iconStream.close();
                }
            } catch (Exception e) {
                log.warn("Failed to load application icon of size: " + size, e);
            }
        }
        
        return icons;
    }
    
    @Override
    public void stop() throws Exception {
        // Cleanup resources
        if (trayManager != null) {
            trayManager.cleanup();
        }
        
        super.stop();
        log.info("PCM Desktop Application stopped");
    }
    
    public static void main(String[] args) {
        // Set system properties for better integration
        setSystemProperties();
        
        // Launch JavaFX application
        launch(args);
    }
    
    private static void setSystemProperties() {
        // Enable system tray on Linux
        System.setProperty("java.awt.headless", "false");
        
        // Improve font rendering
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        // macOS specific settings
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "PCM Desktop");
            System.setProperty("apple.awt.application.appearance", "system");
        }
    }
}
```

## Auto-startup Configuration

### 1. Auto-startup Manager Implementation

#### Cross-platform Auto-startup Manager
```java
package com.noteflix.pcm.startup;

import lombok.extern.slf4j.Slf4j;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class AutoStartupManager {
    
    private static AutoStartupManager instance;
    private final String APP_NAME = "PCMDesktop";
    private final String EXECUTABLE_NAME = "PCM Desktop";
    
    public static AutoStartupManager getInstance() {
        if (instance == null) {
            synchronized (AutoStartupManager.class) {
                if (instance == null) {
                    instance = new AutoStartupManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Check if auto-startup is currently enabled
     */
    public boolean isAutoStartupEnabled() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                return isWindowsAutoStartupEnabled();
            } else if (os.contains("mac")) {
                return isMacOSAutoStartupEnabled();
            } else if (os.contains("linux") || os.contains("unix")) {
                return isLinuxAutoStartupEnabled();
            }
        } catch (Exception e) {
            log.error("Failed to check auto-startup status", e);
        }
        
        return false;
    }
    
    /**
     * Enable auto-startup for the application
     */
    public boolean enableAutoStartup() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                return enableWindowsAutoStartup();
            } else if (os.contains("mac")) {
                return enableMacOSAutoStartup();
            } else if (os.contains("linux") || os.contains("unix")) {
                return enableLinuxAutoStartup();
            }
        } catch (Exception e) {
            log.error("Failed to enable auto-startup", e);
        }
        
        return false;
    }
    
    /**
     * Disable auto-startup for the application
     */
    public boolean disableAutoStartup() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                return disableWindowsAutoStartup();
            } else if (os.contains("mac")) {
                return disableMacOSAutoStartup();
            } else if (os.contains("linux") || os.contains("unix")) {
                return disableLinuxAutoStartup();
            }
        } catch (Exception e) {
            log.error("Failed to disable auto-startup", e);
        }
        
        return false;
    }
    
    // Windows Implementation
    private boolean isWindowsAutoStartupEnabled() {
        try {
            String keyPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
            return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, keyPath, APP_NAME);
        } catch (Exception e) {
            log.debug("Windows auto-startup check failed", e);
            return false;
        }
    }
    
    private boolean enableWindowsAutoStartup() {
        try {
            String keyPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
            String executablePath = getExecutablePath();
            
            // Add to Windows registry
            Advapi32Util.registrySetStringValue(
                WinReg.HKEY_CURRENT_USER, 
                keyPath, 
                APP_NAME, 
                "\"" + executablePath + "\" --startup"
            );
            
            log.info("Windows auto-startup enabled");
            return true;
        } catch (Exception e) {
            log.error("Failed to enable Windows auto-startup", e);
            return false;
        }
    }
    
    private boolean disableWindowsAutoStartup() {
        try {
            String keyPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
            
            if (isWindowsAutoStartupEnabled()) {
                Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, keyPath, APP_NAME);
                log.info("Windows auto-startup disabled");
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to disable Windows auto-startup", e);
            return false;
        }
    }
    
    // macOS Implementation
    private boolean isMacOSAutoStartupEnabled() {
        try {
            String homeDir = System.getProperty("user.home");
            Path plistPath = Paths.get(homeDir, "Library", "LaunchAgents", "com.noteflix.pcm.plist");
            return Files.exists(plistPath);
        } catch (Exception e) {
            log.debug("macOS auto-startup check failed", e);
            return false;
        }
    }
    
    private boolean enableMacOSAutoStartup() {
        try {
            String homeDir = System.getProperty("user.home");
            Path launchAgentsDir = Paths.get(homeDir, "Library", "LaunchAgents");
            Path plistPath = launchAgentsDir.resolve("com.noteflix.pcm.plist");
            
            // Create LaunchAgents directory if it doesn't exist
            Files.createDirectories(launchAgentsDir);
            
            // Create plist content
            String plistContent = createMacOSPlistContent();
            
            // Write plist file
            Files.write(plistPath, plistContent.getBytes());
            
            // Load the launch agent
            ProcessBuilder pb = new ProcessBuilder("launchctl", "load", plistPath.toString());
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("macOS auto-startup enabled");
                return true;
            } else {
                log.error("Failed to load macOS launch agent, exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to enable macOS auto-startup", e);
            return false;
        }
    }
    
    private boolean disableMacOSAutoStartup() {
        try {
            String homeDir = System.getProperty("user.home");
            Path plistPath = Paths.get(homeDir, "Library", "LaunchAgents", "com.noteflix.pcm.plist");
            
            if (Files.exists(plistPath)) {
                // Unload the launch agent
                ProcessBuilder pb = new ProcessBuilder("launchctl", "unload", plistPath.toString());
                Process process = pb.start();
                process.waitFor();
                
                // Remove plist file
                Files.delete(plistPath);
                
                log.info("macOS auto-startup disabled");
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to disable macOS auto-startup", e);
            return false;
        }
    }
    
    private String createMacOSPlistContent() {
        String executablePath = getExecutablePath();
        
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" 
                "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>Label</key>
                <string>com.noteflix.pcm</string>
                <key>ProgramArguments</key>
                <array>
                    <string>%s</string>
                    <string>--startup</string>
                </array>
                <key>RunAtLoad</key>
                <true/>
                <key>KeepAlive</key>
                <false/>
                <key>LaunchOnlyOnce</key>
                <true/>
            </dict>
            </plist>
            """, executablePath);
    }
    
    // Linux Implementation
    private boolean isLinuxAutoStartupEnabled() {
        try {
            String homeDir = System.getProperty("user.home");
            Path desktopFilePath = Paths.get(homeDir, ".config", "autostart", "pcm-desktop.desktop");
            return Files.exists(desktopFilePath);
        } catch (Exception e) {
            log.debug("Linux auto-startup check failed", e);
            return false;
        }
    }
    
    private boolean enableLinuxAutoStartup() {
        try {
            String homeDir = System.getProperty("user.home");
            Path autostartDir = Paths.get(homeDir, ".config", "autostart");
            Path desktopFilePath = autostartDir.resolve("pcm-desktop.desktop");
            
            // Create autostart directory if it doesn't exist
            Files.createDirectories(autostartDir);
            
            // Create desktop file content
            String desktopFileContent = createLinuxDesktopFile();
            
            // Write desktop file
            Files.write(desktopFilePath, desktopFileContent.getBytes());
            
            // Make executable
            desktopFilePath.toFile().setExecutable(true);
            
            log.info("Linux auto-startup enabled");
            return true;
        } catch (Exception e) {
            log.error("Failed to enable Linux auto-startup", e);
            return false;
        }
    }
    
    private boolean disableLinuxAutoStartup() {
        try {
            String homeDir = System.getProperty("user.home");
            Path desktopFilePath = Paths.get(homeDir, ".config", "autostart", "pcm-desktop.desktop");
            
            if (Files.exists(desktopFilePath)) {
                Files.delete(desktopFilePath);
                log.info("Linux auto-startup disabled");
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to disable Linux auto-startup", e);
            return false;
        }
    }
    
    private String createLinuxDesktopFile() {
        String executablePath = getExecutablePath();
        String iconPath = getIconPath();
        
        return String.format("""
            [Desktop Entry]
            Type=Application
            Name=PCM Desktop
            Comment=Personal Content Manager
            Exec=%s --startup
            Icon=%s
            Hidden=false
            NoDisplay=false
            X-GNOME-Autostart-enabled=true
            StartupNotify=true
            Categories=Office;Productivity;
            """, executablePath, iconPath);
    }
    
    /**
     * Get the executable path for the current application
     */
    private String getExecutablePath() {
        try {
            // Try to get the actual executable path
            String javaHome = System.getProperty("java.home");
            String jarPath = getJarPath();
            
            if (jarPath != null && jarPath.endsWith(".jar")) {
                // Running from JAR
                String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    javaBin += ".exe";
                }
                return javaBin + " -jar \"" + jarPath + "\"";
            } else {
                // Running from IDE or different setup
                return System.getProperty("java.home") + "/bin/java -cp " + 
                       System.getProperty("java.class.path") + " " + 
                       PCMDesktopApplication.class.getName();
            }
        } catch (Exception e) {
            log.error("Failed to determine executable path", e);
            return "pcm-desktop"; // Fallback
        }
    }
    
    private String getJarPath() {
        try {
            return new File(AutoStartupManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getPath();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getIconPath() {
        // Return path to application icon for desktop entry
        try {
            String jarDir = new File(getJarPath()).getParent();
            return jarDir + File.separator + "icons" + File.separator + "app" + 
                   File.separator + "icon-48.png";
        } catch (Exception e) {
            return "application-x-executable"; // Fallback to generic icon
        }
    }
}
```

### 2. Settings Integration

#### Auto-startup Settings UI
```java
package com.noteflix.pcm.ui.settings;

import com.noteflix.pcm.startup.AutoStartupManager;
import com.noteflix.pcm.tray.SystemTrayManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemSettingsPane extends VBox {
    
    private final BooleanProperty autoStartupEnabled = new SimpleBooleanProperty();
    private final BooleanProperty minimizeToTrayEnabled = new SimpleBooleanProperty();
    private final BooleanProperty minimizeOnCloseEnabled = new SimpleBooleanProperty();
    
    private final AutoStartupManager startupManager;
    private final SystemTrayManager trayManager;
    
    public SystemSettingsPane() {
        this.startupManager = AutoStartupManager.getInstance();
        this.trayManager = SystemTrayManager.getInstance();
        
        initializeComponents();
        loadCurrentSettings();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setSpacing(15);
        setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("System Integration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Auto-startup section
        VBox startupSection = createAutoStartupSection();
        
        // System tray section
        VBox traySection = createSystemTraySection();
        
        getChildren().addAll(
            titleLabel,
            new Separator(),
            startupSection,
            new Separator(),
            traySection
        );
    }
    
    private VBox createAutoStartupSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("Startup Behavior");
        sectionTitle.setStyle("-fx-font-weight: bold;");
        
        CheckBox autoStartupCheckBox = new CheckBox("Start automatically when computer boots");
        autoStartupCheckBox.selectedProperty().bindBidirectional(autoStartupEnabled);
        
        Label autoStartupDescription = new Label(
            "When enabled, PCM Desktop will start automatically when you log in to your computer."
        );
        autoStartupDescription.setStyle("-fx-text-fill: -fx-text-fill-secondary;");
        autoStartupDescription.setWrapText(true);
        
        section.getChildren().addAll(
            sectionTitle,
            autoStartupCheckBox,
            autoStartupDescription
        );
        
        return section;
    }
    
    private VBox createSystemTraySection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("System Tray");
        sectionTitle.setStyle("-fx-font-weight: bold;");
        
        // Check if system tray is supported
        if (!SystemTray.isSupported()) {
            Label notSupportedLabel = new Label("System tray is not supported on this platform.");
            notSupportedLabel.setStyle("-fx-text-fill: -fx-text-fill-secondary;");
            section.getChildren().addAll(sectionTitle, notSupportedLabel);
            return section;
        }
        
        CheckBox minimizeToTrayCheckBox = new CheckBox("Minimize to system tray instead of taskbar");
        minimizeToTrayCheckBox.selectedProperty().bindBidirectional(minimizeToTrayEnabled);
        
        CheckBox minimizeOnCloseCheckBox = new CheckBox("Minimize to tray when closing window");
        minimizeOnCloseCheckBox.selectedProperty().bindBidirectional(minimizeOnCloseEnabled);
        
        Label trayDescription = new Label(
            "When minimized to tray, the application continues running in the background. " +
            "Double-click the tray icon or use the context menu to restore the window."
        );
        trayDescription.setStyle("-fx-text-fill: -fx-text-fill-secondary;");
        trayDescription.setWrapText(true);
        
        section.getChildren().addAll(
            sectionTitle,
            minimizeToTrayCheckBox,
            minimizeOnCloseCheckBox,
            trayDescription
        );
        
        return section;
    }
    
    private void loadCurrentSettings() {
        // Load auto-startup setting
        autoStartupEnabled.set(startupManager.isAutoStartupEnabled());
        
        // Load tray settings
        minimizeToTrayEnabled.set(
            ConfigurationManager.getInstance().getBoolean("ui.minimizeToTray", true)
        );
        minimizeOnCloseEnabled.set(
            ConfigurationManager.getInstance().getBoolean("ui.minimizeOnClose", true)
        );
    }
    
    private void setupEventHandlers() {
        // Auto-startup change handler
        autoStartupEnabled.addListener((obs, oldValue, newValue) -> {
            try {
                if (newValue) {
                    boolean success = startupManager.enableAutoStartup();
                    if (!success) {
                        // Revert checkbox state
                        autoStartupEnabled.set(false);
                        showErrorAlert("Failed to enable auto-startup", 
                            "Unable to configure automatic startup. Please check your system permissions.");
                    } else {
                        ConfigurationManager.getInstance().setBoolean("system.autoStartup", true);
                    }
                } else {
                    boolean success = startupManager.disableAutoStartup();
                    if (!success) {
                        // Revert checkbox state
                        autoStartupEnabled.set(true);
                        showErrorAlert("Failed to disable auto-startup", 
                            "Unable to remove automatic startup configuration.");
                    } else {
                        ConfigurationManager.getInstance().setBoolean("system.autoStartup", false);
                    }
                }
            } catch (Exception e) {
                log.error("Error changing auto-startup setting", e);
                autoStartupEnabled.set(oldValue); // Revert
                showErrorAlert("Error", "An error occurred while changing the auto-startup setting.");
            }
        });
        
        // Tray settings change handlers
        minimizeToTrayEnabled.addListener((obs, oldValue, newValue) -> {
            ConfigurationManager.getInstance().setBoolean("ui.minimizeToTray", newValue);
        });
        
        minimizeOnCloseEnabled.addListener((obs, oldValue, newValue) -> {
            ConfigurationManager.getInstance().setBoolean("ui.minimizeOnClose", newValue);
        });
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
```

## Cross-Platform Considerations

### 1. Platform-Specific Behavior

#### Windows Specific
```java
// Windows registry management for auto-startup
public class WindowsIntegration {
    
    /**
     * Windows-specific tray icon considerations
     */
    public static void configureWindowsTray(TrayIcon trayIcon) {
        // Windows supports colored tray icons
        // Use high-contrast icons for better visibility
        
        // Windows 10/11 specific: Consider dark/light theme
        if (isWindowsDarkTheme()) {
            // Use light icon for dark theme
            updateTrayIcon(trayIcon, "/icons/tray/windows/icon-light.png");
        } else {
            // Use dark icon for light theme
            updateTrayIcon(trayIcon, "/icons/tray/windows/icon-dark.png");
        }
    }
    
    private static boolean isWindowsDarkTheme() {
        try {
            String keyPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
            int value = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER, keyPath, "AppsUseLightTheme"
            );
            return value == 0; // 0 = dark theme, 1 = light theme
        } catch (Exception e) {
            return false; // Default to light theme
        }
    }
}
```

#### macOS Specific
```java
// macOS-specific integration
public class MacOSIntegration {
    
    /**
     * macOS-specific tray icon configuration
     */
    public static void configureMacOSTray(TrayIcon trayIcon) {
        // macOS prefers template images (black with transparency)
        // The system automatically adjusts for dark/light mode
        
        // Set template image
        BufferedImage templateImage = loadTemplateIcon();
        trayIcon.setImage(templateImage);
        
        // Configure for Retina displays
        if (isRetinaDisplay()) {
            trayIcon.setImageAutoSize(true);
        }
    }
    
    /**
     * Configure macOS-specific app behavior
     */
    public static void configureMacOSApp() {
        // Hide dock icon when minimized to tray (optional)
        System.setProperty("apple.awt.UIElement", "false");
        
        // Configure menu bar appearance
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "PCM Desktop");
        
        // Handle macOS quit behavior
        Desktop desktop = Desktop.getDesktop();
        desktop.setQuitHandler((e, response) -> {
            // Custom quit logic here
            response.performQuit();
        });
    }
    
    private static boolean isRetinaDisplay() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        AffineTransform transform = config.getDefaultTransform();
        return transform.getScaleX() > 1.0;
    }
}
```

#### Linux Specific
```java
// Linux-specific integration
public class LinuxIntegration {
    
    /**
     * Linux desktop environment detection and configuration
     */
    public static void configureLinuxIntegration() {
        String desktopSession = System.getenv("XDG_CURRENT_DESKTOP");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        
        if (desktopSession != null) {
            switch (desktopSession.toLowerCase()) {
                case "gnome":
                    configureGnomeIntegration();
                    break;
                case "kde":
                    configureKDEIntegration();
                    break;
                case "xfce":
                    configureXfceIntegration();
                    break;
                default:
                    configureGenericLinuxIntegration();
            }
        }
        
        // Configure for Wayland vs X11
        if ("wayland".equals(sessionType)) {
            configureWaylandSpecific();
        } else {
            configureX11Specific();
        }
    }
    
    private static void configureGnomeIntegration() {
        // GNOME-specific settings
        // Check if GNOME Shell supports tray icons
        if (!isGnomeShellTraySupported()) {
            log.warn("GNOME Shell may not display tray icons. Consider using TopIcons extension.");
        }
    }
    
    private static void configureKDEIntegration() {
        // KDE-specific settings
        // KDE has good native tray support
    }
    
    private static boolean isGnomeShellTraySupported() {
        try {
            Process process = Runtime.getRuntime().exec(
                "dbus-send --session --dest=org.gnome.Shell " +
                "--type=method_call /org/gnome/Shell " +
                "org.freedesktop.DBus.Properties.Get " +
                "string:'org.gnome.Shell' string:'OverviewVisible'"
            );
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2. Icon Resources Organization

#### Directory Structure
```
src/main/resources/icons/
├── app/                    # Application icons
│   ├── icon-16.png
│   ├── icon-24.png
│   ├── icon-32.png
│   ├── icon-48.png
│   ├── icon-64.png
│   ├── icon-128.png
│   └── icon-256.png
├── tray/                   # System tray icons
│   ├── windows/
│   │   ├── icon-16.png     # Standard size
│   │   ├── icon-32.png     # Large size
│   │   ├── icon-light.png  # For dark theme
│   │   └── icon-dark.png   # For light theme
│   ├── macos/
│   │   ├── icon-template.png        # Black with transparency
│   │   └── icon-template@2x.png     # Retina version
│   └── linux/
│       ├── icon-22.png     # Standard GNOME size
│       ├── icon-24.png     # Alternative size
│       └── icon-32.png     # Larger size
└── notifications/          # Notification icons
    ├── info.png
    ├── warning.png
    └── error.png
```

#### Icon Design Guidelines
```yaml
Application Icons:
  Formats: PNG with transparency
  Sizes: 16, 24, 32, 48, 64, 128, 256px
  Design: Full color, detailed at larger sizes
  
Tray Icons:
  Windows:
    - Format: PNG or ICO
    - Sizes: 16px (standard), 32px (large)
    - Colors: Full color supported
    - Design: Clear at small sizes
    
  macOS:
    - Format: PNG with transparency
    - Style: Template image (black with alpha)
    - Sizes: 16px base, 32px retina (@2x)
    - Design: Simple, monochrome
    
  Linux:
    - Format: PNG with transparency
    - Sizes: 22px (GNOME), 24px, 32px
    - Colors: Full color supported
    - Design: Follow freedesktop.org guidelines
```

## Configuration Management

### 1. Configuration Schema

#### System Integration Settings
```json
{
  "system": {
    "autoStartup": {
      "enabled": false,
      "minimizedStart": true,
      "startupDelay": 5
    }
  },
  "ui": {
    "systemTray": {
      "minimizeToTray": true,
      "minimizeOnClose": true,
      "showNotifications": true,
      "hasShownTrayNotification": false
    },
    "window": {
      "rememberPosition": true,
      "rememberSize": true,
      "startMaximized": false
    }
  },
  "notifications": {
    "enabled": true,
    "types": {
      "system": true,
      "updates": true,
      "errors": true
    }
  }
}
```

### 2. Settings Persistence

#### Configuration Manager Extension
```java
public class SystemIntegrationConfig {
    
    private final ConfigurationManager config;
    
    public SystemIntegrationConfig(ConfigurationManager config) {
        this.config = config;
    }
    
    // Auto-startup settings
    public boolean isAutoStartupEnabled() {
        return config.getBoolean("system.autoStartup.enabled", false);
    }
    
    public void setAutoStartupEnabled(boolean enabled) {
        config.setBoolean("system.autoStartup.enabled", enabled);
    }
    
    public boolean shouldStartMinimized() {
        return config.getBoolean("system.autoStartup.minimizedStart", true);
    }
    
    public int getStartupDelay() {
        return config.getInt("system.autoStartup.startupDelay", 5);
    }
    
    // Tray settings
    public boolean shouldMinimizeToTray() {
        return config.getBoolean("ui.systemTray.minimizeToTray", true);
    }
    
    public void setMinimizeToTray(boolean minimize) {
        config.setBoolean("ui.systemTray.minimizeToTray", minimize);
    }
    
    public boolean shouldMinimizeOnClose() {
        return config.getBoolean("ui.systemTray.minimizeOnClose", true);
    }
    
    public void setMinimizeOnClose(boolean minimize) {
        config.setBoolean("ui.systemTray.minimizeOnClose", minimize);
    }
    
    // Notification settings
    public boolean areNotificationsEnabled() {
        return config.getBoolean("notifications.enabled", true);
    }
    
    public boolean hasShownTrayNotification() {
        return config.getBoolean("ui.systemTray.hasShownTrayNotification", false);
    }
    
    public void setTrayNotificationShown(boolean shown) {
        config.setBoolean("ui.systemTray.hasShownTrayNotification", shown);
    }
}
```

## User Experience Guidelines

### 1. First-time Setup

#### Welcome Configuration Flow
```java
public class FirstTimeSetupWizard {
    
    public void showSystemIntegrationSetup() {
        Dialog<SystemIntegrationPreferences> dialog = new Dialog<>();
        dialog.setTitle("System Integration Setup");
        dialog.setHeaderText("Configure how PCM Desktop integrates with your system");
        
        // Create custom dialog pane
        VBox content = createSetupContent();
        dialog.getDialogPane().setContent(content);
        
        // Add buttons
        ButtonType finishButtonType = new ButtonType("Finish", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, finishButtonType);
        
        // Show and process result
        Optional<SystemIntegrationPreferences> result = dialog.showAndWait();
        result.ifPresent(this::applyPreferences);
    }
    
    private VBox createSetupContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Auto-startup option
        CheckBox autoStartupBox = new CheckBox("Start PCM Desktop when I log in");
        autoStartupBox.setSelected(true); // Default enabled
        
        Label autoStartupDesc = new Label(
            "PCM Desktop will start automatically when you log in to your computer."
        );
        autoStartupDesc.setStyle("-fx-text-fill: gray;");
        
        // System tray option (if supported)
        VBox traySection = new VBox(5);
        if (SystemTray.isSupported()) {
            CheckBox trayBox = new CheckBox("Minimize to system tray");
            trayBox.setSelected(true);
            
            Label trayDesc = new Label(
                "When minimized, PCM Desktop will continue running in the system tray."
            );
            trayDesc.setStyle("-fx-text-fill: gray;");
            
            traySection.getChildren().addAll(trayBox, trayDesc);
        }
        
        // Privacy notice
        Label privacyLabel = new Label(
            "These settings can be changed later in Preferences."
        );
        privacyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        
        content.getChildren().addAll(
            autoStartupBox, autoStartupDesc,
            new Separator(),
            traySection,
            new Separator(),
            privacyLabel
        );
        
        return content;
    }
}
```

### 2. User Notifications

#### Tray Notification System
```java
public class TrayNotificationManager {
    
    private final SystemTrayManager trayManager;
    private final SystemIntegrationConfig config;
    
    public void showWelcomeNotification() {
        if (config.areNotificationsEnabled()) {
            trayManager.showTrayNotification(
                "PCM Desktop Started",
                "Application is running in the system tray. Double-click to open.",
                TrayIcon.MessageType.INFO
            );
        }
    }
    
    public void showMinimizedNotification() {
        if (!config.hasShownTrayNotification()) {
            trayManager.showTrayNotification(
                "Minimized to Tray",
                "PCM Desktop is still running. Right-click the tray icon for options.",
                TrayIcon.MessageType.INFO
            );
            config.setTrayNotificationShown(true);
        }
    }
    
    public void showAutoStartupConfiguredNotification() {
        if (config.areNotificationsEnabled()) {
            trayManager.showTrayNotification(
                "Auto-startup Configured",
                "PCM Desktop will now start automatically when you log in.",
                TrayIcon.MessageType.INFO
            );
        }
    }
}
```

### 3. Accessibility Considerations

#### Keyboard Navigation
```java
public class AccessibilitySupport {
    
    /**
     * Configure keyboard shortcuts for tray operations
     */
    public static void setupGlobalKeyboardShortcuts(Stage primaryStage) {
        // Register global hotkey for show/hide (Ctrl+Alt+P)
        GlobalKeyboardHook.registerHotkey(
            KeyEvent.VK_P, 
            InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK,
            () -> {
                Platform.runLater(() -> {
                    SystemTrayManager tray = SystemTrayManager.getInstance();
                    if (primaryStage.isShowing()) {
                        tray.minimizeToTray();
                    } else {
                        tray.restoreWindow();
                    }
                });
            }
        );
    }
    
    /**
     * Configure screen reader support
     */
    public static void configureScreenReaderSupport(TrayIcon trayIcon) {
        // Set accessible name and description
        trayIcon.setToolTip("PCM Desktop - Personal Content Manager");
        
        // Ensure popup menu is accessible
        PopupMenu popup = trayIcon.getPopupMenu();
        for (int i = 0; i < popup.getItemCount(); i++) {
            MenuItem item = popup.getItem(i);
            if (item != null) {
                // Add mnemonics for keyboard navigation
                addMnemonicToMenuItem(item, i);
            }
        }
    }
    
    private static void addMnemonicToMenuItem(MenuItem item, int index) {
        String label = item.getLabel();
        if (label != null && !label.contains("&")) {
            // Add mnemonic based on first letter or index
            char mnemonic = label.charAt(0);
            item.setLabel("&" + label);
        }
    }
}
```

## Testing & Validation

### 1. Unit Tests

#### System Tray Manager Tests
```java
@ExtendWith(MockitoExtension.class)
class SystemTrayManagerTest {
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private SystemTray mockSystemTray;
    
    private SystemTrayManager trayManager;
    
    @BeforeEach
    void setUp() {
        trayManager = SystemTrayManager.getInstance();
    }
    
    @Test
    void testInitialize_SystemTraySupported_ReturnsTrue() {
        // Given
        try (MockedStatic<SystemTray> systemTrayMock = mockStatic(SystemTray.class)) {
            systemTrayMock.when(SystemTray::isSupported).thenReturn(true);
            systemTrayMock.when(SystemTray::getSystemTray).thenReturn(mockSystemTray);
            
            // When
            boolean result = trayManager.initialize(mockStage);
            
            // Then
            assertTrue(result);
        }
    }
    
    @Test
    void testInitialize_SystemTrayNotSupported_ReturnsFalse() {
        // Given
        try (MockedStatic<SystemTray> systemTrayMock = mockStatic(SystemTray.class)) {
            systemTrayMock.when(SystemTray::isSupported).thenReturn(false);
            
            // When
            boolean result = trayManager.initialize(mockStage);
            
            // Then
            assertFalse(result);
        }
    }
    
    @Test
    void testMinimizeToTray_HidesStageAndSetsFlag() {
        // Given
        trayManager.initialize(mockStage);
        
        // When
        trayManager.minimizeToTray();
        
        // Then
        verify(mockStage).hide();
        assertTrue(trayManager.isMinimizedToTray());
    }
}
```

#### Auto-startup Manager Tests
```java
@ExtendWith(MockitoExtension.class)
class AutoStartupManagerTest {
    
    private AutoStartupManager startupManager;
    
    @BeforeEach
    void setUp() {
        startupManager = AutoStartupManager.getInstance();
    }
    
    @Test
    void testEnableAutoStartup_Windows_CreatesRegistryEntry() {
        // This test would be platform-specific
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("win"));
        
        // Test Windows-specific implementation
        boolean result = startupManager.enableAutoStartup();
        
        assertTrue(result);
        assertTrue(startupManager.isAutoStartupEnabled());
    }
    
    @Test
    void testDisableAutoStartup_RemovesConfiguration() {
        // Given
        startupManager.enableAutoStartup();
        assumeTrue(startupManager.isAutoStartupEnabled());
        
        // When
        boolean result = startupManager.disableAutoStartup();
        
        // Then
        assertTrue(result);
        assertFalse(startupManager.isAutoStartupEnabled());
    }
}
```

### 2. Integration Tests

#### End-to-End Workflow Tests
```java
@TestMethodOrder(OrderAnnotation.class)
class SystemIntegrationE2ETest {
    
    private static TestFX testFX;
    private static SystemTrayManager trayManager;
    private static AutoStartupManager startupManager;
    
    @BeforeAll
    static void setUpClass() {
        testFX = new TestFX();
        trayManager = SystemTrayManager.getInstance();
        startupManager = AutoStartupManager.getInstance();
    }
    
    @Test
    @Order(1)
    void testApplicationStartup_InitializesTrayCorrectly() {
        // Test that application starts and initializes tray
        testFX.launch(PCMDesktopApplication.class);
        
        // Verify tray is initialized if supported
        if (SystemTray.isSupported()) {
            assertNotNull(trayManager);
            // Additional assertions for tray initialization
        }
    }
    
    @Test
    @Order(2)
    void testMinimizeToTray_WorksCorrectly() {
        assumeTrue(SystemTray.isSupported());
        
        // Minimize window
        testFX.clickOn("#minimizeButton");
        
        // Verify window is hidden
        assertFalse(testFX.getStage().isShowing());
        assertTrue(trayManager.isMinimizedToTray());
    }
    
    @Test
    @Order(3)
    void testRestoreFromTray_WorksCorrectly() {
        assumeTrue(SystemTray.isSupported());
        assumeTrue(trayManager.isMinimizedToTray());
        
        // Restore from tray
        trayManager.restoreWindow();
        
        // Verify window is visible
        assertTrue(testFX.getStage().isShowing());
        assertFalse(trayManager.isMinimizedToTray());
    }
    
    @Test
    @Order(4)
    void testAutoStartupConfiguration_WorksCorrectly() {
        // Enable auto-startup
        boolean enableResult = startupManager.enableAutoStartup();
        assertTrue(enableResult);
        assertTrue(startupManager.isAutoStartupEnabled());
        
        // Disable auto-startup
        boolean disableResult = startupManager.disableAutoStartup();
        assertTrue(disableResult);
        assertFalse(startupManager.isAutoStartupEnabled());
    }
}
```

### 3. Manual Testing Checklist

#### System Tray Testing
```yaml
Basic Functionality:
  □ Application minimizes to tray when requested
  □ Tray icon appears in correct location
  □ Double-click on tray icon restores window
  □ Right-click shows context menu
  □ Context menu items work correctly
  □ Application exits properly from tray menu

Cross-Platform Testing:
  Windows:
    □ Icon displays correctly in notification area
    □ Icon adapts to light/dark theme
    □ Balloon notifications work
    □ Icon tooltip displays correctly
    
  macOS:
    □ Template icon displays in menu bar
    □ Icon adapts to dark/light mode automatically
    □ Menu bar menu works correctly
    □ Notifications show in Notification Center
    
  Linux:
    □ Icon displays in system tray (GNOME/KDE/XFCE)
    □ Icon size appropriate for desktop environment
    □ Context menu accessible
    □ Tray area detection works

User Experience:
  □ First-time tray notification shows
  □ Minimize notification only shows once
  □ Notification messages are clear
  □ Keyboard shortcuts work (if implemented)
  □ Accessibility features functional
```

#### Auto-startup Testing
```yaml
Configuration:
  □ Auto-startup can be enabled from settings
  □ Auto-startup can be disabled from settings
  □ Setting persists after application restart
  □ Error handling for permission issues

Platform-Specific:
  Windows:
    □ Registry entry created/removed correctly
    □ Application starts after reboot
    □ Command line arguments preserved
    
  macOS:
    □ Launch Agent plist created correctly
    □ Application loads at login
    □ Plist file removed when disabled
    
  Linux:
    □ Desktop file created in autostart directory
    □ Application starts with session
    □ Desktop file removed when disabled

Behavior:
  □ Application starts minimized (if configured)
  □ Startup delay works correctly
  □ Multiple instances prevented
  □ Error logging for startup failures
```

---

**Implementation Priority:**
1. **High**: System Tray basic functionality
2. **High**: Auto-startup configuration  
3. **Medium**: Cross-platform optimizations
4. **Medium**: Advanced notifications
5. **Low**: Accessibility enhancements

**Dependencies:**
- Java AWT System Tray support
- Platform-specific APIs (JNA)
- Icon assets in multiple formats
- Configuration management system

**Security Considerations:**
- Registry access permissions (Windows)
- File system permissions for autostart directories
- Code signing for auto-startup trust
- User consent for system integration features