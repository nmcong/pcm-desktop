package com.noteflix.pcm.ui;

import atlantafx.base.theme.*;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

/**
 * Theme Manager for PCM Desktop
 * Manages AtlantaFX theme switching
 * 
 * Available themes:
 * - PrimerLight (GitHub-inspired light)
 * - PrimerDark (GitHub-inspired dark)
 * - NordLight (Nord palette light)
 * - NordDark (Nord palette dark)
 * - CupertinoLight (macOS-inspired light)
 * - CupertinoDark (macOS-inspired dark)
 * - Dracula (Dracula color scheme)
 */
@Slf4j
public class ThemeManager {
    
    public enum Theme {
        PRIMER_LIGHT("Primer Light", new PrimerLight()),
        PRIMER_DARK("Primer Dark", new PrimerDark()),
        NORD_LIGHT("Nord Light", new NordLight()),
        NORD_DARK("Nord Dark", new NordDark()),
        CUPERTINO_LIGHT("Cupertino Light", new CupertinoLight()),
        CUPERTINO_DARK("Cupertino Dark", new CupertinoDark()),
        DRACULA("Dracula", new Dracula());
        
        private final String displayName;
        private final atlantafx.base.theme.Theme theme;
        
        Theme(String displayName, atlantafx.base.theme.Theme theme) {
            this.displayName = displayName;
            this.theme = theme;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public atlantafx.base.theme.Theme getTheme() {
            return theme;
        }
    }
    
    private static Theme currentTheme = Theme.PRIMER_LIGHT;
    
    /**
     * Apply a theme to the application
     */
    public static void applyTheme(Theme theme) {
        try {
            Application.setUserAgentStylesheet(theme.getTheme().getUserAgentStylesheet());
            currentTheme = theme;
            log.info("✨ Theme applied: {}", theme.getDisplayName());
        } catch (Exception e) {
            log.error("❌ Failed to apply theme: {}", theme.getDisplayName(), e);
        }
    }
    
    /**
     * Get current theme
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Toggle between light and dark theme
     */
    public static void toggleDarkMode() {
        Theme newTheme = isDarkMode() ? Theme.PRIMER_LIGHT : Theme.PRIMER_DARK;
        applyTheme(newTheme);
    }
    
    /**
     * Check if current theme is dark
     */
    public static boolean isDarkMode() {
        return currentTheme == Theme.PRIMER_DARK || 
               currentTheme == Theme.NORD_DARK || 
               currentTheme == Theme.CUPERTINO_DARK ||
               currentTheme == Theme.DRACULA;
    }
    
    /**
     * Get all available themes
     */
    public static Theme[] getAllThemes() {
        return Theme.values();
    }
}

