package com.noteflix.pcm.ui.components;

import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.events.ThemeChangeListener;
import com.noteflix.pcm.core.navigation.PageNavigator;
import com.noteflix.pcm.core.navigation.NavigationListener;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.ui.pages.*;
import com.noteflix.pcm.ui.pages.ai.AIAssistantPage;
import com.noteflix.pcm.ui.pages.projects.*;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.application.service.project.IProjectService;
import com.noteflix.pcm.application.service.project.ProjectServiceFactory;
import com.noteflix.pcm.domain.entity.Project;
import com.noteflix.pcm.ui.components.sidebar.*;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

/** 
 * Refactored Sidebar component using composition and SOLID principles
 * Main coordinator for sidebar components
 */
@Slf4j
public class SidebarView extends VBox implements ThemeChangeListener, NavigationListener {

  private final ThemeManager themeManager;
  private final IProjectService projectService;
  
  /**
   * Sets the page navigator for navigation functionality
   */
  private PageNavigator pageNavigator;
  
  public void setPageNavigator(PageNavigator pageNavigator) {
    this.pageNavigator = pageNavigator;
    // Update navigation handler when pageNavigator is set
    if (navigationHandler != null) {
      // Recreate navigation handler with new pageNavigator
      SidebarNavigationHandler newNavigationHandler = new SidebarNavigationHandler(
          pageNavigator, 
          projectService, 
          highlightManager,
          () -> mainMenu.clearHighlighting()
      );
      // Replace the old handler - we'll need to update the reference
      updateNavigationHandler(newNavigationHandler);
    }
  }
  
  private void updateNavigationHandler(SidebarNavigationHandler newHandler) {
    // Clean up old handler
    if (navigationHandler != null) {
      navigationHandler.cleanup();
    }
    // Update the reference
    this.navigationHandler = newHandler;
  }
  
  // Component managers following Single Responsibility Principle
  private final SidebarHeader header;
  private final MainMenu mainMenu;
  private final ProjectSectionManager projectSectionManager;
  private final ProjectHighlightManager highlightManager;
  private SidebarNavigationHandler navigationHandler; // Non-final to allow updates
  private final LoadingManager loadingManager;
  
  // Map to store project items for highlighting (shared with managers)
  private final Map<String, HBox> projectItems = new HashMap<>();

  public SidebarView() {
    super(16);

    this.themeManager = ThemeManager.getInstance();
    this.projectService = ProjectServiceFactory.getInstance();

    getStyleClass().add("sidebar");
    setPadding(new Insets(16, 12, 16, 12));
    setPrefWidth(AppConstants.SIDEBAR_WIDTH);
    setMinWidth(AppConstants.SIDEBAR_WIDTH);
    setMaxWidth(AppConstants.SIDEBAR_WIDTH);

    // Initialize component managers
    this.highlightManager = new ProjectHighlightManager(projectItems);
    this.loadingManager = new LoadingManager(projectService, this::handleSectionHeaderCreate);
    this.header = new SidebarHeader(this::openSearchDialog);
    this.mainMenu = new MainMenu(
        this::navigateWithCancellation,
        this::handleAIAssistant,
        this::handleKnowledgeBase,
        this::handleTextComponent,
        this::handleBatchJobs,
        this::handleDBObjects,
        this::handleSettingsMenu
    );
    
    // Navigation handler initialized with null pageNavigator (will be updated when pageNavigator is set)
    this.navigationHandler = new SidebarNavigationHandler(
        null, // pageNavigator will be set later
        projectService, 
        highlightManager,
        () -> mainMenu.clearHighlighting() // Callback to clear menu highlighting
    );
    
    this.projectSectionManager = new ProjectSectionManager(
        projectService,
        projectItems,
        highlightManager,
        this::handleProjectClick,
        this::handleRefreshProjects,
        this::handleNewProject,
        this::buildSidebarWithProjects // For context menu actions - just rebuild UI
    );

    // Register for theme changes
    themeManager.addThemeChangeListener(this);

    // Build initial sidebar with loading state
    buildSidebarWithLoading();
  }

  // Delegate header creation to component
  private VBox createHeader() {
    return header;
  }


  // Delegate main menu creation to component
  private VBox createMainMenu() {
    return mainMenu;
  }



  // Delegate favorites section creation to component
  private VBox createFavoritesSection() {
    return projectSectionManager.createFavoritesSection();
  }

  // Delegate projects section creation to component
  private VBox createProjectsSection() {
    return projectSectionManager.createProjectsSection();
  }


  
  
  // Delegate to navigation handler
  private void cancelCurrentNavigation() {
    navigationHandler.cancelCurrentNavigation();
  }
  
  // Delegate to loading manager
  private void cancelCurrentLoading() {
    loadingManager.cancelCurrentLoading();
  }
  
  // Delegate to navigation handler
  private void navigateWithCancellation(Runnable navigationAction) {
    // Cancel any ongoing loading first
    cancelCurrentLoading();
    navigationHandler.navigateWithCancellation(navigationAction);
  }

  // Enhanced refresh method using loading manager
  private void refreshSidebar() {
    log.debug("Refreshing sidebar with updated project data");
    
    // If currently loading, wait for it to complete then refresh
    if (loadingManager.isLoading()) {
      if (loadingManager.getLoadingTask() != null) {
        loadingManager.getLoadingTask().thenRun(() -> Platform.runLater(this::refreshWithLoadingState));
      }
      return;
    }
    
    // Show loading and refresh data
    refreshWithLoadingState();
  }
  
  // Enhanced refresh with loading state using loading manager
  private void refreshWithLoadingState() {
    log.debug("Refreshing sidebar with loading state");
    
    // Clear current content and show loading
    getChildren().clear();
    getChildren().addAll(createHeader(), createMainMenu());
    
    // Show loading placeholders using loading manager
    getChildren().addAll(
        loadingManager.createLoadingSection("FAVORITES"), 
        loadingManager.createLoadingSection("PROJECTS")
    );
    
    // Start async data refresh using loading manager
    loadingManager.refreshProjectDataAsync(this::buildSidebarWithProjects);
  }
  

  // Enhanced build with loading using loading manager
  private void buildSidebarWithLoading() {
    log.debug("Building sidebar with loading state");
    
    // Add header and main menu immediately
    getChildren().addAll(createHeader(), createMainMenu());
    
    // Add loading placeholders using loading manager
    getChildren().addAll(
        loadingManager.createLoadingSection("FAVORITES"), 
        loadingManager.createLoadingSection("PROJECTS")
    );
    
    // Start async loading using loading manager
    loadingManager.startAsyncDataLoading(this::buildSidebarWithProjects);
  }
  
  
  
  // Enhanced build method using component managers
  private void buildSidebarWithProjects() {
    log.debug("Building sidebar with project data");
    
    // Stop all loading animations using loading manager
    loadingManager.stopLoadingAnimations();
    
    // Clear existing children
    getChildren().clear();
    
    // Clear project items map first - it will be rebuilt when creating sections
    projectItems.clear();
    
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
    
    // Now the projectItems map should be populated
    log.debug("Project items map populated with {} items: {}", projectItems.size(), projectItems.keySet());
    
    // Schedule highlighting restoration using highlight manager
    highlightManager.scheduleHighlightingRestoration();
  }
  
  // Delegate to loading manager
  private void stopLoadingAnimations() {
    loadingManager.stopLoadingAnimations();
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
  
  private void handleSectionHeaderCreate(String title) {
    // Callback for loading manager if needed
    log.debug("Section header created for: {}", title);
  }

  @Override
  public void onThemeChanged(boolean isDarkTheme) {
    log.debug("Theme changed to: {}", isDarkTheme ? "dark" : "light");
    // Icons automatically adapt to theme changes
  }
  
  @Override
  public void onNavigationChanged(BaseView previousPage, BaseView currentPage) {
    if (currentPage != null) {
      // Update menu highlighting using main menu component
      mainMenu.updateActiveMenuItem(currentPage.getClass());
      
      // Handle project page highlighting using highlight manager
      if (currentPage instanceof BaseProjectPage) {
        BaseProjectPage projectPage = (BaseProjectPage) currentPage;
        String projectCode = projectPage.getProjectCode();
        String projectName = getProjectNameFromCode(projectCode);
        
        // Update active project name and highlighting using highlight manager
        highlightManager.setActiveProjectName(projectName);
        highlightManager.updateActiveProjectItem(projectName);
        log.debug("Navigation changed - set active project: {}", projectName);
      } else {
        // Clear project highlighting if not on a project page
        highlightManager.updateActiveProjectItem(null);
      }
    }
  }

  private void handleAIAssistant() {
    // Clear project highlighting when navigating to menu item
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    
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
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(KnowledgeBasePage.class);
    } else {
      showInfo("Knowledge Base", "Browse and search your knowledge base");
    }
  }

  private void handleTextComponent() {
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(UniversalTextDemoPage.class);
    } else {
      showInfo("Universal Text Component", "Demo of Universal Text Component");
    }
  }

  private void handleBatchJobs() {
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(BatchJobsPage.class);
    } else {
      showInfo("Batch Jobs", "Manage scheduled and batch operations");
    }
  }

  private void handleDBObjects() {
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(DatabaseObjectsPage.class);
    } else {
      showInfo("Database Objects", "Database schema and objects");
    }
  }

  private void handleSettingsMenu() {
    navigationHandler.clearProjectHighlightingForMenuNavigation();
    if (pageNavigator != null) {
      pageNavigator.navigateToPage(SettingsPage.class);
    } else {
      showInfo("Settings", "Application configuration");
    }
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

  // Delegate to navigation handler
  private void handleProjectClick(String projectName) {
    navigationHandler.handleProjectClick(projectName);
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

  // Delegate to main menu component
  public void updateActiveMenuItem(Class<? extends BaseView> currentPageClass) {
    mainMenu.updateActiveMenuItem(currentPageClass);
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
   * Enhanced cleanup method using component managers
   */
  public void cleanup() {
    // Cleanup all component managers
    navigationHandler.cleanup();
    loadingManager.cleanup();
    mainMenu.clearHighlighting();
    
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
