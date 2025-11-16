package com.noteflix.pcm.ui.pages.projects;

import com.noteflix.pcm.application.service.project.ProjectData;
import com.noteflix.pcm.application.service.project.ProjectService;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * Base Project Page - Abstract implementation for all project pages
 * Provides common structure and styling for project-specific placeholder pages.
 * Each project page extends this to show relevant project information and features.
 */
@Slf4j
public class BaseProjectPage extends BaseView {

    // Getters
    @Getter
    private final String projectCode;
  private final ProjectService projectService;
  private ProjectData projectData;
  private VBox dynamicContent;
  private StackPane loadingContainer;
    private Label titleLabel;
  private Label descriptionLabel;

  public BaseProjectPage(String projectCode) {
    super(
        "Loading Project...",
        "Loading project information...",
        new FontIcon(Octicons.REPO_24)
    );
    this.projectCode = projectCode;
    this.projectService = new ProjectService();
    log.debug("{} project page initialized", projectCode);
    
    // Initialize loading after construction is complete
    Platform.runLater(this::loadProjectData);
  }

  @Override
  protected VBox createPageHeader() {
      VBox headerContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_SM);
    headerContainer.setAlignment(Pos.TOP_LEFT);
    headerContainer.getStyleClass().add(StyleConstants.PAGE_HEADER);

    // Title with icon
    titleLabel = new Label("Loading Project...");
    titleLabel.setGraphic(new FontIcon(Octicons.REPO_24));
    titleLabel.getStyleClass().addAll("title-1", StyleConstants.PAGE_TITLE);
    titleLabel.setStyle("-fx-font-weight: bold; -fx-graphic-text-gap: " + LayoutConstants.SPACING_MD + "px;");

    // Description
    descriptionLabel = new Label("Loading project information...");
    descriptionLabel.getStyleClass().addAll("text-muted", StyleConstants.PAGE_DESCRIPTION);
    descriptionLabel.setWrapText(true);

    headerContainer.getChildren().addAll(titleLabel, descriptionLabel);
    return headerContainer;
  }

  @Override
  protected Node createMainContent() {
    // Create root container with loading overlay support
    StackPane root = new StackPane();
    
    // Create main content container
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    content.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    content.setAlignment(Pos.CENTER);
    LayoutHelper.setVGrow(content);
    content.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

    // Create dynamic content container
    dynamicContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    dynamicContent.setAlignment(Pos.CENTER);
    LayoutHelper.setVGrow(dynamicContent);
    content.getChildren().add(dynamicContent);
    
    // Create loading overlay
    createLoadingOverlay();
    
    // Add to root
    root.getChildren().addAll(content, loadingContainer);
    StackPane.setAlignment(content, Pos.CENTER);
    StackPane.setAlignment(loadingContainer, Pos.CENTER);

    return root;
  }

  private VBox createProjectAvatar() {
    VBox container = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    container.setAlignment(Pos.CENTER);

    // Large avatar with project initials
    Label avatarLabel = new Label(projectCode);
    avatarLabel.getStyleClass().addAll("project-avatar-large", getProjectColorClass());
    avatarLabel.setStyle(
        "-fx-background-radius: 20px;" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 36px;" +
        "-fx-font-weight: bold;" +
        "-fx-min-width: 80px;" +
        "-fx-min-height: 80px;" +
        "-fx-max-width: 80px;" +
        "-fx-max-height: 80px;" +
        "-fx-alignment: center;"
    );

    container.getChildren().add(avatarLabel);
    return container;
  }

  private VBox createProjectInfo() {
    VBox container = LayoutHelper.createVBox(LayoutConstants.SPACING_SM);
    container.setAlignment(Pos.CENTER);

    if (projectData != null) {
      Label titleLabel = UIFactory.createSectionTitle(projectData.getName());
      titleLabel.getStyleClass().add("project-title");

      Label descriptionLabel = UIFactory.createMutedLabel(projectData.getDescription());
      descriptionLabel.setWrapText(true);
      descriptionLabel.getStyleClass().add("project-description");

      Label statsLabel = UIFactory.createMutedLabel("📱 " + projectData.getScreenCount() + " • 🔄 " + projectData.getFormattedStatus());
      statsLabel.getStyleClass().add("project-stats");

      container.getChildren().addAll(titleLabel, descriptionLabel, statsLabel);
    }
    return container;
  }

  private VBox createProjectFeatures() {
    VBox container = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    container.setAlignment(Pos.CENTER);
    container.getStyleClass().add("project-features");

    Label featuresTitle = UIFactory.createBoldLabel("Available Features:");
    featuresTitle.getStyleClass().add("features-title");

    Label featuresContent = UIFactory.createMutedLabel(getProjectFeaturesText());
    featuresContent.setWrapText(true);
    featuresContent.getStyleClass().add("features-content");

    Label comingSoonLabel = UIFactory.createMutedLabel("Coming soon...");
    comingSoonLabel.getStyleClass().add("coming-soon");
    comingSoonLabel.setStyle("-fx-font-style: italic;");

    container.getChildren().addAll(featuresTitle, featuresContent, comingSoonLabel);
    return container;
  }

  @Override
  public void onActivate() {
    super.onActivate();
    log.debug("{} project page activated", projectCode);
  }

  private void createLoadingOverlay() {
    loadingContainer = new StackPane();
    loadingContainer.getStyleClass().add("loading-overlay");
    loadingContainer.setVisible(false);
    loadingContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    loadingContainer.setMinSize(0, 0);
    loadingContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    
    VBox loadingContent = new VBox(LayoutConstants.SPACING_MD);
    loadingContent.setAlignment(Pos.CENTER);
    loadingContent.getStyleClass().add("loading-content");
    
    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setMaxSize(60, 60);
    
    Label loadingLabel = new Label("Loading project data...");
    loadingLabel.getStyleClass().add("loading-label");
    
    loadingContent.getChildren().addAll(progressIndicator, loadingLabel);
    loadingContainer.getChildren().add(loadingContent);
    StackPane.setAlignment(loadingContent, Pos.CENTER);
  }
  
  private void showLoading() {
    if (loadingContainer != null) {
      loadingContainer.setVisible(true);
    }
  }
  
  private void hideLoading() {
    if (loadingContainer != null) {
      loadingContainer.setVisible(false);
    }
  }

  // Methods that use projectData
  protected String getProjectColorClass() {
    return projectData != null ? projectData.getProjectColorClass() : "project-color-default";
  }

  protected String getProjectFeaturesText() {
    if (projectData != null && projectData.getFeatures() != null) {
      return String.join("\n• ", projectData.getFeatures());
    }
    return "Loading features...";
  }

  private void loadProjectData() {
    if (projectService == null) {
      log.error("ProjectService is null, cannot load project data");
      return;
    }
    
    showLoading();
    projectService.loadProjectAsync(projectCode)
        .thenAccept(this::updateUIWithProjectData)
        .exceptionally(throwable -> {
          Platform.runLater(() -> showErrorState(throwable));
          return null;
        });
  }

  private void updateUIWithProjectData(ProjectData data) {
    Platform.runLater(() -> {
      this.projectData = data;

      if (data != null && data.getName() != null) {
        titleLabel.setText(data.getName() + " (" + projectCode + ")");
        descriptionLabel.setText(data.getDescription() != null ? data.getDescription() : "");
      }
      
      // Create main project card
      VBox projectCard = UIFactory.createCard();
      projectCard.setAlignment(Pos.CENTER);
      projectCard.setPrefHeight(500);
      projectCard.setMaxWidth(600);
      projectCard.getStyleClass().add("project-placeholder-card");

      // Project icon/avatar
      VBox avatarContainer = createProjectAvatar();
      
      // Project info
      VBox projectInfo = createProjectInfo();
      
      // Project features
      VBox featuresSection = createProjectFeatures();

      projectCard.getChildren().addAll(avatarContainer, projectInfo, featuresSection);
      
      // Update dynamic content
      dynamicContent.getChildren().clear();
      dynamicContent.getChildren().add(projectCard);
      
      hideLoading();
      log.info("Project data loaded for {}: {}", projectCode, data != null ? data.getName() : "null");
    });
  }

  private void showErrorState(Throwable throwable) {
    titleLabel.setText("Error Loading Project (" + projectCode + ")");
    descriptionLabel.setText("Failed to load project information");
    
    VBox errorCard = UIFactory.createCard();
    errorCard.setAlignment(Pos.CENTER);
    errorCard.getStyleClass().add("error-state");
    
    FontIcon errorIcon = new FontIcon(Octicons.ALERT_24);
    errorIcon.getStyleClass().add("error-icon");
    
    Label errorTitle = UIFactory.createSectionTitle("Failed to Load Project");
    errorTitle.getStyleClass().add("error-title");
    
    Label errorMessage = UIFactory.createMutedLabel("Could not load project data for: " + projectCode);
    errorMessage.getStyleClass().add("error-message");
    
    errorCard.getChildren().addAll(errorIcon, errorTitle, errorMessage);
    
    dynamicContent.getChildren().clear();
    dynamicContent.getChildren().add(errorCard);
    hideLoading();
    
    log.error("Failed to load project data for {}", projectCode, throwable);
  }
}