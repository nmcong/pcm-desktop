package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for left sidebar navigation
 */
@Slf4j
public class SidebarController implements Initializable {

    @FXML
    private VBox sidebar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing Sidebar component");
    }

    @FXML
    private void handleNewProject() {
        log.info("Creating new project");
        showInfo("New Project", "Create a new project");
    }

    @FXML
    private void handleProject(MouseEvent event) {
        log.info("Opening project");
        showInfo("Project", "View project details");
    }

    @FXML
    private void handleKnowledgeBase(MouseEvent event) {
        log.info("Opening Knowledge Base");
        showInfo("Knowledge Base", 
            "Browse and search your knowledge base:\n\n" +
            "• Documentation\n" +
            "• Best practices\n" +
            "• Design patterns\n" +
            "• Technical notes");
    }

    @FXML
    private void handleBatchJobs(MouseEvent event) {
        log.info("Opening Batch Jobs");
        showInfo("Batch Jobs", 
            "Manage scheduled and batch operations:\n\n" +
            "• View running jobs\n" +
            "• Schedule new tasks\n" +
            "• Job history\n" +
            "• Execution logs");
    }

    @FXML
    private void handleDBObjects(MouseEvent event) {
        log.info("Opening DB Objects");
        showInfo("Database Objects", 
            "Database schema and objects:\n\n" +
            "• Tables\n" +
            "• Views\n" +
            "• Stored procedures\n" +
            "• Triggers & Functions");
    }

    @FXML
    private void handleSettingsMenu(MouseEvent event) {
        log.info("Opening Settings from menu");
        showInfo("Settings", 
            "Application configuration:\n\n" +
            "• User preferences\n" +
            "• Project settings\n" +
            "• Database connections\n" +
            "• Theme & appearance");
    }

    @FXML
    private void handleSettings(MouseEvent event) {
        log.info("Opening Settings");
        showInfo("Settings", 
            "PCM Desktop Settings\n\n" +
            "Configure your preferences:\n" +
            "• User preferences\n" +
            "• Project configuration\n" +
            "• Database connections\n" +
            "• Theme customization");
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

