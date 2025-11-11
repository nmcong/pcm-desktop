package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for content header with tabs
 */
@Slf4j
public class ContentHeaderController implements Initializable {

    @FXML
    private Label pageTitle;
    
    @FXML
    private Label statusText;
    
    @FXML
    private Label metaText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing ContentHeader component");
    }

    @FXML
    private void handleShare() {
        log.info("Share button clicked");
        showInfo("Share", "Share this screen with your team");
    }

    @FXML
    private void handleMore() {
        log.info("More options clicked");
        showInfo("More Options", 
            "• Edit\n" +
            "• Duplicate\n" +
            "• Export\n" +
            "• Delete");
    }

    @FXML
    private void handleTabOverview() {
        log.info("Tab: Overview");
        showInfo("Overview", "Screen overview and details");
    }

    @FXML
    private void handleTabEvents() {
        log.info("Tab: Events");
        showInfo("Events", "Screen events and interactions");
    }

    @FXML
    private void handleTabSourceCode() {
        log.info("Tab: Source Code");
        showInfo("Source Code", "View related source code files");
    }

    @FXML
    private void handleTabDatabase() {
        log.info("Tab: Database");
        showInfo("Database", "View related database objects");
    }

    @FXML
    private void handleTabAPI() {
        log.info("Tab: API Endpoints");
        showInfo("API Endpoints", "View related API endpoints");
    }

    public void setTitle(String title) {
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
    }

    public void setStatus(String status) {
        if (statusText != null) {
            statusText.setText(status);
        }
    }

    public void setMeta(String meta) {
        if (metaText != null) {
            metaText.setText(meta);
        }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

