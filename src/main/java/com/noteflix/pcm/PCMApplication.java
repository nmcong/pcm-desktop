package com.noteflix.pcm;

import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.provider.ProviderSyncService;
import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.core.utils.Asyncs;
import com.noteflix.pcm.infrastructure.database.ConnectionManager;
import com.noteflix.pcm.infrastructure.database.DatabaseMigrationManager;
import com.noteflix.pcm.ui.MainController;
import com.noteflix.pcm.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * PCM Desktop Application - Main Entry Point
 *
 * <p>Project Cognition Mentor (PCM) AI-Powered System Analysis & Business Management Tool
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
        log.info("🚀 Starting Project Cognition Mentor - AI-Powered System Analysis Tool...");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        // Initialize DI container
        log.info("🔧 Initializing Dependency Injection...");
        Injector.getInstance(); // Initialize DI container and register default dependencies
        log.info("✅ DI Container initialized");

        // Initialize i18n
        log.info("🌍 Initializing internationalization...");
        I18n.setLocale("en"); // Default to English
        log.info("✅ i18n initialized: {}", I18n.getCurrentLocale().getDisplayName());

        // Auto-initialize database if needed
        log.info("🔄 Checking database...");
        initializeDatabase();
        log.info("✅ Database ready");

        // Initialize providers and sync to database
        log.info("🔄 Initializing LLM providers...");
        initializeProviders();
        log.info("✅ LLM providers initialized and synced to database");
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

            // Show window
            primaryStage.show();

            log.info("✅ Application started successfully");
            log.info("🎨 UI built with pure Java code following AtlantaFX Sampler patterns");

        } catch (Exception e) {
            log.error("❌ Failed to start application", e);
            throw new RuntimeException("Failed to load application UI", e);
        }
    }

    /**
     * Initialize database - auto-creates tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            DatabaseMigrationManager migrationManager =
                    new DatabaseMigrationManager(ConnectionManager.INSTANCE);

            // This will only run if tables don't exist (idempotent)
            migrationManager.migrate();

            log.info("✅ Database initialized");

        } catch (Exception e) {
            log.error("❌ Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Initialize LLM providers and sync to database
     */
    private void initializeProviders() {
        try {
            // Initialize AIService which registers all providers
            AIService aiService = new AIService();
            log.info("✅ AIService initialized with providers");

            // Sync registered providers to database
            ProviderSyncService syncService = new ProviderSyncService();
            int syncedCount = syncService.syncProvidersToDatabase();
            syncService.syncActiveProvider();
            log.info("✅ Synced {} provider(s) to database", syncedCount);

        } catch (Exception e) {
            log.error("❌ Provider initialization failed", e);
            // Don't throw - allow app to start even if providers fail
            log.warn("Application will continue without LLM providers");
        }
    }

    @Override
    public void stop() {
        log.info("Shutting down Project Cognition Mentor...");

        // Shutdown async executor
        Asyncs.shutdown();
        log.info("✅ Async executor shutdown complete");

        // Other cleanup
        log.info("✅ Application shutdown complete");
    }
}
