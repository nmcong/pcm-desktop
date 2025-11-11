package com.noteflix.pcm.core.theme;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.noteflix.pcm.core.events.ThemeChangeListener;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Central theme management for the application
 * Handles theme switching and theme-aware resource loading
 */
@Slf4j
public class ThemeManager {
    
    private static ThemeManager instance;
    private boolean isDarkTheme = false;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    
    private ThemeManager() {}
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Switches between light and dark themes
     */
    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
    }
    
    /**
     * Sets a specific theme
     */
    public void setTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
        applyTheme();
    }
    
    /**
     * Adds a theme change listener
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a theme change listener
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Applies the current theme to the application
     */
    private void applyTheme() {
        try {
            if (isDarkTheme) {
                log.info("Applying dark theme");
                Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
            } else {
                log.info("Applying light theme");
                Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            }
            
            // Notify all listeners about theme change
            notifyListeners();
        } catch (Exception e) {
            log.error("Error applying theme", e);
            throw new RuntimeException("Failed to apply theme", e);
        }
    }
    
    /**
     * Notifies all registered listeners about theme change
     */
    private void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            try {
                listener.onThemeChanged(isDarkTheme);
            } catch (Exception e) {
                log.warn("Error notifying theme change listener", e);
            }
        }
    }
    
    /**
     * Gets the appropriate icon path based on current theme
     */
    public String getThemedIcon(String lightIcon, String darkIcon) {
        return isDarkTheme ? darkIcon : lightIcon;
    }
    
    /**
     * Gets the brain-circuit icon path for current theme
     */
    public String getBrainCircuitIcon() {
        return getThemedIcon("/images/icons/brain-circuit.png", "/images/icons/brain-circuit_dark.png");
    }
    
    /**
     * Gets the bot icon path for current theme
     */
    public String getBotIcon() {
        return getThemedIcon("/images/icons/bot.png", "/images/icons/bot_dark.png");
    }
    
    public boolean isDarkTheme() {
        return isDarkTheme;
    }
}