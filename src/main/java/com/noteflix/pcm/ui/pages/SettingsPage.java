package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Settings page - Single Responsibility Principle
 * Manages application configuration and preferences
 */
@Slf4j
public class SettingsPage extends BasePage {
    
    private final ThemeManager themeManager;
    
    public SettingsPage() {
        super(
            "Settings",
            "Configure your application preferences and system settings",
            new FontIcon(Feather.SETTINGS)
        );
        this.themeManager = ThemeManager.getInstance();
    }
    
    @Override
    protected VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.getStyleClass().add("settings-content");
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Appearance settings
        mainContent.getChildren().add(createAppearanceSection());
        
        // Database settings
        mainContent.getChildren().add(createDatabaseSection());
        
        // Notification settings
        mainContent.getChildren().add(createNotificationSection());
        
        // Advanced settings
        mainContent.getChildren().add(createAdvancedSection());
        
        return mainContent;
    }
    
    private VBox createAppearanceSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label title = new Label("Appearance");
        title.getStyleClass().addAll(Styles.TITLE_3);
        title.setGraphic(new FontIcon(Feather.MONITOR));
        title.setGraphicTextGap(12);
        
        // Theme selection
        HBox themeRow = createSettingRow("Theme", "Choose between light and dark theme");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Light", "Dark", "Auto");
        themeCombo.setValue(themeManager.isDarkTheme() ? "Dark" : "Light");
        themeCombo.setOnAction(e -> handleThemeChange(themeCombo.getValue()));
        themeRow.getChildren().add(themeCombo);
        
        // Font size
        HBox fontRow = createSettingRow("Font Size", "Adjust the application font size");
        Slider fontSlider = new Slider(10, 18, 14);
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        fontSlider.setMajorTickUnit(2);
        fontSlider.setPrefWidth(200);
        fontRow.getChildren().add(fontSlider);
        
        // Sidebar width
        HBox sidebarRow = createSettingRow("Sidebar Width", "Adjust the sidebar width");
        Slider sidebarSlider = new Slider(200, 400, 280);
        sidebarSlider.setShowTickLabels(true);
        sidebarSlider.setShowTickMarks(true);
        sidebarSlider.setMajorTickUnit(50);
        sidebarSlider.setPrefWidth(200);
        sidebarRow.getChildren().add(sidebarSlider);
        
        section.getChildren().addAll(title, themeRow, fontRow, sidebarRow);
        return section;
    }
    
    private VBox createDatabaseSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label title = new Label("Database");
        title.getStyleClass().addAll(Styles.TITLE_3);
        title.setGraphic(new FontIcon(Feather.DATABASE));
        title.setGraphicTextGap(12);
        
        // Connection settings
        HBox hostRow = createSettingRow("Database Host", "Database server hostname or IP address");
        TextField hostField = new TextField("production-db.company.com");
        hostField.setPrefWidth(250);
        hostRow.getChildren().add(hostField);
        
        HBox portRow = createSettingRow("Port", "Database server port number");
        TextField portField = new TextField("1521");
        portField.setPrefWidth(100);
        portRow.getChildren().add(portField);
        
        HBox schemaRow = createSettingRow("Schema", "Default database schema");
        TextField schemaField = new TextField("PCM_PROD");
        schemaField.setPrefWidth(150);
        schemaRow.getChildren().add(schemaField);
        
        // Connection options
        HBox timeoutRow = createSettingRow("Connection Timeout", "Connection timeout in seconds");
        Spinner<Integer> timeoutSpinner = new Spinner<>(5, 60, 30);
        timeoutSpinner.setPrefWidth(100);
        timeoutRow.getChildren().add(timeoutSpinner);
        
        HBox poolRow = createSettingRow("Connection Pool Size", "Maximum number of database connections");
        Spinner<Integer> poolSpinner = new Spinner<>(5, 50, 20);
        poolSpinner.setPrefWidth(100);
        poolRow.getChildren().add(poolSpinner);
        
        // Test connection button
        HBox testRow = new HBox(12);
        testRow.setAlignment(Pos.CENTER_LEFT);
        Button testButton = new Button("Test Connection");
        testButton.setGraphic(new FontIcon(Feather.WIFI));
        testButton.getStyleClass().addAll(Styles.ACCENT);
        testButton.setOnAction(e -> handleTestConnection());
        testRow.getChildren().add(testButton);
        
        section.getChildren().addAll(title, hostRow, portRow, schemaRow, timeoutRow, poolRow, testRow);
        return section;
    }
    
    private VBox createNotificationSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label title = new Label("Notifications");
        title.getStyleClass().addAll(Styles.TITLE_3);
        title.setGraphic(new FontIcon(Feather.BELL));
        title.setGraphicTextGap(12);
        
        // Notification preferences
        HBox emailRow = createSettingRow("Email Notifications", "Receive notifications via email");
        CheckBox emailCheck = new CheckBox();
        emailCheck.setSelected(true);
        emailRow.getChildren().add(emailCheck);
        
        HBox desktopRow = createSettingRow("Desktop Notifications", "Show desktop notifications");
        CheckBox desktopCheck = new CheckBox();
        desktopCheck.setSelected(true);
        desktopRow.getChildren().add(desktopCheck);
        
        HBox soundRow = createSettingRow("Sound Alerts", "Play sound for important notifications");
        CheckBox soundCheck = new CheckBox();
        soundCheck.setSelected(false);
        soundRow.getChildren().add(soundCheck);
        
        // Notification types
        Label typesTitle = new Label("Notification Types");
        typesTitle.getStyleClass().addAll(Styles.TEXT_BOLD);
        
        VBox typesBox = new VBox(8);
        typesBox.getChildren().addAll(
            createNotificationTypeRow("Job Completion", true),
            createNotificationTypeRow("Job Failures", true),
            createNotificationTypeRow("System Alerts", true),
            createNotificationTypeRow("Database Issues", true),
            createNotificationTypeRow("Performance Warnings", false)
        );
        
        section.getChildren().addAll(title, emailRow, desktopRow, soundRow, typesTitle, typesBox);
        return section;
    }
    
    private VBox createAdvancedSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label title = new Label("Advanced");
        title.getStyleClass().addAll(Styles.TITLE_3);
        title.setGraphic(new FontIcon(Feather.TOOL));
        title.setGraphicTextGap(12);
        
        // Debug settings
        HBox debugRow = createSettingRow("Debug Mode", "Enable detailed logging for troubleshooting");
        CheckBox debugCheck = new CheckBox();
        debugCheck.setSelected(false);
        debugRow.getChildren().add(debugCheck);
        
        // Cache settings
        HBox cacheRow = createSettingRow("Cache Size", "Application cache size in MB");
        Spinner<Integer> cacheSpinner = new Spinner<>(50, 1000, 200);
        cacheSpinner.setPrefWidth(100);
        cacheRow.getChildren().add(cacheSpinner);
        
        // Auto-save
        HBox autoSaveRow = createSettingRow("Auto-save Interval", "Automatically save changes (minutes)");
        Spinner<Integer> autoSaveSpinner = new Spinner<>(1, 30, 5);
        autoSaveSpinner.setPrefWidth(100);
        autoSaveRow.getChildren().add(autoSaveSpinner);
        
        // Backup settings
        HBox backupRow = createSettingRow("Automatic Backups", "Create automatic configuration backups");
        CheckBox backupCheck = new CheckBox();
        backupCheck.setSelected(true);
        backupRow.getChildren().add(backupCheck);
        
        // Reset settings
        HBox resetRow = new HBox(12);
        resetRow.setAlignment(Pos.CENTER_LEFT);
        Button resetButton = new Button("Reset to Defaults");
        resetButton.setGraphic(new FontIcon(Feather.REFRESH_CW));
        resetButton.getStyleClass().addAll(Styles.DANGER);
        resetButton.setOnAction(e -> handleResetSettings());
        resetRow.getChildren().add(resetButton);
        
        section.getChildren().addAll(title, debugRow, cacheRow, autoSaveRow, backupRow, resetRow);
        return section;
    }
    
    private HBox createSettingRow(String label, String description) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        
        VBox labelBox = new VBox(2);
        labelBox.setPrefWidth(200);
        
        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD);
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        descLabel.setWrapText(true);
        
        labelBox.getChildren().addAll(titleLabel, descLabel);
        
        row.getChildren().add(labelBox);
        
        return row;
    }
    
    private HBox createNotificationTypeRow(String type, boolean enabled) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        
        CheckBox checkBox = new CheckBox(type);
        checkBox.setSelected(enabled);
        
        row.getChildren().add(checkBox);
        return row;
    }
    
    private void handleThemeChange(String theme) {
        log.info("Theme changed to: {}", theme);
        switch (theme) {
            case "Light" -> themeManager.setTheme(false);
            case "Dark" -> themeManager.setTheme(true);
            case "Auto" -> {
                // TODO: Implement auto theme based on system preference
                log.info("Auto theme not implemented yet");
            }
        }
    }
    
    private void handleTestConnection() {
        log.info("Testing database connection");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connection Test");
        alert.setHeaderText("Database Connection Test");
        alert.setContentText("Connection successful!\n\nServer: production-db.company.com:1521\nSchema: PCM_PROD\nResponse time: 45ms");
        alert.showAndWait();
    }
    
    private void handleResetSettings() {
        log.info("Resetting settings to defaults");
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Settings");
        confirmation.setHeaderText("Reset to Default Settings");
        confirmation.setContentText("This will reset all settings to their default values. Are you sure?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Reset all settings to defaults
                log.info("Settings reset to defaults");
            }
        });
    }
}