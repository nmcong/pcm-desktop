package com.noteflix.pcm.ui.pages.settings.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import com.noteflix.pcm.ui.pages.settings.tabs.LLMProvidersTab.ProviderAction;
import com.noteflix.pcm.ui.pages.settings.tabs.LLMProvidersTab.ProviderActionType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.function.Consumer;

/**
 * Provider Configuration Card
 *
 * <p>Displays and manages configuration for a single LLM provider
 *
 * <p>Features:
 * - Display provider info
 * - Edit API key, base URL, default model
 * - Test connection
 * - Load available models
 * - Set as active provider
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ProviderConfigCard extends VBox {

    private final ProviderConfiguration config;
    private final Consumer<ProviderAction> actionHandler;

    // UI components
    private TextField apiKeyField;
    private TextField baseUrlField;
    private ComboBox<String> modelComboBox;
    private ToggleButton activeToggle;
    private ToggleButton enabledToggle;
    private Label statusLabel;
    private Button testButton;
    private Button loadModelsButton;
    private Button saveButton;

    public ProviderConfigCard(
            ProviderConfiguration config, Consumer<ProviderAction> actionHandler) {
        this.config = config;
        this.actionHandler = actionHandler;

        setSpacing(0);
        getStyleClass().add("provider-config-card");

        // Build UI
        buildCard();
    }

    private void buildCard() {
        // Header
        HBox header = createHeader();

        // Content (collapsible)
        VBox content = createContent();

        // Add to card
        getChildren().addAll(header, content);
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setPadding(new Insets(16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");

        // Icon
        FontIcon icon = new FontIcon(Octicons.COMMENT_DISCUSSION_24);
        icon.setIconSize(24);
        icon.getStyleClass().add("provider-icon");

        // Title & subtitle
        VBox titleBox = new VBox(4);
        Label title = new Label(config.getDisplayName());
        title.getStyleClass().addAll(Styles.TITLE_4);

        Label subtitle = new Label(config.getProviderName());
        subtitle.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status badge
        statusLabel = new Label();
        updateStatusLabel();
        statusLabel.getStyleClass().addAll(Styles.SMALL);

        // Active toggle
        activeToggle = new ToggleButton();
        activeToggle.setSelected(config.isActive());
        activeToggle.setTooltip(new Tooltip("Set as active provider"));
        FontIcon starIcon = new FontIcon(
                config.isActive() ? Octicons.STAR_FILL_16 : Octicons.STAR_16);
        starIcon.setIconSize(16);
        activeToggle.setGraphic(starIcon);
        activeToggle.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        activeToggle.setOnAction(e -> handleSetActive());

        header.getChildren().addAll(icon, titleBox, statusLabel, activeToggle);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(0, 16, 16, 16));
        content.getStyleClass().add("card-content");

        // Enabled toggle
        HBox enabledRow = new HBox(8);
        enabledRow.setAlignment(Pos.CENTER_LEFT);
        Label enabledLabel = new Label("Enabled");
        enabledLabel.getStyleClass().addAll(Styles.TEXT_SMALL);
        enabledToggle = new ToggleButton();
        enabledToggle.setSelected(config.isEnabled());
        enabledRow.getChildren().addAll(enabledLabel, new Region(), enabledToggle);
        HBox.setHgrow(enabledRow.getChildren().get(1), Priority.ALWAYS);

        // API Key field (if required)
        VBox configFields = new VBox(12);
        if (config.isRequiresApiKey()) {
            VBox apiKeyBox = createFieldBox("API Key", config.getApiKey(), true);
            apiKeyField = (TextField) apiKeyBox.lookup(".text-field");
            configFields.getChildren().add(apiKeyBox);
        }

        // Base URL field
        VBox baseUrlBox = createFieldBox(
                "Base URL", config.getApiBaseUrl() != null ? config.getApiBaseUrl() : "", false);
        baseUrlField = (TextField) baseUrlBox.lookup(".text-field");
        configFields.getChildren().add(baseUrlBox);

        // Default Model field with load button
        HBox modelRow = new HBox(8);
        VBox modelBox = createModelFieldBox();
        modelComboBox = (ComboBox<String>) modelBox.lookup(".combo-box");
        if (config.getDefaultModel() != null) {
            modelComboBox.getItems().add(config.getDefaultModel());
            modelComboBox.setValue(config.getDefaultModel());
        }
        HBox.setHgrow(modelBox, Priority.ALWAYS);

        loadModelsButton = new Button();
        loadModelsButton.setGraphic(new FontIcon(Octicons.SYNC_16));
        loadModelsButton.setTooltip(new Tooltip("Load available models"));
        loadModelsButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        loadModelsButton.setOnAction(e -> handleLoadModels());

        modelRow.getChildren().addAll(modelBox, loadModelsButton);
        configFields.getChildren().add(modelRow);

        // Action buttons
        HBox actionButtons = createActionButtons();

        content.getChildren().addAll(enabledRow, configFields, actionButtons);
        return content;
    }

    private VBox createFieldBox(String labelText, String value, boolean isPassword) {
        VBox box = new VBox(4);

        Label label = new Label(labelText);
        label.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        TextField field;
        if (isPassword) {
            PasswordField passwordField = new PasswordField();
            passwordField.setText(value != null ? value : "");
            field = passwordField;
        } else {
            field = new TextField();
            field.setText(value != null ? value : "");
        }

        field.getStyleClass().add("text-field");
        field.setPromptText("Enter " + labelText.toLowerCase());

        box.getChildren().addAll(label, field);
        return box;
    }

    private VBox createModelFieldBox() {
        VBox box = new VBox(4);

        Label label = new Label("Default Model");
        label.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.getStyleClass().add("combo-box");
        comboBox.setPromptText("Select or enter model name");
        comboBox.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(label, comboBox);
        return box;
    }

    private HBox createActionButtons() {
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        testButton = new Button("Test Connection");
        testButton.setGraphic(new FontIcon(Octicons.PULSE_16));
        testButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        testButton.setOnAction(e -> handleTest());

        saveButton = new Button("Save");
        saveButton.setGraphic(new FontIcon(Octicons.CHECK_16));
        saveButton.getStyleClass().addAll(Styles.ACCENT);
        saveButton.setOnAction(e -> handleSave());

        buttons.getChildren().addAll(testButton, saveButton);
        return buttons;
    }

    private void updateStatusLabel() {
        if (config.getTestStatus() == null) {
            statusLabel.setText("Not tested");
            statusLabel.getStyleClass().removeAll("success", "danger", "warning");
            statusLabel.getStyleClass().add("text-muted");
        } else if ("success".equals(config.getTestStatus())) {
            statusLabel.setText("✓ Connected");
            statusLabel.getStyleClass().removeAll("danger", "warning", "text-muted");
            statusLabel.getStyleClass().add("success");
        } else {
            statusLabel.setText("✗ Failed");
            statusLabel.getStyleClass().removeAll("success", "warning", "text-muted");
            statusLabel.getStyleClass().add("danger");
        }
    }

    private void handleSave() {
        // Collect data from fields
        if (apiKeyField != null) {
            config.setApiKey(apiKeyField.getText());
        }
        config.setApiBaseUrl(baseUrlField.getText());
        config.setDefaultModel(modelComboBox.getValue());
        config.setEnabled(enabledToggle.isSelected());

        // Trigger save action
        actionHandler.accept(new ProviderAction(ProviderActionType.SAVE, config));
    }

    private void handleTest() {
        // Save first
        handleSave();

        // Then test
        actionHandler.accept(new ProviderAction(ProviderActionType.TEST, config));
    }

    private void handleLoadModels() {
        // Save first
        handleSave();

        // Then load models
        actionHandler.accept(new ProviderAction(ProviderActionType.LOAD_MODELS, config));
    }

    private void handleSetActive() {
        config.setActive(activeToggle.isSelected());
        actionHandler.accept(new ProviderAction(ProviderActionType.SET_ACTIVE, config));
    }
}

