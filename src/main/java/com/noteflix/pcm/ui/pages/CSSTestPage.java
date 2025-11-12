package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Test page for CSS theming system
 * Demonstrates how ai-assistant-dark.css is applied
 */
@Slf4j
public class CSSTestPage extends BasePage {

    private ThemeManager themeManager = ThemeManager.getInstance();

    public CSSTestPage() {
        super(
                "CSS Theme Test",
                "Test the theme system and ai-assistant-dark.css application",
                new FontIcon(Feather.SETTINGS)
        );
    }

    @Override
    protected VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // Theme controls
        VBox themeControls = createThemeControls();

        // Test elements with ai-assistant-dark.css classes
        VBox testElements = createTestElements();

        content.getChildren().addAll(themeControls, testElements);

        return content;
    }

    private VBox createThemeControls() {
        VBox controls = new VBox(10);
        controls.getStyleClass().add("card");
        controls.setPadding(new Insets(16));
        controls.setAlignment(Pos.CENTER);

        Label title = new Label("Theme Controls");
        title.getStyleClass().add(Styles.TITLE_3);

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER);

        Button lightButton = new Button("Light Theme");
        lightButton.setGraphic(new FontIcon(Feather.SUN));
        lightButton.getStyleClass().add(Styles.ACCENT);
        lightButton.setOnAction(e -> {
            themeManager.setTheme(false);
            log.info("Switched to light theme");
        });

        Button darkButton = new Button("Dark Theme");
        darkButton.setGraphic(new FontIcon(Feather.MOON));
        darkButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        darkButton.setOnAction(e -> {
            themeManager.setTheme(true);
            log.info("Switched to dark theme - ai-assistant-dark.css should be applied");
        });

        Button toggleButton = new Button("Toggle Theme");
        toggleButton.setGraphic(new FontIcon(Feather.REFRESH_CCW));
        toggleButton.setOnAction(e -> {
            themeManager.toggleTheme();
            log.info("Toggled theme - Current: {}", themeManager.isDarkTheme() ? "Dark" : "Light");
        });

        buttonRow.getChildren().addAll(lightButton, darkButton, toggleButton);

        Label currentTheme = new Label("Current: " + (themeManager != null && themeManager.isDarkTheme() ? "Dark" : "Light"));
        currentTheme.getStyleClass().add("text-muted");

        // Update label when theme changes
        themeManager.addThemeChangeListener(isDark -> {
            javafx.application.Platform.runLater(() ->
                    currentTheme.setText("Current: " + (isDark ? "Dark" : "Light"))
            );
        });

        controls.getChildren().addAll(title, buttonRow, currentTheme);
        return controls;
    }

    private VBox createTestElements() {
        VBox elements = new VBox(15);
        elements.getStyleClass().add("card");
        elements.setPadding(new Insets(16));

        Label title = new Label("Test Elements with ai-assistant-dark.css Classes");
        title.getStyleClass().add(Styles.TITLE_4);

        // Chat-like elements to test ai-assistant-dark.css
        VBox chatSection = createChatTestSection();

        // Color test section
        VBox colorSection = createColorTestSection();

        elements.getChildren().addAll(title, chatSection, colorSection);
        return elements;
    }

    private VBox createChatTestSection() {
        VBox chatSection = new VBox(10);
        chatSection.getStyleClass().add("ai-chat-page"); // This class is in ai-assistant-dark.css
        chatSection.setPadding(new Insets(16));

        Label chatTitle = new Label("AI Chat Test Area");
        chatTitle.getStyleClass().add(Styles.TEXT_BOLD);

        // Simulated chat messages
        VBox userMessage = new VBox(5);
        userMessage.getStyleClass().add("user-message-bubble");
        userMessage.setPadding(new Insets(10));
        userMessage.setMaxWidth(300);
        userMessage.setAlignment(Pos.CENTER_RIGHT);

        Label userText = new Label("This is a user message bubble");
        userText.setWrapText(true);
        userMessage.getChildren().add(userText);

        VBox aiMessage = new VBox(5);
        aiMessage.getStyleClass().add("ai-message-bubble");
        aiMessage.setPadding(new Insets(10));
        aiMessage.setMaxWidth(300);

        Label aiText = new Label("This is an AI message bubble with some longer text to test wrapping");
        aiText.setWrapText(true);
        aiMessage.getChildren().add(aiText);

        chatSection.getChildren().addAll(chatTitle, userMessage, aiMessage);
        return chatSection;
    }

    private VBox createColorTestSection() {
        VBox colorSection = new VBox(10);
        colorSection.setPadding(new Insets(16));

        Label colorTitle = new Label("Color Variables Test");
        colorTitle.getStyleClass().add(Styles.TEXT_BOLD);

        // Test different color classes from ai-assistant-dark.css
        HBox colorRow1 = new HBox(10);
        colorRow1.setAlignment(Pos.CENTER);

        Button primaryButton = new Button("Primary Color");
        primaryButton.setStyle("-fx-background-color: -fx-accent-primary; -fx-text-fill: white;");

        Button secondaryButton = new Button("Secondary Color");
        secondaryButton.setStyle("-fx-background-color: -fx-accent-secondary; -fx-text-fill: white;");

        colorRow1.getChildren().addAll(primaryButton, secondaryButton);

        HBox colorRow2 = new HBox(10);
        colorRow2.setAlignment(Pos.CENTER);

        Label successLabel = new Label("Success Color");
        successLabel.setStyle("-fx-background-color: -fx-success; -fx-text-fill: white; -fx-padding: 5px 10px;");

        Label warningLabel = new Label("Warning Color");
        warningLabel.setStyle("-fx-background-color: -fx-warning; -fx-text-fill: white; -fx-padding: 5px 10px;");

        Label errorLabel = new Label("Error Color");
        errorLabel.setStyle("-fx-background-color: -fx-error; -fx-text-fill: white; -fx-padding: 5px 10px;");

        colorRow2.getChildren().addAll(successLabel, warningLabel, errorLabel);

        colorSection.getChildren().addAll(colorTitle, colorRow1, colorRow2);
        return colorSection;
    }

    @Override
    public void onPageActivated() {
        super.onPageActivated();
        log.info("CSS Test page activated. Current theme: {}", themeManager.isDarkTheme() ? "Dark" : "Light");
        log.info("To test: Toggle theme and observe ai-assistant-dark.css variables in action");
    }
}