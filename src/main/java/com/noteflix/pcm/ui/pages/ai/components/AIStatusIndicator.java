package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * Component for displaying AI status indicators
 * Supports different states: thinking, loading, planning, searching
 */
public class AIStatusIndicator extends HBox {

    private final Label statusLabel;
    private final FontIcon statusIcon;

    public AIStatusIndicator() {
        setAlignment(Pos.TOP_LEFT);
        setSpacing(12);
        getStyleClass().add("ai-status-indicator");

        // Avatar
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.getStyleClass().add("message-avatar");
        avatarBox.setPrefSize(32, 32);
        avatarBox.setMinSize(32, 32);
        avatarBox.setMaxSize(32, 32);

        FontIcon avatarIcon = new FontIcon(Octicons.DEPENDABOT_24);
        avatarIcon.setIconSize(18);
        avatarIcon.getStyleClass().add("ai-avatar");

        avatarBox.getChildren().add(avatarIcon);

        // Status content
        VBox contentBox = new VBox(4);
        contentBox.setMaxWidth(600);

        HBox statusRow = new HBox(8);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        statusIcon = new FontIcon(Octicons.SYNC_16);
        statusIcon.getStyleClass().add("status-icon");

        statusLabel = new Label("AI is thinking...");
        statusLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "status-text");

        statusRow.getChildren().addAll(statusIcon, statusLabel);
        contentBox.getChildren().add(statusRow);

        getChildren().addAll(avatarBox, contentBox);
    }

    public void showThinking() {
        statusIcon.setIconCode(Octicons.SYNC_16);
        statusLabel.setText("AI is thinking...");
        getStyleClass().removeAll("status-loading", "status-planning", "status-searching");
        getStyleClass().add("status-thinking");
    }

    public void showLoading() {
        statusIcon.setIconCode(Octicons.DOWNLOAD_16);
        statusLabel.setText("Loading data...");
        getStyleClass().removeAll("status-thinking", "status-planning", "status-searching");
        getStyleClass().add("status-loading");
    }

    public void showPlanning() {
        statusIcon.setIconCode(Octicons.CHECKLIST_16);
        statusLabel.setText("Creating plan...");
        getStyleClass().removeAll("status-thinking", "status-loading", "status-searching");
        getStyleClass().add("status-planning");
    }

    public void showSearching(String query) {
        statusIcon.setIconCode(Octicons.SEARCH_16);
        statusLabel.setText("Searching for: " + query);
        getStyleClass().removeAll("status-thinking", "status-loading", "status-planning");
        getStyleClass().add("status-searching");
    }

    public void setCustomStatus(String iconCode, String message) {
        try {
            Octicons icon = Octicons.valueOf(iconCode);
            statusIcon.setIconCode(icon);
            statusLabel.setText(message);
        } catch (IllegalArgumentException e) {
            statusIcon.setIconCode(Octicons.INFO_16);
            statusLabel.setText(message);
        }
    }
}

