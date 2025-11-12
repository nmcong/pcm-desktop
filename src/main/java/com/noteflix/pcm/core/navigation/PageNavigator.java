package com.noteflix.pcm.core.navigation;

import com.noteflix.pcm.ui.pages.BasePage;

/**
 * Interface for page navigation - Dependency Inversion Principle Allows different implementations
 * of navigation without tight coupling
 */
public interface PageNavigator {

  /**
   * Navigate to a specific page
   *
   * @param page The page to navigate to
   */
  void navigateToPage(BasePage page);

  /**
   * Navigate to a page by its class type
   *
   * @param pageClass The class of the page to navigate to
   */
  void navigateToPage(Class<? extends BasePage> pageClass);

  /**
   * Get the currently active page
   *
   * @return The current page, or null if none is active
   */
  BasePage getCurrentPage();

  /**
   * Check if navigation can go back
   *
   * @return true if back navigation is possible
   */
  boolean canGoBack();

  /** Navigate back to the previous page */
  void goBack();
}
