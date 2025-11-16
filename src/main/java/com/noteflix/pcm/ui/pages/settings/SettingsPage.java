package com.noteflix.pcm.ui.pages.settings;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.pages.settings.tabs.DatabaseManagementTab;
import com.noteflix.pcm.ui.pages.settings.tabs.LLMProvidersTab;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * Settings Page with tabbed interface
 *
 * <p>Provides configuration UI for:
 * - LLM Providers (OpenAI, Anthropic, Ollama, etc.)
 * - Application Preferences
 * - Database Settings
 * - System Settings
 *
 * <p>Uses AtlantaFX TabPane for modern, styled tabs
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public class SettingsPage extends BaseView {

    private TabPane tabPane;
    private LLMProvidersTab llmProvidersTab;
    private DatabaseManagementTab databaseTab;

    public SettingsPage() {
        super(
                "Settings",
                "Configure application settings and LLM providers",
                new FontIcon(Octicons.GEAR_24)
        );
        log.info("SettingsPage initialized with tabs");
    }

    @Override
    protected Node createMainContent() {
        VBox content = new VBox();
        content.setPadding(new Insets(0));
        content.getStyleClass().add("settings-page");

        // Create TabPane
        tabPane = new TabPane();
        tabPane.setSide(Side.TOP);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().addAll(Styles.DENSE);

        // Create tabs
        Tab llmTab = createLLMProvidersTab();
        Tab dbTab = createDatabaseManagementTab();
        Tab preferencesTab = createPlaceholderTab("Preferences", Octicons.TOOLS_24);
        Tab systemTab = createPlaceholderTab("System", Octicons.SERVER_24);

        tabPane.getTabs().addAll(llmTab, dbTab, preferencesTab, systemTab);

        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().add(tabPane);

        return content;
    }

    private Tab createLLMProvidersTab() {
        Tab tab = new Tab("LLM Providers");
        tab.setGraphic(new FontIcon(Octicons.COMMENT_DISCUSSION_24));

        llmProvidersTab = new LLMProvidersTab();
        tab.setContent(llmProvidersTab);

        return tab;
    }

    private Tab createDatabaseManagementTab() {
        Tab tab = new Tab("Database");
        tab.setGraphic(new FontIcon(Octicons.DATABASE_24));

        databaseTab = new DatabaseManagementTab();
        tab.setContent(databaseTab);

        return tab;
    }

    private Tab createPlaceholderTab(String title, Octicons icon) {
        Tab tab = new Tab(title);
        tab.setGraphic(new FontIcon(icon));

        VBox placeholder = new VBox(20);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setPadding(new Insets(40));

        FontIcon placeholderIcon = new FontIcon(icon);
        placeholderIcon.setIconSize(48);
        placeholderIcon.getStyleClass().add("text-muted");

        javafx.scene.control.Label label =
                new javafx.scene.control.Label(title + " settings coming soon...");
        label.getStyleClass().addAll(Styles.TITLE_3, "text-muted");

        placeholder.getChildren().addAll(placeholderIcon, label);
        tab.setContent(placeholder);

        return tab;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        log.debug("SettingsPage activated");

        // Refresh LLM providers tab if it exists
        if (llmProvidersTab != null) {
            llmProvidersTab.refresh();
        }
    }
}

