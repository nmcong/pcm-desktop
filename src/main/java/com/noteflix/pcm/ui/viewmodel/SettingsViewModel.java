package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.core.theme.ThemeManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingsViewModel extends BaseViewModel {

  private final ThemeManager themeManager;

  // Appearance properties
  private final StringProperty selectedTheme = new SimpleStringProperty("Light");
  private final StringProperty selectedLanguage = new SimpleStringProperty("English");
  private final DoubleProperty fontSize = new SimpleDoubleProperty(14.0);
  private final DoubleProperty sidebarWidth = new SimpleDoubleProperty(280.0);

  // Database properties
  private final StringProperty databasePath = new SimpleStringProperty("./data/pcm-desktop.db");

  // Notification properties
  private final BooleanProperty emailNotificationsEnabled = new SimpleBooleanProperty(false);

  // Available options
  private final ObservableList<String> availableThemes =
      FXCollections.observableArrayList("Light", "Dark");
  private final ObservableList<String> availableLanguages =
      FXCollections.observableArrayList("English", "Vietnamese");

  public SettingsViewModel(ThemeManager themeManager) {
    this.themeManager = themeManager;
    log.info("SettingsViewModel initialized");
  }

  public SettingsViewModel() {
    this(ThemeManager.getInstance());
  }

  public void loadSettings() {
    selectedTheme.set(themeManager.isDarkTheme() ? "Dark" : "Light");

    // Listen to theme changes and apply
    selectedTheme.addListener(
        (obs, oldVal, newVal) -> {
          if ("Dark".equals(newVal)) {
            themeManager.setTheme(true);
          } else {
            themeManager.setTheme(false);
          }
        });

    // Listen to language changes and apply
    selectedLanguage.addListener(
        (obs, oldVal, newVal) -> {
          if ("Vietnamese".equals(newVal)) {
            I18n.setLocale("vi");
          } else {
            I18n.setLocale("en");
          }
        });

    log.debug("Settings loaded");
  }

  public void changeDatabasePath() {
    log.info("Opening file chooser for database path...");
    // TODO: Show file chooser dialog
    // For now, just log
  }

  public void runMigrations() {
    setBusy(true);
    log.info("Running database migrations...");

    runAsync(
            () -> {
              Thread.sleep(2000); // Simulate migration
              return true;
            },
            success -> {
              log.info("Migrations completed successfully");
            },
            error -> {
              setError("Migration failed", error);
            })
        .whenComplete((r, ex) -> setBusy(false));
  }

  public void toggleEmailNotifications() {
    emailNotificationsEnabled.set(!emailNotificationsEnabled.get());
    log.info("Email notifications: {}", emailNotificationsEnabled.get());
  }

  public void resetSettings() {
    selectedTheme.set("Light");
    selectedLanguage.set("English");
    fontSize.set(14.0);
    sidebarWidth.set(280.0);
    emailNotificationsEnabled.set(false);
    log.info("Settings reset to defaults");
  }

  // Property accessors
  public StringProperty selectedThemeProperty() {
    return selectedTheme;
  }

  public String getSelectedTheme() {
    return selectedTheme.get();
  }

  public void setSelectedTheme(String theme) {
    selectedTheme.set(theme);
  }

  public StringProperty selectedLanguageProperty() {
    return selectedLanguage;
  }

  public String getSelectedLanguage() {
    return selectedLanguage.get();
  }

  public void setSelectedLanguage(String language) {
    selectedLanguage.set(language);
  }

  public DoubleProperty fontSizeProperty() {
    return fontSize;
  }

  public double getFontSize() {
    return fontSize.get();
  }

  public void setFontSize(double size) {
    fontSize.set(size);
  }

  public DoubleProperty sidebarWidthProperty() {
    return sidebarWidth;
  }

  public double getSidebarWidth() {
    return sidebarWidth.get();
  }

  public void setSidebarWidth(double width) {
    sidebarWidth.set(width);
  }

  public StringProperty databasePathProperty() {
    return databasePath;
  }

  public String getDatabasePath() {
    return databasePath.get();
  }

  public void setDatabasePath(String path) {
    databasePath.set(path);
  }

  public BooleanProperty emailNotificationsEnabledProperty() {
    return emailNotificationsEnabled;
  }

  public boolean isEmailNotificationsEnabled() {
    return emailNotificationsEnabled.get();
  }

  public void setEmailNotificationsEnabled(boolean enabled) {
    emailNotificationsEnabled.set(enabled);
  }

  public ObservableList<String> getAvailableThemes() {
    return availableThemes;
  }

  public ObservableList<String> getAvailableLanguages() {
    return availableLanguages;
  }
}
