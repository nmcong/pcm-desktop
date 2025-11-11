package com.noteflix.pcm.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main View Controller for PCM Desktop
 * 
 * PCM (Project Code Management) - AI-Powered System Analysis Tool
 * 
 * This controller manages the main application window, providing navigation
 * and coordination between different modules:
 * - System/Subsystem management
 * - Screen/Form analysis
 * - Database objects management
 * - Batch job tracking
 * - Workflow visualization
 * - AI-powered query interface
 * 
 * @author Noteflix Team
 */
@Slf4j
public class MainController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField sidebarSearch;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox mainContent;

    @FXML
    private TextArea descriptionArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Main Controller...");
        
        setupSearchFunctionality();
        setupSidebar();
        setupMainContent();
        
        log.info("Main Controller initialized");
    }

    /**
     * Setup search functionality
     */
    private void setupSearchFunctionality() {
        log.debug("Setting up search functionality...");
        
        if (sidebarSearch != null) {
            sidebarSearch.setOnAction(e -> handleSidebarSearch(sidebarSearch.getText()));
        }
    }

    /**
     * Setup sidebar navigation
     */
    private void setupSidebar() {
        log.debug("Setting up sidebar...");
        // Sidebar structure is defined in FXML
        // Add any dynamic behavior here if needed
    }

    /**
     * Setup main content area
     */
    private void setupMainContent() {
        log.debug("Setting up main content...");
        
        // Initialize description area if available
        if (descriptionArea != null) {
            descriptionArea.setWrapText(true);
            descriptionArea.setEditable(true);
            
            // Add change listener for description updates
            descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
                log.debug("Description updated");
            });
        }
    }

    /**
     * Create welcome view
     */
    private javafx.scene.layout.VBox createWelcomeView() {
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(20);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setStyle("-fx-padding: 40;");

        Label titleLabel = new Label("Welcome to PCM Desktop");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("AI-Powered System Analysis & Business Management");
        subtitleLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #666;");
        
        Label descriptionLabel = new Label("Analyze source code, manage business logic, and query your enterprise system with AI");
        descriptionLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(600);

        Button getStartedBtn = new Button("Get Started");
        getStartedBtn.setStyle("-fx-font-size: 14; -fx-padding: 10 30;");
        getStartedBtn.setOnAction(e -> handleGetStarted());

        vbox.getChildren().addAll(titleLabel, subtitleLabel, descriptionLabel, getStartedBtn);
        return vbox;
    }

    /**
     * Handle sidebar search
     */
    private void handleSidebarSearch(String query) {
        log.info("Sidebar search: {}", query);
        // Implement sidebar project search
    }

    /**
     * Handle get started button
     */
    @FXML
    private void handleGetStarted() {
        log.info("Get Started clicked");
        showAlert("Getting Started", 
            "Welcome to PCM Desktop!\n\n" +
            "PCM (Project Code Management) is an AI-powered tool for:\n" +
            "• Analyzing enterprise system architecture\n" +
            "• Managing subsystems, screens, and workflows\n" +
            "• Tracking database objects and batch jobs\n" +
            "• Querying system information with natural language\n\n" +
            "Start by exploring the navigation menu on the left.", 
            Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> New action
     */
    @FXML
    private void handleFileNew() {
        log.info("File -> New clicked");
        showAlert("New File", "Create new file functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> Open action
     */
    @FXML
    private void handleFileOpen() {
        log.info("File -> Open clicked");
        showAlert("Open File", "Open file functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> Exit action
     */
    @FXML
    private void handleFileExit() {
        log.info("File -> Exit clicked");
        javafx.application.Platform.exit();
    }

    /**
     * Handle Help -> About action
     */
    @FXML
    private void handleHelpAbout() {
        log.info("Help -> About clicked");
        showAlert(
            "About PCM Desktop",
            "PCM Desktop Application\n" +
            "Version 1.0.0\n\n" +
            "Project Code Management\n" +
            "AI-Powered System Analysis & Business Management Tool\n\n" +
            "Built with JavaFX 21 and Java 21\n" +
            "© 2025 Noteflix Team",
            Alert.AlertType.INFORMATION
        );
    }

    /**
     * Update description area
     */
    private void updateDescription(String description) {
        if (descriptionArea != null) {
            descriptionArea.setText(description);
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

