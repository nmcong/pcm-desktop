package com.noteflix.pcm.ui.components.sidebar;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Manages project highlighting logic for the sidebar
 * Follows Single Responsibility Principle - only handles project highlighting
 */
@Slf4j
public class ProjectHighlightManager {
    
    private final Map<String, HBox> projectItems;
    private String activeProjectName = null;
    private HBox activeProjectItem = null;
    
    public ProjectHighlightManager(Map<String, HBox> projectItems) {
        this.projectItems = projectItems;
    }
    
    /**
     * Updates the active project item highlighting
     * @param projectName The name of the project to highlight (null to clear)
     */
    public void updateActiveProjectItem(String projectName) {
        // Remove active state from ALL project items to ensure clean state
        projectItems.values().forEach(item -> item.getStyleClass().remove("active"));
        
        // Clear active project item reference
        activeProjectItem = null;
        
        // Only clear activeProjectName if explicitly setting to null
        if (projectName == null) {
            activeProjectName = null;
            log.debug("Cleared project highlighting");
            return;
        }
        
        // Set new active project name
        activeProjectName = projectName;
        
        // Add active state to current project item
        HBox currentProjectItem = projectItems.get(projectName);
        if (currentProjectItem != null) {
            currentProjectItem.getStyleClass().add("active");
            activeProjectItem = currentProjectItem;
            log.debug("Highlighted project item: {}", projectName);
        } else {
            log.debug("No project item found for: {} (will highlight when UI rebuilds)", projectName);
        }
    }
    
    /**
     * Restore project highlighting after UI rebuild
     */
    public void restoreProjectHighlighting() {
        if (activeProjectName == null) {
            return;
        }
        
        log.debug("Attempting to restore highlighting for project: {} (projectItems size: {})", 
                  activeProjectName, projectItems.size());
        
        HBox projectItem = projectItems.get(activeProjectName);
        if (projectItem != null) {
            projectItem.getStyleClass().add("active");
            activeProjectItem = projectItem;
            log.info("✓ Restored highlighting for project: {}", activeProjectName);
        } else {
            log.warn("✗ Could not restore highlighting - project item not found: {}", activeProjectName);
            log.debug("Available project items: {}", projectItems.keySet());
        }
    }
    
    /**
     * Ensure highlighting is applied - final fallback method
     */
    public void ensureHighlightingIsApplied() {
        if (activeProjectName != null && (activeProjectItem == null || 
            !activeProjectItem.getStyleClass().contains("active"))) {
            log.debug("Final attempt to ensure highlighting for: {}", activeProjectName);
            
            HBox projectItem = projectItems.get(activeProjectName);
            if (projectItem != null) {
                // Remove active from all items first
                projectItems.values().forEach(item -> item.getStyleClass().remove("active"));
                
                // Add active to correct item
                projectItem.getStyleClass().add("active");
                activeProjectItem = projectItem;
                log.info("✓ Final highlighting applied for project: {}", activeProjectName);
            } else {
                log.warn("✗ Final highlighting failed - still no project item for: {}", activeProjectName);
            }
        }
    }
    
    /**
     * Schedule highlighting restoration with proper timing
     */
    public void scheduleHighlightingRestoration() {
        if (activeProjectName != null) {
            Platform.runLater(() -> {
                log.debug("Attempting to restore highlighting for: {} (available: {})", 
                         activeProjectName, projectItems.keySet());
                restoreProjectHighlighting();
                
                // Double-check with a small delay
                Platform.runLater(() -> {
                    if (!projectItems.isEmpty()) {
                        ensureHighlightingIsApplied();
                    }
                });
            });
        }
    }
    
    /**
     * Apply highlighting immediately when creating project item
     * @param projectName The project name
     * @param projectItem The project item UI component
     */
    public void applyImmediateHighlighting(String projectName, HBox projectItem) {
        if (projectName.equals(activeProjectName)) {
            projectItem.getStyleClass().add("active");
            activeProjectItem = projectItem;
            log.debug("Immediately highlighted active project: {}", projectName);
        }
    }
    
    /**
     * Get the current active project name
     */
    public String getActiveProjectName() {
        return activeProjectName;
    }
    
    /**
     * Set active project name (used when navigating to project pages)
     */
    public void setActiveProjectName(String projectName) {
        this.activeProjectName = projectName;
        log.debug("Set active project name: {}", projectName);
    }
}