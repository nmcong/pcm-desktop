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
import com.noteflix.pcm.application.service.project.IProjectService;
import com.noteflix.pcm.application.service.project.ProjectServiceFactory;
import com.noteflix.pcm.domain.entity.Project;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

/** Sidebar component built with pure Java (no FXML) Following AtlantaFX Sampler patterns */
@Slf4j
public class SidebarView extends VBox implements ThemeChangeListener, NavigationListener {

  private final ThemeManager themeManager;
  private final IProjectService projectService;
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
  private String activeProjectName = null; // Track current active project name
  
  // Navigation cancellation support
  private CompletableFuture<?> currentNavigationTask = null;
  
  // Loading state
  private boolean isLoading = false;
  private CompletableFuture<Void> loadingTask = null;

  public SidebarView() {
    super(16);

    this.themeManager = ThemeManager.getInstance();
    this.projectService = ProjectServiceFactory.getInstance();

    getStyleClass().add("sidebar");
    setPadding(new Insets(16, 12, 16, 12));
    setPrefWidth(AppConstants.SIDEBAR_WIDTH);
    setMinWidth(AppConstants.SIDEBAR_WIDTH);
    setMaxWidth(AppConstants.SIDEBAR_WIDTH);

    // Register for theme changes
    themeManager.addThemeChangeListener(this);

    // Build initial sidebar with loading state
    buildSidebarWithLoading();
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

  /** Creates favorites section with header (only if there are favorites) */
  private VBox createFavoritesSection() {
    // Get favorite projects from service
    List<Project> favoriteProjects = projectService.getFavoriteProjects();
    
    // Return null if no favorites (will be filtered out)
    if (favoriteProjects.isEmpty()) {
      log.debug("No favorite projects found, hiding favorites section");
      return null;
    }
    
    VBox section = new VBox(8);

    // Section header with refresh button
    Button refreshButton = new Button();
    refreshButton.setGraphic(new FontIcon(Octicons.SYNC_24));
    refreshButton
        .getStyleClass()
        .addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
    refreshButton.setTooltip(new Tooltip("Refresh Projects"));
    refreshButton.setOnAction(e -> handleRefreshProjects());

    HBox sectionHeader = createSectionHeader("FAVORITES", Octicons.STAR_24, refreshButton);

    // Favorites cards
    VBox favoritesCard = new VBox(4);
    favoritesCard.getStyleClass().add("card");
    favoritesCard.setPadding(new Insets(8));

    // Add favorite projects dynamically from service
    for (Project project : favoriteProjects) {
      String details = project.getScreenCount() + " screens • " + project.getStatus().getDisplayName();
      favoritesCard.getChildren().add(
        createProjectItem(project.getCode(), project.getName(), details, project.getColor())
      );
    }

    section.getChildren().addAll(sectionHeader, favoritesCard);
    return section;
  }

  /** Creates projects section with scrollable list (only if there are non-favorite projects) */
  private VBox createProjectsSection() {
    // Get non-favorite projects from service
    List<Project> nonFavoriteProjects = projectService.getNonFavoriteProjects();
    
    // Return null if no non-favorite projects (will be filtered out)
    if (nonFavoriteProjects.isEmpty()) {
      log.debug("No non-favorite projects found, hiding projects section");
      return null;
    }
    
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

    // Projects list in scrollpane - only show non-favorite projects
    VBox projectsList = new VBox(4);
    projectsList.getStyleClass().add("card");
    projectsList.setPadding(new Insets(8));

    // Add only non-favorite projects from service
    for (Project project : nonFavoriteProjects) {
      String details = project.getScreenCount() + " screens";
      projectsList.getChildren().add(
        createProjectItem(project.getCode(), project.getName(), details, project.getColor())
      );
    }

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
    projectItem.setOnMouseClicked(e -> {
      // Only handle left-click for navigation
      if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
        handleProjectClick(name);
      }
    });
    
    // Add context menu for favorite toggle
    ContextMenu contextMenu = createProjectContextMenu(initials);
    projectItem.setOnContextMenuRequested(e -> {
      contextMenu.show(projectItem, e.getScreenX(), e.getScreenY());
      e.consume(); // Prevent other handlers from processing this event
    });
    
    // Store project item for highlighting
    projectItems.put(name, projectItem);

    return projectItem;
  }
  
  /** Creates context menu for project items */
  private ContextMenu createProjectContextMenu(String projectCode) {
    ContextMenu contextMenu = new ContextMenu();
    
    Optional<Project> projectOpt = projectService.getProjectByCode(projectCode);
    if (projectOpt.isPresent()) {
      Project project = projectOpt.get();
      
      MenuItem favoriteItem = new MenuItem();
      if (project.isFavorite()) {
        favoriteItem.setText("Remove from Favorites");
        favoriteItem.setGraphic(new FontIcon(Octicons.STAR_FILL_24));
      } else {
        favoriteItem.setText("Add to Favorites");
        favoriteItem.setGraphic(new FontIcon(Octicons.STAR_24));
      }
      
      favoriteItem.setOnAction(e -> {
        projectService.toggleFavorite(projectCode);
        // Just rebuild UI without loading state - data is already updated in service
        buildSidebarWithProjects();
      });
      
      contextMenu.getItems().add(favoriteItem);
    }
    
    return contextMenu;
  }
  
  /** 
   * Cancel current navigation task if any
   */
  private void cancelCurrentNavigation() {
    if (currentNavigationTask != null && !currentNavigationTask.isDone()) {
      log.debug("Cancelling current navigation task");
      currentNavigationTask.cancel(true);
      currentNavigationTask = null;
    }
  }
  
  /** 
   * Safe navigation that cancels previous navigation
   */
  private void navigateWithCancellation(Runnable navigationAction) {
    // Cancel any ongoing navigation
    cancelCurrentNavigation();
    
    // Execute navigation immediately for UI components (menu items)
    // For async operations, this would be wrapped in CompletableFuture
    currentNavigationTask = CompletableFuture.runAsync(() -> {
      try {
        // Small delay to simulate async operation
        Thread.sleep(100);
        
        // Check if not cancelled before proceeding
        if (!Thread.currentThread().isInterrupted()) {
          javafx.application.Platform.runLater(navigationAction);
        }
      } catch (InterruptedException e) {
        log.debug("Navigation task was cancelled");
        Thread.currentThread().interrupt();
      }
    });
  }

  /** Refresh sidebar to show updated project lists */
  private void refreshSidebar() {
    log.debug("Refreshing sidebar with updated project data");
    
    // If currently loading, wait for it to complete then refresh
    if (isLoading) {
      if (loadingTask != null) {
        loadingTask.thenRun(() -> Platform.runLater(this::refreshWithLoadingState));
      }
      return;
    }
    
    // Show loading and refresh data
    refreshWithLoadingState();
  }
  
  /**
   * Refresh sidebar with loading state - used when data needs to be refetched
   */
  private void refreshWithLoadingState() {
    log.debug("Refreshing sidebar with loading state");
    isLoading = true;
    
    // Clear current content and show loading
    getChildren().clear();
    getChildren().addAll(createHeader(), createMainMenu());
    
    // Always show loading placeholders when refreshing (since we're refetching data)
    getChildren().addAll(createLoadingSection("FAVORITES"), createLoadingSection("PROJECTS"));
    
    // Start async data refresh
    refreshProjectDataAsync();
  }
  
  /**
   * Refresh project data asynchronously and update UI
   */
  private void refreshProjectDataAsync() {
    if (loadingTask != null && !loadingTask.isDone()) {
      loadingTask.cancel(true);
    }
    
    loadingTask = projectService.refreshDataAsync().thenRun(() -> {
      Platform.runLater(() -> {
        isLoading = false;
        buildSidebarWithProjects();
        log.info("Sidebar refresh completed");
      });
    });
  }

  /** 
   * Builds sidebar with loading state, then loads projects asynchronously
   */
  private void buildSidebarWithLoading() {
    log.debug("Building sidebar with loading state");
    isLoading = true;
    
    // Add header and main menu immediately
    getChildren().addAll(createHeader(), createMainMenu());
    
    // Add loading placeholders for project sections
    getChildren().addAll(createLoadingSection("FAVORITES"), createLoadingSection("PROJECTS"));
    
    // Start async loading of project data
    loadProjectsAsync();
  }
  
  /** 
   * Creates a loading placeholder section
   */
  private VBox createLoadingSection(String title) {
    VBox section = new VBox(8);
    
    // Section header
    Octicons icon = title.equals("FAVORITES") ? Octicons.STAR_24 : Octicons.REPO_24;
    HBox sectionHeader = createSectionHeader(title, icon, null);
    
    // Loading indicator
    VBox loadingCard = new VBox(12);
    loadingCard.getStyleClass().add("card");
    loadingCard.setPadding(new Insets(16));
    loadingCard.setAlignment(Pos.CENTER);
    
    // Loading spinner icon
    FontIcon spinnerIcon = new FontIcon(Octicons.SYNC_24);
    spinnerIcon.getStyleClass().add("loading-spinner");
    
    // Loading text
    Label loadingText = new Label("Loading " + title.toLowerCase() + "...");
    loadingText.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
    
    loadingCard.getChildren().addAll(spinnerIcon, loadingText);
    section.getChildren().addAll(sectionHeader, loadingCard);
    
    return section;
  }
  
  /** 
   * Loads projects asynchronously and updates UI
   */
  private void loadProjectsAsync() {
    if (loadingTask != null && !loadingTask.isDone()) {
      loadingTask.cancel(true);
    }
    
    loadingTask = CompletableFuture.runAsync(() -> {
      try {
        log.info("Starting async project data loading");
        
        // Simulate loading delay (1 second as requested)
        Thread.sleep(1000);
        
        // Check if not cancelled before proceeding
        if (!Thread.currentThread().isInterrupted()) {
          Platform.runLater(() -> {
            isLoading = false;
            buildSidebarWithProjects();
            log.info("Project data loading completed");
          });
        }
      } catch (InterruptedException e) {
        log.debug("Project loading was cancelled");
        Thread.currentThread().interrupt();
      }
    });
  }
  
  /** 
   * Builds sidebar with actual project data
   */
  private void buildSidebarWithProjects() {
    log.debug("Building sidebar with project data");
    
    // Clear existing children
    getChildren().clear();
    
    // Add header and main menu
    getChildren().addAll(createHeader(), createMainMenu());
    
    // Add favorites section only if it exists
    VBox favoritesSection = createFavoritesSection();
    if (favoritesSection != null) {
      getChildren().add(favoritesSection);
    }
    
    // Add projects section only if it exists
    VBox projectsSection = createProjectsSection();
    if (projectsSection != null) {
      getChildren().add(projectsSection);
    }
    
    // Clear and rebuild project items map
    projectItems.clear();
    
    // Restore highlighting if there was an active project
    if (activeProjectName != null) {
      restoreProjectHighlighting(activeProjectName);
    }
  }
  
  /**
   * Restore project highlighting after rebuild
   */
  private void restoreProjectHighlighting(String projectName) {
    HBox projectItem = projectItems.get(projectName);
    if (projectItem != null) {
      projectItem.getStyleClass().add("active");
      activeProjectItem = projectItem;
      log.debug("Restored highlighting for project: {}", projectName);
    }
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
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleKnowledgeBase() {
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleTextComponent() {
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleBatchJobs() {
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleDBObjects() {
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleSettingsMenu() {
    navigateWithCancellation(() -> {
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
    });
  }

  private void handleNewProject() {
    log.info("Creating new project");
    showInfo("New Project", "Create a new project");
  }
  
  private void handleRefreshProjects() {
    log.info("Refreshing project list");
    // Show loading state and refresh data from service
    refreshSidebar();
  }

  private void handleProjectClick(String projectName) {
    log.info("Opening project: {}", projectName);
    
    // Update highlighting immediately
    updateActiveProjectItem(projectName);
    
    navigateWithCancellation(() -> {
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
    });
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
    
    // Update active project name tracking
    activeProjectName = projectName;
    
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
    return projectService.getProjectByCode(projectCode)
        .map(Project::getName)
        .orElse(null);
  }
  
  /**
   * Cleanup method to unregister listeners Should be called when the component is no longer needed
   */
  public void cleanup() {
    // Cancel any ongoing navigation
    cancelCurrentNavigation();
    
    // Cancel any ongoing loading
    if (loadingTask != null && !loadingTask.isDone()) {
      loadingTask.cancel(true);
    }
    
    if (themeManager != null) {
      themeManager.removeThemeChangeListener(this);
    }
  }
  
  private String getProjectCodeFromName(String projectName) {
    return projectService.getAllProjects().stream()
        .filter(project -> project.getName().equals(projectName))
        .map(Project::getCode)
        .findFirst()
        .orElse(null);
  }
}
