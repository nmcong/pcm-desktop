package com.noteflix.pcm.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main Controller for PCM Desktop WebApp
 * 
 * This controller manages the main PCM interface for project code management.
 * Individual components are now managed by their own controllers:
 * - NavbarController: Top navigation bar
 * - SidebarController: Left sidebar with navigation and projects
 * - ContentHeaderController: Page header with tabs
 * - StatsCardsController: Statistics cards
 * - DescriptionCardController: Description editor
 * - TagsCardController: Tags management
 * - RelatedItemsCardController: Related items
 * - PropertiesPanelController: Properties panel
 * - ActivityPanelController: Activity feed
 * 
 * @author Noteflix Team
 * @version 3.0.0 - Modular Component Architecture
 */
@Slf4j
public class MainController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox mainContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("🚀 Initializing PCM Desktop - Modular Component Architecture...");
        
        try {
            log.info("✅ PCM WebApp Controller initialized successfully");
            log.info("📦 All components are now self-contained with their own controllers");
        } catch (Exception e) {
            log.error("❌ Error initializing PCM controller", e);
        }
    }

    // ===== MENU HANDLERS =====

    /**
     * Handle File -> New
     */
    @FXML
    private void handleFileNew() {
        log.info("📄 File -> New");
        showInfo("New Screen", 
            "Create New Screen\n\n" +
            "Choose template:\n" +
            "• Blank screen\n" +
            "• List view\n" +
            "• Detail view\n" +
            "• Form\n" +
            "• Dashboard");
    }

    /**
     * Handle File -> Open
     */
    @FXML
    private void handleFileOpen() {
        log.info("📂 File -> Open");
        showInfo("Open Project", "Select a PCM project to open");
    }

    /**
     * Handle File -> Save
     */
    @FXML
    private void handleFileSave() {
        log.info("💾 File -> Save");
        showInfo("Save", "Saving current changes...");
        // TODO: Implement save functionality
    }

    /**
     * Handle File -> Exit
     */
    @FXML
    private void handleFileExit() {
        log.info("🚪 File -> Exit");
        javafx.application.Platform.exit();
    }

    /**
     * Handle Help -> About
     */
    @FXML
    private void handleHelpAbout() {
        log.info("ℹ️ Help -> About");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About PCM Desktop");
        alert.setHeaderText("PCM Desktop - Project Code Management");
        alert.setContentText(
            "Version 3.0.0 - Modular Component Architecture\n\n" +
            "AI-Powered System Analysis & Business Management\n\n" +
            "Features:\n" +
            "• Screen and form tracking\n" +
            "• Workflow visualization\n" +
            "• Database object management\n" +
            "• Source code analysis\n" +
            "• AI-powered natural language queries\n" +
            "• Knowledge base integration\n\n" +
            "Architecture:\n" +
            "• Modular component-based design\n" +
            "• Self-contained component controllers\n" +
            "• Reusable UI components\n\n" +
            "Built with JavaFX 21 and Java 21\n" +
            "© 2025 Noteflix Team"
        );
        alert.showAndWait();
    }

    // ===== UTILITY METHODS =====

    /**
     * Show information dialog
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show warning dialog
     */
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Refresh current view
     */
    public void refreshView() {
        log.info("🔄 Refreshing view");
        // TODO: Implement view refresh
    }
}
