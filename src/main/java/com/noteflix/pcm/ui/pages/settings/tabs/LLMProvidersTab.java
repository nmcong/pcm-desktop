package com.noteflix.pcm.ui.pages.settings.tabs;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.application.service.provider.ProviderConfigurationService;
import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import com.noteflix.pcm.ui.pages.settings.components.ProviderConfigCard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.List;

/**
 * LLM Providers Tab
 *
 * <p>Displays and manages configurations for all registered LLM providers:
 * - OpenAI
 * - Anthropic
 * - Ollama
 * - Custom APIs
 *
 * <p>Features:
 * - Configure API keys
 * - Select default models
 * - Test connections
 * - Set active provider
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LLMProvidersTab extends VBox {

    private final ProviderConfigurationService service;
    private VBox providersContainer;

    public LLMProvidersTab() {
        this.service = new ProviderConfigurationService();

        setSpacing(0);
        setPadding(new Insets(0));
        getStyleClass().add("llm-providers-tab");

        // Header
        VBox header = createHeader();

        // Providers container
        providersContainer = new VBox(16);
        providersContainer.setPadding(new Insets(20));
        providersContainer.getStyleClass().add("providers-container");

        ScrollPane scrollPane = new ScrollPane(providersContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("providers-scroll");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(header, scrollPane);

        // Load providers
        loadProviders();
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(20, 20, 16, 20));
        header.getStyleClass().add("tab-header");

        Label title = new Label("LLM Provider Configurations");
        title.getStyleClass().addAll(Styles.TITLE_3);

        Label subtitle = new Label(
                "Configure API keys, models, and connection settings for AI providers"
        );
        subtitle.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        subtitle.setWrapText(true);

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private void loadProviders() {
        try {
            List<ProviderConfiguration> configs = service.getAllConfigurations();

            if (configs.isEmpty()) {
                showEmptyState();
            } else {
                displayProviders(configs);
            }
        } catch (Exception e) {
            log.error("Failed to load provider configurations", e);
            showErrorState(e);
        }
    }

    private void displayProviders(List<ProviderConfiguration> configs) {
        providersContainer.getChildren().clear();

        for (ProviderConfiguration config : configs) {
            ProviderConfigCard card = new ProviderConfigCard(config, this::handleProviderAction);
            providersContainer.getChildren().add(card);
        }
    }

    private void showEmptyState() {
        providersContainer.getChildren().clear();

        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));

        FontIcon icon = new FontIcon(Octicons.INBOX_24);
        icon.setIconSize(48);
        icon.getStyleClass().add("text-muted");

        Label label = new Label("No provider configurations found");
        label.getStyleClass().addAll(Styles.TITLE_4, "text-muted");

        emptyState.getChildren().addAll(icon, label);
        providersContainer.getChildren().add(emptyState);
    }

    private void showErrorState(Exception e) {
        providersContainer.getChildren().clear();

        VBox errorState = new VBox(16);
        errorState.setAlignment(Pos.CENTER);
        errorState.setPadding(new Insets(60));

        FontIcon icon = new FontIcon(Octicons.ALERT_24);
        icon.setIconSize(48);
        icon.getStyleClass().add("text-danger");

        Label label = new Label("Failed to load providers");
        label.getStyleClass().addAll(Styles.TITLE_4, "text-danger");

        Label errorMessage = new Label(e.getMessage());
        errorMessage.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        errorState.getChildren().addAll(icon, label, errorMessage);
        providersContainer.getChildren().add(errorState);
    }

    private void handleProviderAction(ProviderAction action) {
        switch (action.type()) {
            case SAVE -> handleSave(action.configuration());
            case TEST -> handleTest(action.configuration());
            case LOAD_MODELS -> handleLoadModels(action.configuration());
            case SET_ACTIVE -> handleSetActive(action.configuration());
        }
    }

    private void handleSave(ProviderConfiguration config) {
        try {
            service.saveConfiguration(config);
            log.info("Saved provider configuration: {}", config.getProviderName());
            // Refresh to show updated data
            refresh();
        } catch (Exception e) {
            log.error("Failed to save provider configuration", e);
            // TODO: Show error dialog
        }
    }

    private void handleTest(ProviderConfiguration config) {
        log.info("Testing connection for: {}", config.getProviderName());
        service.testConnection(config.getProviderName()).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    log.info("Connection test successful for: {}", config.getProviderName());
                    refresh();
                } else {
                    log.warn("Connection test failed for: {}", config.getProviderName());
                    refresh();
                }
            });
        });
    }

    private void handleLoadModels(ProviderConfiguration config) {
        log.info("Loading models for: {}", config.getProviderName());
        service.loadModels(config.getProviderName()).thenAccept(models -> {
            Platform.runLater(() -> {
                log.info("Loaded {} models for: {}", models.size(), config.getProviderName());
                // TODO: Show models dialog
            });
        });
    }

    private void handleSetActive(ProviderConfiguration config) {
        try {
            service.setActiveProvider(config.getProviderName());
            log.info("Set active provider: {}", config.getProviderName());
            refresh();
        } catch (Exception e) {
            log.error("Failed to set active provider", e);
        }
    }

    /**
     * Refresh the tab content
     */
    public void refresh() {
        loadProviders();
    }

    /**
     * Provider action enum
     */
    public enum ProviderActionType {
        SAVE, TEST, LOAD_MODELS, SET_ACTIVE
    }

    /**
     * Provider action data class
     */
    public record ProviderAction(ProviderActionType type, ProviderConfiguration configuration) {}
}

