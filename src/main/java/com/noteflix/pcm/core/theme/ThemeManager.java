package com.noteflix.pcm.core.theme;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.noteflix.pcm.core.events.ThemeChangeListener;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;

/**
 * Central theme management for the application Handles theme switching and theme-aware resource
 * loading
 */
@Slf4j
public class ThemeManager {

  private static ThemeManager instance;
  private final List<ThemeChangeListener> listeners = new ArrayList<>();
  private boolean isDarkTheme = false;
  private Scene mainScene; // Reference to main scene for CSS management

  private ThemeManager() {}

  public static ThemeManager getInstance() {
    if (instance == null) {
      instance = new ThemeManager();
    }
    return instance;
  }

  /** Switches between light and dark themes */
  public void toggleTheme() {
    isDarkTheme = !isDarkTheme;
    applyTheme();
  }

  /** Sets a specific theme */
  public void setTheme(boolean darkTheme) {
    isDarkTheme = darkTheme;
    applyTheme();
  }

  /** Adds a theme change listener */
  public void addThemeChangeListener(ThemeChangeListener listener) {
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /** Removes a theme change listener */
  public void removeThemeChangeListener(ThemeChangeListener listener) {
    listeners.remove(listener);
  }

  /** Sets the main scene reference for CSS management */
  public void setMainScene(Scene scene) {
    this.mainScene = scene;
    // Apply theme to the scene immediately
    applyThemeToScene();
  }

  /** Applies the current theme to the application */
  private void applyTheme() {
    try {
      if (isDarkTheme) {
        log.info("Applying dark theme");
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
      } else {
        log.info("Applying light theme");
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
      }

      // Apply theme-specific CSS to scene
      applyThemeToScene();

      // Notify all listeners about theme change
      notifyListeners();
    } catch (Exception e) {
      log.error("Error applying theme", e);
      throw new RuntimeException("Failed to apply theme", e);
    }
  }

  /** Applies theme-specific CSS files to the main scene */
  private void applyThemeToScene() {
    if (mainScene == null) {
      log.warn("Main scene not set, cannot apply custom CSS");
      return;
    }

    try {
      // Clear existing stylesheets (except user agent stylesheet)
      mainScene.getStylesheets().clear();

      // Always add the main styles
      if (getClass().getResource("/css/styles.css") != null) {
        String mainCssUrl = getClass().getResource("/css/styles.css").toExternalForm();
        mainScene.getStylesheets().add(mainCssUrl);
        log.info("Applied main CSS: {}", mainCssUrl);
      } else {
        log.warn("Main CSS file not found: /css/styles.css");
      }

      // Add theme-specific CSS
      if (isDarkTheme) {
        if (getClass().getResource("/css/ai-assistant-dark.css") != null) {
          String darkCssUrl = getClass().getResource("/css/ai-assistant-dark.css").toExternalForm();
          mainScene.getStylesheets().add(darkCssUrl);
          log.info("Applied dark theme CSS: {}", darkCssUrl);
        } else {
          log.warn("Dark theme CSS file not found: /css/ai-assistant-dark.css");
        }
      }
      // Note: You can add light-specific CSS here if needed
      // else {
      //     if (getClass().getResource("/css/ai-assistant-light.css") != null) {
      //         String lightCssUrl =
      // getClass().getResource("/css/ai-assistant-light.css").toExternalForm();
      //         mainScene.getStylesheets().add(lightCssUrl);
      //         log.info("Applied light theme CSS: {}", lightCssUrl);
      //     }
      // }

    } catch (Exception e) {
      log.error("Failed to apply theme CSS to scene", e);
    }
  }

  /** Notifies all registered listeners about theme change */
  private void notifyListeners() {
    for (ThemeChangeListener listener : listeners) {
      try {
        listener.onThemeChanged(isDarkTheme);
      } catch (Exception e) {
        log.warn("Error notifying theme change listener", e);
      }
    }
  }

  /** Gets the appropriate icon path based on current theme */
  public String getThemedIcon(String lightIcon, String darkIcon) {
    return isDarkTheme ? darkIcon : lightIcon;
  }

  /** Gets the brain-circuit icon path for current theme */
  public String getBrainCircuitIcon() {
    return getThemedIcon("/images/icons/brain-circuit.png", "/images/icons/brain-circuit_dark.png");
  }

  /** Gets the bot icon path for current theme */
  public String getBotIcon() {
    return getThemedIcon("/images/icons/bot.png", "/images/icons/bot_dark.png");
  }

  public boolean isDarkTheme() {
    return isDarkTheme;
  }
}
