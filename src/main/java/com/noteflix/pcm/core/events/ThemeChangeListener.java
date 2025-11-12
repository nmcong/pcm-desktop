package com.noteflix.pcm.core.events;

/** Interface for components that need to react to theme changes */
public interface ThemeChangeListener {

  /**
   * Called when the theme changes
   *
   * @param isDarkTheme true if switching to dark theme, false for light theme
   */
  void onThemeChanged(boolean isDarkTheme);
}
