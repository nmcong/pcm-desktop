package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for properties panel
 */
@Slf4j
public class PropertiesPanelController implements Initializable {

    @FXML
    private Label statusValue;
    
    @FXML
    private Label projectValue;
    
    @FXML
    private Label screenIdValue;
    
    @FXML
    private Label createdValue;
    
    @FXML
    private Label updatedValue;
    
    @FXML
    private Label authorValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing PropertiesPanel component");
    }

    public void setStatus(String status) {
        if (statusValue != null) {
            statusValue.setText(status);
        }
    }

    public void setProject(String project) {
        if (projectValue != null) {
            projectValue.setText(project);
        }
    }

    public void setScreenId(String screenId) {
        if (screenIdValue != null) {
            screenIdValue.setText(screenId);
        }
    }

    public void setCreated(String created) {
        if (createdValue != null) {
            createdValue.setText(created);
        }
    }

    public void setUpdated(String updated) {
        if (updatedValue != null) {
            updatedValue.setText(updated);
        }
    }

    public void setAuthor(String author) {
        if (authorValue != null) {
            authorValue.setText(author);
        }
    }
}

