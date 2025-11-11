package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for top navigation bar
 */
@Slf4j
public class NavbarController implements Initializable {

    @FXML
    private Button notificationBtn;
    
    @FXML
    private Button settingsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing Navbar component");
    }

    @FXML
    private void handleNewScreen() {
        log.info("➕ New Screen button clicked");
        showInfo("New Screen", "Creating new screen...");
    }

    @FXML
    private void handleNotifications() {
        log.info("🔔 Notifications button clicked");
        showInfo("Notifications", 
            "Recent Activity:\n\n" +
            "• John Doe updated 'User Profile' screen\n" +
            "• Sarah Kim created new workflow 'Payment Flow'\n" +
            "• System: Database sync completed");
    }

    @FXML
    private void handleSettings() {
        log.info("⚙️ Settings button clicked");
        showInfo("Settings", 
            "PCM Desktop Settings\n\n" +
            "Available settings:\n" +
            "• User preferences\n" +
            "• Project configuration\n" +
            "• Database connections");
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

