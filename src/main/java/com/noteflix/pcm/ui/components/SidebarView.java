package com.noteflix.pcm.ui.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Sidebar component built with pure Java (no FXML)
 * Following AtlantaFX Sampler patterns
 */
@Slf4j
public class SidebarView extends VBox {
    
    public SidebarView() {
        super(16);
        
        getStyleClass().add("sidebar");
        setPadding(new Insets(16, 12, 16, 12));
        setPrefWidth(280);
        setMinWidth(280);
        setMaxWidth(280);
        
        // Build sidebar components
        getChildren().addAll(
            createHeader(),
            createMainMenu(),
            createFavoritesSection(),
            createProjectsSection(),
            createSettingsButton()
        );
    }
    
    /**
     * Creates the header with app title and theme switch (AtlantaFX Sampler pattern)
     */
    private VBox createHeader() {
        VBox headerSection = new VBox(20);
        headerSection.getStyleClass().add("header");
        
        // Logo section
        FontIcon appIcon = new FontIcon(Feather.LAYERS);
        appIcon.setIconSize(32);
        appIcon.setStyle("-fx-icon-color: -color-accent-emphasis;");
        
        Label titleLabel = new Label("PCM Desktop");
        titleLabel.getStyleClass().add(Styles.TITLE_3);
        
        Button themeButton = new Button();
        themeButton.setGraphic(new FontIcon(Feather.SUN));
        themeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SMALL);
        themeButton.setTooltip(new Tooltip("Switch Theme"));
        themeButton.setOnAction(e -> handleThemeSwitch());
        
        HBox logoSection = new HBox(10, appIcon, titleLabel, createSpacer(), themeButton);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        logoSection.getStyleClass().add("logo");
        
        headerSection.getChildren().addAll(logoSection, createSearchButton());
        return headerSection;
    }
    
    /**
     * Creates search button following AtlantaFX Sampler pattern
     */
    private Button createSearchButton() {
        // Search label with icon
        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(14);
        
        Label searchLabel = new Label("Search");
        searchLabel.setGraphic(searchIcon);
        searchLabel.getStyleClass().add("search-label");
        searchLabel.setGraphicTextGap(8);
        
        // Keyboard hint
        Label hintLabel = new Label("Press /");
        hintLabel.getStyleClass().addAll("hint", "text-muted", "text-small");
        
        // Content container
        HBox content = new HBox(12, searchLabel, createSpacer(), hintLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.getStyleClass().add("content");
        
        // Search button
        Button searchButton = new Button();
        searchButton.setGraphic(content);
        searchButton.getStyleClass().add("search-button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setAlignment(Pos.CENTER_LEFT);
        searchButton.setOnAction(e -> openSearchDialog());
        
        return searchButton;
    }
    
    /**
     * Creates main menu with icon buttons
     */
    private VBox createMainMenu() {
        VBox menu = new VBox(4);
        menu.getStyleClass().add("card");
        menu.setPadding(new Insets(8));
        
        menu.getChildren().addAll(
            createMenuItem("Knowledge Base", Feather.BOOK_OPEN, this::handleKnowledgeBase),
            createMenuItem("Batch Jobs", Feather.CLOCK, this::handleBatchJobs),
            createMenuItem("DB Objects", Feather.DATABASE, this::handleDBObjects),
            createMenuItem("Settings", Feather.SLIDERS, this::handleSettingsMenu)
        );
        
        return menu;
    }
    
    /**
     * Creates a menu item button
     */
    private Button createMenuItem(String text, Feather icon, Runnable action) {
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);
        
        Label label = new Label(text);
        
        HBox content = new HBox(12, iconNode, label);
        content.setAlignment(Pos.CENTER_LEFT);
        
        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().addAll(Styles.FLAT, Styles.LEFT_PILL);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> action.run());
        
        return button;
    }
    
    /**
     * Creates favorites section with header
     */
    private VBox createFavoritesSection() {
        VBox section = new VBox(8);
        
        // Section header
        HBox sectionHeader = createSectionHeader("ƯU THÍCH", Feather.STAR, null);
        
        // Favorites cards
        VBox favoritesCard = new VBox(4);
        favoritesCard.getStyleClass().add("card");
        favoritesCard.setPadding(new Insets(8));
        
        favoritesCard.getChildren().addAll(
            createProjectItem("CS", "Customer Service", "24 screens • Active", "-color-accent-emphasis"),
            createProjectItem("OM", "Order Management", "18 screens • Active", "-color-success-emphasis")
        );
        
        section.getChildren().addAll(sectionHeader, favoritesCard);
        return section;
    }
    
    /**
     * Creates projects section with scrollable list
     */
    private VBox createProjectsSection() {
        VBox section = new VBox(8);
        VBox.setVgrow(section, Priority.ALWAYS);
        
        // Section header with add button
        Button addButton = new Button();
        addButton.setGraphic(new FontIcon(Feather.PLUS));
        addButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SMALL);
        addButton.setTooltip(new Tooltip("New Project"));
        addButton.setOnAction(e -> handleNewProject());
        
        HBox sectionHeader = createSectionHeader("DỰ ÁN", Feather.FOLDER, addButton);
        
        // Projects list in scrollpane
        VBox projectsList = new VBox(4);
        projectsList.getStyleClass().add("card");
        projectsList.setPadding(new Insets(8));
        
        projectsList.getChildren().addAll(
            createProjectItem("CS", "Customer Service", "24 screens", "-color-accent-emphasis"),
            createProjectItem("OM", "Order Management", "18 screens", "-color-success-emphasis"),
            createProjectItem("PG", "Payment Gateway", "12 screens", "-color-warning-emphasis"),
            createProjectItem("IA", "Inventory Admin", "15 screens", "-color-accent-emphasis"),
            createProjectItem("RP", "Reports Portal", "8 screens", "-color-danger-emphasis")
        );
        
        ScrollPane scrollPane = new ScrollPane(projectsList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add(Styles.DENSE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionHeader, scrollPane);
        return section;
    }
    
    /**
     * Creates section header with icon and optional action button
     */
    private HBox createSectionHeader(String title, Feather icon, Button actionButton) {
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);
        
        HBox header = new HBox(8, iconNode, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        
        if (actionButton != null) {
            Region spacer = createSpacer();
            header.getChildren().addAll(spacer, actionButton);
        }
        
        return header;
    }
    
    /**
     * Creates a project item with avatar and details
     */
    private HBox createProjectItem(String initials, String name, String details, String colorVar) {
        // Avatar with initials
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        StackPane avatar = new StackPane(initialsLabel);
        avatar.setStyle("-fx-background-color: " + colorVar + "; -fx-background-radius: 6px;");
        avatar.setMinSize(32, 32);
        avatar.setMaxSize(32, 32);
        
        // Project details
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);
        
        Label detailsLabel = new Label(details);
        detailsLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        
        VBox textBox = new VBox(2, nameLabel, detailsLabel);
        
        // Project item container
        HBox projectItem = new HBox(12, avatar, textBox);
        projectItem.setAlignment(Pos.CENTER_LEFT);
        projectItem.getStyleClass().add("list-item");
        projectItem.setPadding(new Insets(8));
        projectItem.setOnMouseClicked(e -> handleProjectClick(name));
        
        return projectItem;
    }
    
    /**
     * Creates settings button at bottom
     */
    private Button createSettingsButton() {
        FontIcon icon = new FontIcon(Feather.SETTINGS);
        icon.setIconSize(16);
        
        Label label = new Label("Settings");
        
        HBox content = new HBox(8, icon, label);
        content.setAlignment(Pos.CENTER_LEFT);
        
        Button settingsButton = new Button();
        settingsButton.setGraphic(content);
        settingsButton.setMaxWidth(Double.MAX_VALUE);
        settingsButton.getStyleClass().addAll(Styles.FLAT, Styles.LEFT_PILL);
        settingsButton.setAlignment(Pos.CENTER_LEFT);
        settingsButton.setOnAction(e -> handleSettings());
        
        return settingsButton;
    }
    
    /**
     * Creates a horizontal spacer
     */
    private Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    // Event handlers
    
    private void openSearchDialog() {
        log.info("Opening Search Dialog");
        showInfo("Search", 
            "Search across your projects:\n\n" +
            "• Find projects by name\n" +
            "• Search in descriptions\n" +
            "• Filter by status\n" +
            "• Quick navigation\n\n" +
            "Tip: Use keyboard shortcut '/' to open search quickly");
    }
    
    private void handleThemeSwitch() {
        log.info("Switching Theme");
        showInfo("Theme Switch", 
            "Theme customization:\n\n" +
            "• Light mode\n" +
            "• Dark mode\n" +
            "• Auto (system)\n" +
            "• Custom themes\n\n" +
            "Current theme will be applied globally");
    }
    
    private void handleKnowledgeBase() {
        log.info("Opening Knowledge Base");
        showInfo("Knowledge Base", 
            "Browse and search your knowledge base:\n\n" +
            "• Documentation\n" +
            "• Best practices\n" +
            "• Design patterns\n" +
            "• Technical notes");
    }
    
    private void handleBatchJobs() {
        log.info("Opening Batch Jobs");
        showInfo("Batch Jobs", 
            "Manage scheduled and batch operations:\n\n" +
            "• View running jobs\n" +
            "• Schedule new tasks\n" +
            "• Job history\n" +
            "• Execution logs");
    }
    
    private void handleDBObjects() {
        log.info("Opening DB Objects");
        showInfo("Database Objects", 
            "Database schema and objects:\n\n" +
            "• Tables\n" +
            "• Views\n" +
            "• Stored procedures\n" +
            "• Triggers & Functions");
    }
    
    private void handleSettingsMenu() {
        log.info("Opening Settings from menu");
        showInfo("Settings", 
            "Application configuration:\n\n" +
            "• User preferences\n" +
            "• Project settings\n" +
            "• Database connections\n" +
            "• Theme & appearance");
    }
    
    private void handleNewProject() {
        log.info("Creating new project");
        showInfo("New Project", "Create a new project");
    }
    
    private void handleProjectClick(String projectName) {
        log.info("Opening project: {}", projectName);
        showInfo("Project", "View details for: " + projectName);
    }
    
    private void handleSettings() {
        log.info("Opening Settings");
        showInfo("Settings", 
            "PCM Desktop Settings\n\n" +
            "Configure your preferences:\n" +
            "• User preferences\n" +
            "• Project configuration\n" +
            "• Database connections\n" +
            "• Theme customization");
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

