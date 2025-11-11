package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for description card
 */
@Slf4j
public class DescriptionCardController implements Initializable {

    @FXML
    private TextArea descriptionArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing DescriptionCard component");
        
        if (descriptionArea != null) {
            descriptionArea.setWrapText(true);
            
            // Auto-save on text change
            descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!oldValue.equals(newValue)) {
                    log.debug("📝 Description updated ({} chars)", newValue.length());
                    // TODO: Implement auto-save
                }
            });
        }
    }

    @FXML
    private void handleAIEnhance() {
        log.info("✨ AI Enhance description");
        
        if (descriptionArea != null && !descriptionArea.getText().trim().isEmpty()) {
            showInfo("AI Enhancement", 
                "AI will analyze and enhance your description:\n\n" +
                "• Improve clarity and structure\n" +
                "• Add missing technical details\n" +
                "• Suggest related components\n" +
                "• Generate user stories\n" +
                "• Create acceptance criteria");
            
            // TODO: Implement actual AI enhancement
        } else {
            showWarning("No Description", "Please add a description first to use AI enhancement.");
        }
    }

    public void setDescription(String description) {
        if (descriptionArea != null) {
            descriptionArea.setText(description);
        }
    }

    public String getDescription() {
        return descriptionArea != null ? descriptionArea.getText() : "";
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

