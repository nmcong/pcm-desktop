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
 * Personal Content Manager for Desktop
 */
@Slf4j
public class PCMApplication extends Application {

    private static final String APP_TITLE = "PCM - Personal Content Manager";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;

    public static void main(String[] args) {
        log.info("🚀 Starting PCM Desktop Application...");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            log.info("Initializing application window...");

            // Load FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainView.fxml")
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

