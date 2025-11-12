package com.noteflix.pcm.core.navigation;

/**
 * Route enumeration for application navigation
 *
 * <p>Defines all possible routes/pages in the application. This enum-based approach provides: -
 * Type safety (no magic strings) - Compile-time validation - Easy refactoring - Clear overview of
 * all application routes
 *
 * <p>Follows: Open/Closed Principle - easy to extend by adding new enum values
 *
 * @author PCM Team
 * @version 2.0.0
 */
public enum Route {
  AI_ASSISTANT("AI Assistant", "ai-assistant"),
  KNOWLEDGE_BASE("Knowledge Base", "knowledge-base"),
  DATABASE_OBJECTS("Database Objects", "database-objects"),
  BATCH_JOBS("Batch Jobs", "batch-jobs"),
  SETTINGS("Settings", "settings"),
  UNIVERSAL_TEXT_DEMO("Universal Text Demo", "universal-text-demo"),
  CSS_TEST("CSS Test", "css-test");

  private final String displayName;
  private final String routeId;

  Route(String displayName, String routeId) {
    this.displayName = displayName;
    this.routeId = routeId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getRouteId() {
    return routeId;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
