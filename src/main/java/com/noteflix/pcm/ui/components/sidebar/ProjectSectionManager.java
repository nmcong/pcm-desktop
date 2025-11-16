package com.noteflix.pcm.ui.components.sidebar;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.application.service.project.IProjectService;
import com.noteflix.pcm.domain.entity.Project;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Manages project sections (favorites and projects) for the sidebar
 * Handles project item creation and context menus
 */
@Slf4j
public class ProjectSectionManager {
    
    private final IProjectService projectService;
    private final Map<String, HBox> projectItems;
    private final ProjectHighlightManager highlightManager;
    private final Consumer<String> onProjectClick;
    private final Runnable onRefreshProjects;
    private final Runnable onNewProject;
    
    public ProjectSectionManager(IProjectService projectService,
                                Map<String, HBox> projectItems,
                                ProjectHighlightManager highlightManager,
                                Consumer<String> onProjectClick,
                                Runnable onRefreshProjects,
                                Runnable onNewProject) {
        this.projectService = projectService;
        this.projectItems = projectItems;
        this.highlightManager = highlightManager;
        this.onProjectClick = onProjectClick;
        this.onRefreshProjects = onRefreshProjects;
        this.onNewProject = onNewProject;
    }
    
    /**
     * Creates favorites section with header (only if there are favorites)
     */
    public VBox createFavoritesSection() {
        List<Project> favoriteProjects = projectService.getFavoriteProjects();
        
        if (favoriteProjects.isEmpty()) {
            log.debug("No favorite projects found, hiding favorites section");
            return null;
        }
        
        VBox section = new VBox(8);
        
        // Section header with refresh button
        Button refreshButton = createRefreshButton();
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
    
    /**
     * Creates projects section with scrollable list (only if there are non-favorite projects)
     */
    public VBox createProjectsSection() {
        List<Project> nonFavoriteProjects = projectService.getNonFavoriteProjects();
        
        if (nonFavoriteProjects.isEmpty()) {
            log.debug("No non-favorite projects found, hiding projects section");
            return null;
        }
        
        VBox section = new VBox(8);
        VBox.setVgrow(section, Priority.ALWAYS);
        
        // Section header with add button
        Button addButton = createAddButton();
        HBox sectionHeader = createSectionHeader("PROJECTS", Octicons.REPO_24, addButton);
        
        // Projects list in scrollpane
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
    
    private HBox createSectionHeader(String title, Octicons icon, Button actionButton) {
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);
        
        HBox header = new HBox(8, iconNode, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        
        if (actionButton != null) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(spacer, actionButton);
        }
        
        return header;
    }
    
    private Button createRefreshButton() {
        Button refreshButton = new Button();
        refreshButton.setGraphic(new FontIcon(Octicons.SYNC_24));
        refreshButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
        refreshButton.setTooltip(new Tooltip("Refresh Projects"));
        refreshButton.setOnAction(e -> onRefreshProjects.run());
        return refreshButton;
    }
    
    private Button createAddButton() {
        Button addButton = new Button();
        addButton.setGraphic(new FontIcon(Octicons.PLUS_24));
        addButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
        addButton.setTooltip(new Tooltip("New Project"));
        addButton.setOnAction(e -> onNewProject.run());
        return addButton;
    }
    
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
                onProjectClick.accept(name);
            }
        });
        
        // Add context menu for favorite toggle
        ContextMenu contextMenu = createProjectContextMenu(initials);
        projectItem.setOnContextMenuRequested(e -> {
            contextMenu.show(projectItem, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        
        // Store project item for highlighting
        projectItems.put(name, projectItem);
        log.debug("Added project item to map: {} (map size: {})", name, projectItems.size());
        
        // Apply immediate highlighting if this is the active project
        highlightManager.applyImmediateHighlighting(name, projectItem);
        
        return projectItem;
    }
    
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
                log.debug("Toggled favorite for project: {} (active project: {})", 
                         projectCode, highlightManager.getActiveProjectName());
                // Trigger UI rebuild
                onRefreshProjects.run();
            });
            
            contextMenu.getItems().add(favoriteItem);
        }
        
        return contextMenu;
    }
}