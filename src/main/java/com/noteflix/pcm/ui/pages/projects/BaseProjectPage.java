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
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.time.format.DateTimeFormatter;

/**
 * Base Project Page - Abstract implementation for all project pages
 * 
 * Provides common structure and styling for project-specific placeholder pages.
 * Each project page extends this to show relevant project information and features.
 */
@Slf4j
public abstract class BaseProjectPage extends BaseView {

  private final String projectCode;
  private final ProjectService projectService;
  private ProjectData projectData;
  private VBox dynamicContent;

  protected BaseProjectPage(String projectCode) {
    super(
        "Loading Project..." + " (" + projectCode + ")",
        "Loading project information...",
        new FontIcon(Octicons.REPO_24)
    );
    this.projectCode = projectCode;
    this.projectService = new ProjectService();
    log.debug("{} project page initialized", projectCode);
  }

  @Override
  protected Node createMainContent() {
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    content.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    content.setAlignment(Pos.CENTER);
    LayoutHelper.setVGrow(content);

    // Create dynamic content container
    dynamicContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    content.getChildren().add(dynamicContent);

    // Start loading project data
    loadProjectData();

    return content;
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

  // Methods that use projectData
  protected String getProjectColorClass() {
    return projectData != null ? projectData.getColorClass() : "project-color-default";
  }

  protected String getProjectFeaturesText() {
    return projectData != null ? projectData.getFormattedFeatures() : "Loading features...";
  }

  private void loadProjectData() {
    showLoading();
    projectService.loadProject(projectCode)
        .thenAccept(this::updateUIWithProjectData)
        .exceptionally(throwable -> {
          Platform.runLater(() -> showErrorState(throwable));
          return null;
        });
  }

  private void updateUIWithProjectData(ProjectData data) {
    Platform.runLater(() -> {
      this.projectData = data;
      
      // Update title and description
      setTitle(data.getName() + " (" + projectCode + ")");
      setDescription(data.getDescription());
      
      // Create main project card
      VBox projectCard = UIFactory.createCard();
      projectCard.setAlignment(Pos.CENTER);
      projectCard.setPrefHeight(500);
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
      log.info("Project data loaded for {}: {}", projectCode, data.getName());
    });
  }

  private void showErrorState(Throwable throwable) {
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

  // Getters
  protected String getProjectCode() { return projectCode; }
  protected ProjectData getProjectData() { return projectData; }
}