package com.noteflix.pcm.ui.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for statistics cards
 */
@Slf4j
public class StatsCardsController implements Initializable {

    @FXML
    private Label eventsValue;
    
    @FXML
    private Label eventsChange;
    
    @FXML
    private Label filesValue;
    
    @FXML
    private Label filesChange;
    
    @FXML
    private Label tablesValue;
    
    @FXML
    private Label tablesChange;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing StatsCards component");
    }

    public void updateStats(int events, int files, int tables) {
        if (eventsValue != null) {
            eventsValue.setText(String.valueOf(events));
        }
        if (filesValue != null) {
            filesValue.setText(String.valueOf(files));
        }
        if (tablesValue != null) {
            tablesValue.setText(String.valueOf(tables));
        }
    }

    public void setEventsChange(String change, boolean positive) {
        if (eventsChange != null) {
            eventsChange.setText(change);
            eventsChange.getStyleClass().clear();
            eventsChange.getStyleClass().add("stat-change");
            eventsChange.getStyleClass().add(positive ? "positive" : "neutral");
        }
    }

    public void setFilesChange(String change, boolean positive) {
        if (filesChange != null) {
            filesChange.setText(change);
            filesChange.getStyleClass().clear();
            filesChange.getStyleClass().add("stat-change");
            filesChange.getStyleClass().add(positive ? "positive" : "neutral");
        }
    }

    public void setTablesChange(String change, boolean positive) {
        if (tablesChange != null) {
            tablesChange.setText(change);
            tablesChange.getStyleClass().clear();
            tablesChange.getStyleClass().add("stat-change");
            tablesChange.getStyleClass().add(positive ? "positive" : "neutral");
        }
    }
}

