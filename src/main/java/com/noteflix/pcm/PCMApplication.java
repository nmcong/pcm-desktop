package com.noteflix.pcm;

import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.core.utils.Asyncs;
import com.noteflix.pcm.infrastructure.database.ConnectionManager;
import com.noteflix.pcm.infrastructure.database.DatabaseMigrationManager;
import com.noteflix.pcm.ui.MainController;
import com.noteflix.pcm.ui.MainView;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * PCM Desktop Application - Main Entry Point
 *
 * <p>Project Code Management (PCM) AI-Powered System Analysis & Business Management Tool
 *
 * <p>This application provides comprehensive management and analysis capabilities for enterprise
 * software systems, including: - Source code analysis and mapping - Subsystem and project
 * management - Screen/form tracking with event management - Database object (Oracle) management -
 * Batch job configuration and analysis - Workflow visualization - Knowledge base management -
 * AI-powered natural language queries via LLM integration
 *
 * @author Noteflix Team
 * @version 1.0.0
 */
@Slf4j
public class PCMApplication extends Application {

  public static void main(String[] args) {
    log.info("🚀 Starting PCM Desktop Application - AI-Powered System Analysis Tool...");
    launch(args);
  }

  @Override
  public void init() throws Exception {
    super.init();

    // Initialize DI container
    log.info("🔧 Initializing Dependency Injection...");
    Injector injector = Injector.getInstance();
    log.info("✅ DI Container initialized");

    // Initialize i18n
    log.info("🌍 Initializing internationalization...");
    I18n.setLocale("en"); // Default to English
    log.info("✅ i18n initialized: {}", I18n.getCurrentLocale().getDisplayName());

    // Run database migrations BEFORE UI initialization
    log.info("🔄 Running database migrations...");
    runDatabaseMigrations();
  }

  @Override
  public void start(Stage primaryStage) {
    try {
      log.info("Initializing application window...");

      // Initialize ThemeManager with default light theme
      ThemeManager themeManager = ThemeManager.getInstance();
      log.info("✨ ThemeManager initialized");

      // Create controller and view (Java-based, no FXML)
      MainController controller = new MainController();
      MainView mainView = new MainView(controller);
      log.info("✅ Main view created with Java code (no FXML)");

      // Create scene
      Scene scene =
          new Scene(
              mainView, AppConstants.DEFAULT_WINDOW_WIDTH, AppConstants.DEFAULT_WINDOW_HEIGHT);

      // Set scene reference in ThemeManager for CSS management
      themeManager.setMainScene(scene);
      themeManager.setTheme(false); // Start with light theme
      log.info("✨ Theme system initialized with CSS management");

      // Configure stage
      primaryStage.setTitle(AppConstants.APP_TITLE);
      primaryStage.setScene(scene);
      primaryStage.setMinWidth(AppConstants.MIN_WINDOW_WIDTH);
      primaryStage.setMinHeight(AppConstants.MIN_WINDOW_HEIGHT);

      // Set application icon (optional)
      try {
        Image icon =
            new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(AppConstants.ICON_APP)));
        primaryStage.getIcons().add(icon);
      } catch (Exception e) {
        log.warn("Could not load application icon: {}", e.getMessage());
      }

      // Show window
      primaryStage.show();

      log.info("✅ Application started successfully");
      log.info("🎨 UI built with pure Java code following AtlantaFX Sampler patterns");

    } catch (Exception e) {
      log.error("❌ Failed to start application", e);
      throw new RuntimeException("Failed to load application UI", e);
    }
  }

  /** Run database migrations */
  private void runDatabaseMigrations() {
    try {
      DatabaseMigrationManager migrationManager =
          new DatabaseMigrationManager(ConnectionManager.INSTANCE);

      migrationManager.migrate();

      log.info("✅ Database migrations completed");

    } catch (Exception e) {
      log.error("❌ Database migration failed", e);
      throw new RuntimeException("Failed to run database migrations", e);
    }
  }

  @Override
  public void stop() {
    log.info("Shutting down PCM Desktop Application...");

    // Shutdown async executor
    Asyncs.shutdown();
    log.info("✅ Async executor shutdown complete");

    // Other cleanup
    log.info("✅ Application shutdown complete");
  }
}
