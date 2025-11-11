package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for related items card
 */
@Slf4j
public class RelatedItemsCardController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing RelatedItemsCard component");
    }

    @FXML
    private void handleRelatedItemClick(MouseEvent event) {
        log.info("🔗 Related item clicked");
        showInfo("Navigate", "Opening related screen...");
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

