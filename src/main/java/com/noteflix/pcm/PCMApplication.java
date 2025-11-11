package com.noteflix.pcm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

    private static final String APP_TITLE = "PCM Desktop - AI-Powered System Analysis";
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        log.info("🚀 Starting PCM Desktop Application - AI-Powered System Analysis Tool...");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("Initializing application window...");

            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("fxml/MainView.fxml")
            );
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Load CSS
            String css = Objects.requireNonNull(
                getClass().getResource("/css/styles.css")
            ).toExternalForm();
            scene.getStylesheets().add(css);

            // Configure stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);

            // Set application icon (optional)
            try {
                Image icon = new Image(
                    Objects.requireNonNull(
                        getClass().getResourceAsStream("/images/app-icon.png")
                    )
                );
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                log.warn("Could not load application icon: {}", e.getMessage());
            }

            // Show window
            primaryStage.show();

            log.info("✅ Application started successfully");

        } catch (IOException e) {
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

