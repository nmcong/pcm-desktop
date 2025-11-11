package com.noteflix.pcm;

import atlantafx.base.theme.PrimerLight;
import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.ui.MainController;
import com.noteflix.pcm.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * PCM Desktop Application - Main Entry Point
 * 
 * Project Code Management (PCM)
 * AI-Powered System Analysis & Business Management Tool
 * 
 * This application provides comprehensive management and analysis capabilities for
 * enterprise software systems, including:
 * - Source code analysis and mapping
 * - Subsystem and project management
 * - Screen/form tracking with event management
 * - Database object (Oracle) management
 * - Batch job configuration and analysis
 * - Workflow visualization
 * - Knowledge base management
 * - AI-powered natural language queries via LLM integration
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
            Scene scene = new Scene(mainView, AppConstants.DEFAULT_WINDOW_WIDTH, AppConstants.DEFAULT_WINDOW_HEIGHT);

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
                Image icon = new Image(
                    Objects.requireNonNull(
                        getClass().getResourceAsStream(AppConstants.ICON_APP)
                    )
                );
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

    @Override
    public void stop() {
        log.info("Shutting down PCM Desktop Application...");
        // Cleanup resources here
    }
}

