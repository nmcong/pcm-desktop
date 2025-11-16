package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Welcome screen component shown when no conversation is active
 */
public class WelcomeScreen extends VBox {

    private final ChatEventHandler eventHandler;

    public WelcomeScreen(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        setAlignment(Pos.CENTER);
        getStyleClass().add("welcome-content");
        setPadding(new Insets(40));
        setSpacing(24);

        // AI icon with gradient background
        VBox iconContainer = createWelcomeIcon();

        Label title = new Label("AI Assistant");
        title.getStyleClass().addAll(Styles.TITLE_2);

        Label subtitle = new Label("How can I help you today?");
        subtitle.getStyleClass().addAll(Styles.TEXT_MUTED);

        // Quick suggestions
        GridPane suggestions = createQuickSuggestions();

        getChildren().addAll(iconContainer, title, subtitle, suggestions);
    }

    private VBox createWelcomeIcon() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("welcome-icon-container");
        container.setPrefSize(90, 90);
        container.setMinSize(90, 90);
        container.setMaxSize(90, 90);

        FontIcon mainIcon = new FontIcon(AntDesignIconsOutlined.COMMENT);
        mainIcon.setIconSize(40);
        mainIcon.getStyleClass().add("welcome-ai-icon");

        container.getChildren().add(mainIcon);
        return container;
    }

    private GridPane createQuickSuggestions() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(16);
        grid.setVgap(16);
        grid.getStyleClass().add("quick-suggestions");
        grid.setMaxWidth(600);

        // Define 4 suggestion themes with icons
        SuggestionCard[] suggestions = {
                new SuggestionCard(
                        Feather.SEARCH, "Search Knowledge", "Explore database schema and structure"),
                new SuggestionCard(
                        Feather.TOOL, "Find Solutions", "Review code quality and best practices"),
                new SuggestionCard(
                        Feather.EDIT_3, "Create Content", "Generate documentation and reports"),
                new SuggestionCard(
                        Feather.ACTIVITY, "Analyze System", "Get insights on performance and metrics")
        };

        // Add cards to grid (2x2 layout)
        int row = 0, col = 0;
        for (SuggestionCard suggestion : suggestions) {
            VBox card = createSuggestionCard(suggestion);
            grid.add(card, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private VBox createSuggestionCard(SuggestionCard suggestion) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("suggestion-card");
        card.setPrefWidth(280);
        card.setPrefHeight(120);

        // Icon
        FontIcon icon = new FontIcon(suggestion.icon());
        icon.setIconSize(28);
        icon.getStyleClass().add("suggestion-icon");

        // Title
        Label titleLabel = new Label(suggestion.title());
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "suggestion-title");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(250);

        // Description
        Label descLabel = new Label(suggestion.description());
        descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "suggestion-desc", "text-muted");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(250);

        card.getChildren().addAll(icon, titleLabel, descLabel);

        // Click handler
        card.setOnMouseClicked(e -> eventHandler.onSendMessage(suggestion.description()));

        return card;
    }
}

