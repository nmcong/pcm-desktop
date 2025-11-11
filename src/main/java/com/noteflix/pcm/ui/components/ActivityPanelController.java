package com.noteflix.pcm.ui.components;

import javafx.fxml.Initializable;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for activity panel
 */
@Slf4j
public class ActivityPanelController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing ActivityPanel component");
    }

    // Methods to add/update activities can be added here
    // For now, activities are static in the FXML
}

