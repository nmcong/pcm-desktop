package com.noteflix.pcm.ui.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.pages.ai.AIAssistantPage;
import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.events.ThemeChangeListener;
import com.noteflix.pcm.core.navigation.PageNavigator;
import com.noteflix.pcm.core.navigation.NavigationListener;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.ui.pages.*;
import com.noteflix.pcm.ui.pages.projects.*;
import com.noteflix.pcm.ui.base.BaseView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.HashMap;
import java.util.Map;

/** Sidebar component built with pure Java (no FXML) Following AtlantaFX Sampler patterns */
@Slf4j
public class SidebarView extends VBox implements ThemeChangeListener, NavigationListener {

  private final ThemeManager themeManager;
    /**
     * -- SETTER --
     *  Sets the page navigator for navigation functionality Dependency Injection - follows Dependency
     *  Inversion Principle
     */
    @Setter
    private PageNavigator pageNavigator;
  private FontIcon appIcon;
  
  // Map to store menu buttons for highlighting
  private final Map<Class<? extends BaseView>, Button> menuButtons = new HashMap<>();
  // Map to store project items for highlighting
  private final Map<String, HBox> projectItems = new HashMap<>();
  private Button activeMenuButton = null;
  private HBox activeProjectItem = null;

  public SidebarView() {
    super(16);

    this.themeManager = ThemeManager.getInstance();

    getStyleClass().add("sidebar");
    setPadding(new Insets(16, 12, 16, 12));
    setPrefWidth(AppConstants.SIDEBAR_WIDTH);
    setMinWidth(AppConstants.SIDEBAR_WIDTH);
    setMaxWidth(AppConstants.SIDEBAR_WIDTH);

    // Register for theme changes
    themeManager.addThemeChangeListener(this);

    // Build sidebar components
    getChildren()
        .addAll(
            createHeader(), createMainMenu(), createFavoritesSection(), createProjectsSection());
  }

    /** Creates the header with app title and theme switch (AtlantaFX Sampler pattern) */
  private VBox createHeader() {
    VBox headerSection = new VBox(20);
    headerSection.getStyleClass().add("header");

    // Logo section with DEPENDABOT icon
    appIcon = new FontIcon(Octicons.CPU_16);
    appIcon.getStyleClass().add("app-icon");

    Label titleLabel = new Label("PCM");
    titleLabel.getStyleClass().add(Styles.TITLE_3);

    HBox logoSection = new HBox(10, appIcon, titleLabel);
    logoSection.setAlignment(Pos.CENTER_LEFT);
    logoSection.getStyleClass().add("logo");

    headerSection.getChildren().addAll(logoSection, createSearchButton());
    return headerSection;
  }

  /** Creates search button following AtlantaFX Sampler pattern */
  private Button createSearchButton() {
    // Search label with icon
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

  /** Creates main menu with icon buttons */
  private VBox createMainMenu() {
    VBox menu = new VBox(4);
    menu.getStyleClass().add("card");
    menu.setPadding(new Insets(8));

    // Create menu items and store references for highlighting
    Button aiAssistantBtn = createAIAssistantMenuItem();
    Button knowledgeBaseBtn = createMenuItem("Knowledge Base", Octicons.BOOK_24, this::handleKnowledgeBase);
    Button textComponentBtn = createMenuItem("Text Component", Octicons.FILE_CODE_24, this::handleTextComponent);
    Button batchJobsBtn = createMenuItem("Batch Jobs", Octicons.CLOCK_24, this::handleBatchJobs);
    Button dbObjectsBtn = createMenuItem("DB Objects", Octicons.DATABASE_24, this::handleDBObjects);
    Button settingsBtn = createMenuItem("Settings", Octicons.TOOLS_24, this::handleSettingsMenu);
    
    // Store button references for highlighting
    menuButtons.put(AIAssistantPage.class, aiAssistantBtn);
    menuButtons.put(KnowledgeBasePage.class, knowledgeBaseBtn);
    menuButtons.put(UniversalTextDemoPage.class, textComponentBtn);
    menuButtons.put(BatchJobsPage.class, batchJobsBtn);
    menuButtons.put(DatabaseObjectsPage.class, dbObjectsBtn);
    menuButtons.put(SettingsPage.class, settingsBtn);

    menu.getChildren().addAll(
        aiAssistantBtn,
        knowledgeBaseBtn, 
        textComponentBtn,
        batchJobsBtn,
        dbObjectsBtn,
        settingsBtn);

    return menu;
  }

  /** Creates AI Assistant menu item with DEPENDABOT icon */
  private Button createAIAssistantMenuItem() {
    // DEPENDABOT icon
    FontIcon dependabotIcon = new FontIcon(Octicons.DEPENDABOT_24);

    Label label = new Label("AI Assistant");

    HBox content = new HBox(12, dependabotIcon, label);
    content.setAlignment(Pos.CENTER_LEFT);

    Button button = new Button();
    button.setGraphic(content);
    button.setMaxWidth(Double.MAX_VALUE);
    button
        .getStyleClass()
        .addAll(Styles.FLAT, Styles.LEFT_PILL, "sidebar-menu-item", "ai-assistant-btn");
    button.setAlignment(Pos.CENTER_LEFT);
    button.setOnAction(e -> handleAIAssistant());

    return button;
  }

  /** Creates a menu item button */
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
    button.setOnAction(e -> action.run());

    return button;
  }

  /** Creates favorites section with header */
  private VBox createFavoritesSection() {
    VBox section = new VBox(8);

    // Section header
    HBox sectionHeader = createSectionHeader("FAVORITES", Octicons.STAR_24, null);

    // Favorites cards
    VBox favoritesCard = new VBox(4);
    favoritesCard.getStyleClass().add("card");
    favoritesCard.setPadding(new Insets(8));

    favoritesCard
        .getChildren()
        .addAll(
            createProjectItem(
                "CS", "Customer Service", "24 screens • Active", "-color-accent-emphasis"),
            createProjectItem(
                "OM", "Order Management", "18 screens • Active", "-color-success-emphasis"));

    section.getChildren().addAll(sectionHeader, favoritesCard);
    return section;
  }

  /** Creates projects section with scrollable list */
  private VBox createProjectsSection() {
    VBox section = new VBox(8);
    VBox.setVgrow(section, Priority.ALWAYS);

    // Section header with add button
    Button addButton = new Button();
    addButton.setGraphic(new FontIcon(Octicons.PLUS_24));
    addButton
        .getStyleClass()
        .addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
    addButton.setTooltip(new Tooltip("New Project"));
    addButton.setOnAction(e -> handleNewProject());

    HBox sectionHeader = createSectionHeader("PROJECTS", Octicons.REPO_24, addButton);

    // Projects list in scrollpane
    VBox projectsList = new VBox(4);
    projectsList.getStyleClass().add("card");
    projectsList.setPadding(new Insets(8));

    projectsList
        .getChildren()
        .addAll(
            createProjectItem("CS", "Customer Service", "24 screens", "-color-accent-emphasis"),
            createProjectItem("OM", "Order Management", "18 screens", "-color-success-emphasis"),
            createProjectItem("PG", "Payment Gateway", "12 screens", "-color-warning-emphasis"),
            createProjectItem("IA", "Inventory Admin", "15 screens", "-color-accent-emphasis"),
            createProjectItem("RP", "Reports Portal", "8 screens", "-color-danger-emphasis"));

    ScrollPane scrollPane = new ScrollPane(projectsList);
    scrollPane.setFitToWidth(true);
    scrollPane.getStyleClass().add(Styles.DENSE);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    section.getChildren().addAll(sectionHeader, scrollPane);
    return section;
  }

  /** Creates section header with icon and optional action button */
  private HBox createSectionHeader(String title, Octicons icon, Button actionButton) {
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

  /** Creates a project item with avatar and details */
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
    
    // Store project item for highlighting
    projectItems.put(name, projectItem);

    return projectItem;
  }

  /** Creates a horizontal spacer */
  private Region createSpacer() {
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    return spacer;
  }

  // Event handlers

  private void openSearchDialog() {
    log.info("Opening Search Dialog");
    showInfo(
        "Search",
        "Search across your projects:\n\n"
            + "• Find projects by name\n"
            + "• Search in descriptions\n"
            + "• Filter by status\n"
            + "• Quick navigation\n\n"
            + "Tip: Use keyboard shortcut '/' to open search quickly");
  }

  @Override
  public void onThemeChanged(boolean isDarkTheme) {
    log.debug("Theme changed to: {}", isDarkTheme ? "dark" : "light");

    // App icon doesn't need theme update (using FontIcon instead of ImageView)
    // FontIcon automatically adapts to theme
    // DEPENDABOT icon also doesn't need theme update

    // Theme button removed - now handled in MainView navbar
  }
  
  @Override
  public void onNavigationChanged(BaseView previousPage, BaseView currentPage) {
    if (currentPage != null) {
      updateActiveMenuItem(currentPage.getClass());
      
      // Handle project page highlighting
      if (currentPage instanceof BaseProjectPage) {
        BaseProjectPage projectPage = (BaseProjectPage) currentPage;
        String projectCode = projectPage.getProjectCode();
        String projectName = getProjectNameFromCode(projectCode);
        updateActiveProjectItem(projectName);
      } else {
        // Clear project highlighting if not on a project page
        updateActiveProjectItem(null);
      }
    }
  }

  private void handleAIAssistant() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(AIAssistantPage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "AI Assistant",
          "AI-Powered System Analysis Assistant:\n\n"
              + "• Natural language queries\n"
              + "• Code analysis and suggestions\n"
              + "• Database insights\n"
              + "• Workflow optimization\n"
              + "• Business process analysis\n\n"
              + "Ask me anything about your system!");
    }
  }

  private void handleKnowledgeBase() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(KnowledgeBasePage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "Knowledge Base",
          "Browse and search your knowledge base:\n\n"
              + "• Documentation\n"
              + "• Best practices\n"
              + "• Design patterns\n"
              + "• Technical notes");
    }
  }

  private void handleTextComponent() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(UniversalTextDemoPage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "Universal Text Component",
          "Demo of Universal Text Component:\n\n"
              + "• Markdown rendering\n"
              + "• Syntax highlighting\n"
              + "• Multiple view modes\n"
              + "• Live preview\n"
              + "• Theme support");
    }
  }

  private void handleBatchJobs() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(BatchJobsPage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "Batch Jobs",
          "Manage scheduled and batch operations:\n\n"
              + "• View running jobs\n"
              + "• Schedule new tasks\n"
              + "• Job history\n"
              + "• Execution logs");
    }
  }

  private void handleDBObjects() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(DatabaseObjectsPage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "Database Objects",
          "Database schema and objects:\n\n"
              + "• Tables\n"
              + "• Views\n"
              + "• Stored procedures\n"
              + "• Triggers & Functions");
    }
  }

  private void handleSettingsMenu() {
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(SettingsPage.class);
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo(
          "Settings",
          "Application configuration:\n\n"
              + "• User preferences\n"
              + "• Project settings\n"
              + "• Database connections\n"
              + "• Theme & appearance");
    }
  }

  private void handleNewProject() {
    log.info("Creating new project");
    showInfo("New Project", "Create a new project");
  }

  private void handleProjectClick(String projectName) {
    log.info("Opening project: {}", projectName);
    
    if (pageNavigator != null) {
      String projectCode = getProjectCodeFromName(projectName);
      if (projectCode != null) {
        // Navigate to BaseProjectPage with the project code
        BaseProjectPage projectPage = new BaseProjectPage(projectCode);
        pageNavigator.navigateToPage(projectPage);
      } else {
        showInfo("Project", "Project code not found for: " + projectName);
      }
    } else {
      log.warn("PageNavigator not set - showing fallback dialog");
      showInfo("Project", "View details for: " + projectName);
    }
  }
  
  private Class<? extends BaseView> getProjectPageClass(String projectName) {
    // All projects now use BaseProjectPage with different project codes
    return switch (projectName) {
      case "Customer Service", "Order Management", "Payment Gateway", "Inventory Admin", "Reports Portal" -> BaseProjectPage.class;
      default -> null;
    };
  }

  private void showInfo(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Updates the active menu item highlighting based on the current page
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
   * Updates the active project item highlighting
   * @param projectName The name of the project to highlight (null to clear)
   */
  private void updateActiveProjectItem(String projectName) {
    // Remove active state from previous project item
    if (activeProjectItem != null) {
      activeProjectItem.getStyleClass().remove("active");
    }
    
    // Add active state to current project item
    if (projectName != null) {
      HBox currentProjectItem = projectItems.get(projectName);
      if (currentProjectItem != null) {
        currentProjectItem.getStyleClass().add("active");
        activeProjectItem = currentProjectItem;
        log.debug("Highlighted project item: {}", projectName);
      } else {
        activeProjectItem = null;
        log.debug("No project item found for: {}", projectName);
      }
    } else {
      activeProjectItem = null;
    }
  }
  
  /**
   * Get project name from project code
   * @param projectCode The project code (e.g., "CS", "OM")
   * @return The full project name
   */
  private String getProjectNameFromCode(String projectCode) {
    return switch (projectCode) {
      case "CS" -> "Customer Service";
      case "OM" -> "Order Management";
      case "PG" -> "Payment Gateway";
      case "IA" -> "Inventory Admin";
      case "RP" -> "Reports Portal";
      default -> null;
    };
  }
  
  /**
   * Cleanup method to unregister listeners Should be called when the component is no longer needed
   */
  public void cleanup() {
    if (themeManager != null) {
      themeManager.removeThemeChangeListener(this);
    }
  }
  
  private String getProjectCodeFromName(String projectName) {
    return switch (projectName) {
      case "Customer Service" -> "CS";
      case "Order Management" -> "OM";
      case "Payment Gateway" -> "PG";
      case "Inventory Admin" -> "IA";
      case "Reports Portal" -> "RP";
      default -> null;
    };
  }
}
