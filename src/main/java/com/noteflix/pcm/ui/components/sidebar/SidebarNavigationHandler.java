package com.noteflix.pcm.ui.components.sidebar;

import com.noteflix.pcm.application.service.project.IProjectService;
import com.noteflix.pcm.core.navigation.PageNavigator;
import com.noteflix.pcm.domain.entity.Project;
import com.noteflix.pcm.ui.pages.projects.BaseProjectPage;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Handles navigation logic for the sidebar
 * Manages navigation cancellation and project navigation
 */
@Slf4j
public class SidebarNavigationHandler {
    
    private final PageNavigator pageNavigator;
    private final IProjectService projectService;
    private final ProjectHighlightManager highlightManager;
    private final Runnable onClearMenuHighlight; // Callback to clear menu highlighting
    
    // Navigation cancellation support
    private CompletableFuture<?> currentNavigationTask = null;
    
    public SidebarNavigationHandler(PageNavigator pageNavigator, 
                                   IProjectService projectService,
                                   ProjectHighlightManager highlightManager,
                                   Runnable onClearMenuHighlight) {
        this.pageNavigator = pageNavigator;
        this.projectService = projectService;
        this.highlightManager = highlightManager;
        this.onClearMenuHighlight = onClearMenuHighlight;
    }
    
    /**
     * Handle project click navigation
     */
    public void handleProjectClick(String projectName) {
        log.info("Opening project: {}", projectName);
        
        // Clear menu highlighting when clicking on project
        if (onClearMenuHighlight != null) {
            onClearMenuHighlight.run();
        }
        
        // Store active project name for highlighting
        highlightManager.setActiveProjectName(projectName);
        
        // Try to highlight immediately if possible
        highlightManager.updateActiveProjectItem(projectName);
        
        // Navigate directly without cancellation wrapper for immediate response
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
    
    /**
     * Safe navigation that cancels previous navigation
     */
    public void navigateWithCancellation(Runnable navigationAction) {
        // Cancel any ongoing navigation
        cancelCurrentNavigation();
        
        // Execute navigation immediately for UI components (menu items)
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
    
    /**
     * Cancel current navigation task if any
     */
    public void cancelCurrentNavigation() {
        if (currentNavigationTask != null && !currentNavigationTask.isDone()) {
            log.debug("Cancelling current navigation task");
            currentNavigationTask.cancel(true);
            currentNavigationTask = null;
        }
    }
    
    /**
     * Clear project highlighting for menu navigation
     */
    public void clearProjectHighlightingForMenuNavigation() {
        highlightManager.updateActiveProjectItem(null);
    }
    
    /**
     * Get project code from project name
     */
    private String getProjectCodeFromName(String projectName) {
        return projectService.getAllProjects().stream()
            .filter(project -> project.getName().equals(projectName))
            .map(Project::getCode)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Show info dialog
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Cleanup navigation resources
     */
    public void cleanup() {
        cancelCurrentNavigation();
    }
}