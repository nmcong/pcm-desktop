package com.noteflix.pcm.ui.components.sidebar;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.pages.BatchJobsPage;
import com.noteflix.pcm.ui.pages.DatabaseObjectsPage;
import com.noteflix.pcm.ui.pages.KnowledgeBasePage;
import com.noteflix.pcm.ui.pages.settings.SettingsPage;
import com.noteflix.pcm.ui.pages.ai.AIAssistantPage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Main navigation menu component for the sidebar
 * Handles menu item creation and highlighting
 */
@Slf4j
public class MainMenu extends VBox {

    private final Map<Class<? extends BaseView>, Button> menuButtons = new HashMap<>();
    private final Consumer<Runnable> navigationHandler;
    // Menu item handlers
    private final Runnable onAIAssistant;
    private final Runnable onKnowledgeBase;
    private final Runnable onBatchJobs;
    private final Runnable onDBObjects;
    private final Runnable onSettings;
    private Button activeMenuButton = null;

    public MainMenu(Consumer<Runnable> navigationHandler,
                    Runnable onAIAssistant,
                    Runnable onKnowledgeBase,
                    Runnable onBatchJobs,
                    Runnable onDBObjects,
                    Runnable onSettings) {
        super(4);

        this.navigationHandler = navigationHandler;
        this.onAIAssistant = onAIAssistant;
        this.onKnowledgeBase = onKnowledgeBase;
        this.onBatchJobs = onBatchJobs;
        this.onDBObjects = onDBObjects;
        this.onSettings = onSettings;

        buildMenu();
    }

    private void buildMenu() {
        getStyleClass().add("card");
        setPadding(new Insets(8));

        // Create menu items
        Button aiAssistantBtn = createAIAssistantMenuItem();
        Button knowledgeBaseBtn = createMenuItem("Knowledge Base", Octicons.BOOK_24, onKnowledgeBase);
        Button batchJobsBtn = createMenuItem("Batch Jobs", Octicons.CLOCK_24, onBatchJobs);
        Button dbObjectsBtn = createMenuItem("DB Objects", Octicons.DATABASE_24, onDBObjects);
        Button settingsBtn = createMenuItem("Settings", Octicons.TOOLS_24, onSettings);

        // Store button references for highlighting
        menuButtons.put(AIAssistantPage.class, aiAssistantBtn);
        menuButtons.put(KnowledgeBasePage.class, knowledgeBaseBtn);
        menuButtons.put(BatchJobsPage.class, batchJobsBtn);
        menuButtons.put(DatabaseObjectsPage.class, dbObjectsBtn);
        menuButtons.put(SettingsPage.class, settingsBtn);

        getChildren().addAll(
                aiAssistantBtn,
                knowledgeBaseBtn,
                batchJobsBtn,
                dbObjectsBtn,
                settingsBtn);
    }

    private Button createAIAssistantMenuItem() {
        FontIcon dependabotIcon = new FontIcon(Octicons.DEPENDABOT_24);
        Label label = new Label("AI Assistant");

        HBox content = new HBox(12, dependabotIcon, label);
        content.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().addAll(Styles.FLAT, Styles.LEFT_PILL, "sidebar-menu-item", "ai-assistant-btn");
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> navigationHandler.accept(onAIAssistant));

        return button;
    }

    private Button createMenuItem(String text, Octicons icon, Runnable action) {
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);

        Label label = new Label(text);

        HBox content = new HBox(12, iconNode, label);
        content.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().addAll(Styles.FLAT, Styles.LEFT_PILL, "sidebar-menu-item");
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> navigationHandler.accept(action));

        return button;
    }

    /**
     * Updates the active menu item highlighting
     *
     * @param currentPageClass The class of the currently active page
     */
    public void updateActiveMenuItem(Class<? extends BaseView> currentPageClass) {
        // Remove active state from previous button
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("active");
        }

        // Add active state to current button
        Button currentButton = menuButtons.get(currentPageClass);
        if (currentButton != null) {
            currentButton.getStyleClass().add("active");
            activeMenuButton = currentButton;
            log.debug("Highlighted menu item for page: {}", currentPageClass.getSimpleName());
        } else {
            activeMenuButton = null;
            log.debug("No menu item found for page: {}", currentPageClass != null ? currentPageClass.getSimpleName() : "null");
        }
    }

    /**
     * Clear menu highlighting
     */
    public void clearHighlighting() {
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("active");
            activeMenuButton = null;
        }
    }
}