package com.noteflix.pcm.core.navigation;

import com.noteflix.pcm.ui.base.BaseView;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of PageNavigator - Single Responsibility Principle Manages page navigation
 * with history support
 */
@Slf4j
public class DefaultPageNavigator implements PageNavigator {

  private final StackPane contentContainer;
  private final Map<Class<? extends BaseView>, BaseView> pageCache;
  private final Stack<BaseView> navigationHistory;
  private BaseView currentPage;

  public DefaultPageNavigator(StackPane contentContainer) {
    this.contentContainer = contentContainer;
    this.pageCache = new HashMap<>();
    this.navigationHistory = new Stack<>();
  }

  @Override
  public void navigateToPage(BaseView page) {
    if (page == null) {
      log.warn("Attempted to navigate to null page");
      return;
    }

    // Deactivate current page
    if (currentPage != null) {
      currentPage.onDeactivate();
      navigationHistory.push(currentPage);
    }

    // Set new current page
    currentPage = page;

    // Update content container
    contentContainer.getChildren().clear();
    contentContainer.getChildren().add(page);

    // Activate new page
    page.onActivate();

    log.info("Navigated to page: {}", page.getPageTitle());
  }

  @Override
  public void navigateToPage(Class<? extends BaseView> pageClass) {
    BaseView page = getOrCreatePage(pageClass);
    navigateToPage(page);
  }

  @Override
  public BaseView getCurrentPage() {
    return currentPage;
  }

  @Override
  public boolean canGoBack() {
    return !navigationHistory.isEmpty();
  }

  @Override
  public void goBack() {
    if (!canGoBack()) {
      log.warn("Cannot go back - navigation history is empty");
      return;
    }

    BaseView previousPage = navigationHistory.pop();

    // Deactivate current page (don't add to history since we're going back)
    if (currentPage != null) {
      currentPage.onDeactivate();
    }

    // Set previous page as current
    currentPage = previousPage;

    // Update content container
    contentContainer.getChildren().clear();
    contentContainer.getChildren().add(previousPage);

    // Activate previous page
    previousPage.onActivate();

    log.info("Navigated back to page: {}", previousPage.getPageTitle());
  }

  /**
   * Gets an existing page from cache or creates a new one Implements lazy loading for better
   * performance
   */
  private BaseView getOrCreatePage(Class<? extends BaseView> pageClass) {
    return pageCache.computeIfAbsent(
        pageClass,
        clazz -> {
          try {
            BaseView page = clazz.getDeclaredConstructor().newInstance();
            log.debug("Created new page instance: {}", clazz.getSimpleName());
            return page;
          } catch (Exception e) {
            log.error("Failed to create page instance: {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Failed to create page: " + clazz.getSimpleName(), e);
          }
        });
  }

  /** Clears navigation history - useful for memory management */
  public void clearHistory() {
    navigationHistory.clear();
    log.debug("Navigation history cleared");
  }

  /** Clears page cache - useful for forcing page recreation */
  public void clearCache() {
    pageCache.clear();
    log.debug("Page cache cleared");
  }
}
