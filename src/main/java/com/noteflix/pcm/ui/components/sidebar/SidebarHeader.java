package com.noteflix.pcm.ui.components.sidebar;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * Header component for the sidebar containing logo and search button
 * Follows Single Responsibility Principle - only handles header UI
 */
@Slf4j
public class SidebarHeader extends VBox {

    private final FontIcon appIcon;
    private final Runnable onSearchAction;

    public SidebarHeader(Runnable onSearchAction) {
        super(20);
        this.onSearchAction = onSearchAction;
        this.appIcon = new FontIcon(Octicons.CPU_16);

        getStyleClass().add("header");
        buildHeader();
    }

    private void buildHeader() {
        // Logo section
        appIcon.getStyleClass().add("app-icon");

        Label titleLabel = new Label("PCM");
        titleLabel.getStyleClass().add(Styles.TITLE_3);

        HBox logoSection = new HBox(10, appIcon, titleLabel);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.getStyleClass().add("logo");

        // Add components
        getChildren().addAll(logoSection, createSearchButton());
    }

    private Button createSearchButton() {
        // Search icon and label
        FontIcon searchIcon = new FontIcon(Octicons.SEARCH_24);
        searchIcon.setIconSize(14);

        Label searchLabel = new Label("Search");
        searchLabel.setGraphic(searchIcon);
        searchLabel.getStyleClass().add("search-label");
        searchLabel.setGraphicTextGap(8);

        // Keyboard hint
        Label hintLabel = new Label("Press /");
        hintLabel.getStyleClass().addAll("hint", "text-muted", "text-small");

        // Content container
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox content = new HBox(12, searchLabel, spacer, hintLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getStyleClass().add("content");

        // Search button
        Button searchButton = new Button();
        searchButton.setGraphic(content);
        searchButton.getStyleClass().add("search-button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setAlignment(Pos.CENTER_LEFT);
        searchButton.setOnAction(e -> {
            if (onSearchAction != null) {
                onSearchAction.run();
            }
        });

        return searchButton;
    }

    public FontIcon getAppIcon() {
        return appIcon;
    }
}