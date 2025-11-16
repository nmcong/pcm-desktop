package com.noteflix.pcm.ui.components.sidebar;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.application.service.project.IProjectService;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Manages loading states and animations for the sidebar
 * Handles async data loading with visual feedback
 */
@Slf4j
public class LoadingManager {
    
    private final IProjectService projectService;
    private final Consumer<String> onSectionHeaderCreate;
    private final Runnable onRefreshStart; // Callback when refresh starts
    private final Runnable onRefreshComplete; // Callback when refresh completes
    
    // Loading state
    private boolean isLoading = false;
    private CompletableFuture<Void> loadingTask = null;
    
    // Loading animations
    private final List<RotateTransition> loadingAnimations = new ArrayList<>();
    
    public LoadingManager(IProjectService projectService, 
                         Consumer<String> onSectionHeaderCreate,
                         Runnable onRefreshStart,
                         Runnable onRefreshComplete) {
        this.projectService = projectService;
        this.onSectionHeaderCreate = onSectionHeaderCreate;
        this.onRefreshStart = onRefreshStart;
        this.onRefreshComplete = onRefreshComplete;
    }
    
    /**
     * Creates a loading placeholder section
     */
    public VBox createLoadingSection(String title) {
        VBox section = new VBox(8);
        
        // Section header
        Octicons icon = title.equals("FAVORITES") ? Octicons.STAR_24 : Octicons.REPO_24;
        HBox sectionHeader = createSectionHeader(title, icon);
        
        // Loading indicator
        VBox loadingCard = new VBox(12);
        loadingCard.getStyleClass().add("card");
        loadingCard.setPadding(new Insets(16));
        loadingCard.setAlignment(Pos.CENTER);
        
        // Loading spinner icon with rotation animation
        FontIcon spinnerIcon = new FontIcon(Octicons.SYNC_24);
        spinnerIcon.getStyleClass().add("loading-spinner");
        
        // Create rotation animation
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1.0), spinnerIcon);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.play();
        
        // Track animation for cleanup
        loadingAnimations.add(rotateTransition);
        
        // Loading text
        Label loadingText = new Label("Loading " + title.toLowerCase() + "...");
        loadingText.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        
        loadingCard.getChildren().addAll(spinnerIcon, loadingText);
        section.getChildren().addAll(sectionHeader, loadingCard);
        
        return section;
    }
    
    private HBox createSectionHeader(String title, Octicons icon) {
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);
        
        HBox header = new HBox(8, iconNode, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        
        return header;
    }
    
    /**
     * Start async project data loading
     */
    public CompletableFuture<Void> startAsyncDataLoading(Runnable onComplete) {
        if (loadingTask != null && !loadingTask.isDone()) {
            loadingTask.cancel(true);
        }
        
        isLoading = true;
        
        loadingTask = CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async project data loading");
                
                // Simulate loading delay (1 second as requested)
                Thread.sleep(1000);
                
                // Check if not cancelled before proceeding
                if (!Thread.currentThread().isInterrupted()) {
                    Platform.runLater(() -> {
                        isLoading = false;
                        onComplete.run();
                        log.info("Project data loading completed");
                    });
                }
            } catch (InterruptedException e) {
                log.debug("Project loading was cancelled");
                Thread.currentThread().interrupt();
            }
        });
        
        return loadingTask;
    }
    
    /**
     * Refresh project data asynchronously
     */
    public CompletableFuture<Void> refreshProjectDataAsync(Runnable onComplete) {
        if (loadingTask != null && !loadingTask.isDone()) {
            loadingTask.cancel(true);
        }
        
        isLoading = true;
        
        // Notify refresh start (to start button animation)
        if (onRefreshStart != null) {
            onRefreshStart.run();
        }
        
        loadingTask = projectService.refreshDataAsync().thenRun(() -> {
            Platform.runLater(() -> {
                isLoading = false;
                
                // Notify refresh complete (to stop button animation)
                if (onRefreshComplete != null) {
                    onRefreshComplete.run();
                }
                
                onComplete.run();
                log.info("Sidebar refresh completed");
            });
        });
        
        return loadingTask;
    }
    
    /**
     * Cancel current loading task if any
     */
    public void cancelCurrentLoading() {
        if (loadingTask != null && !loadingTask.isDone()) {
            log.debug("Cancelling current loading task");
            loadingTask.cancel(true);
            isLoading = false;
            stopLoadingAnimations();
        }
    }
    
    /**
     * Stop all loading animations and clear the list
     */
    public void stopLoadingAnimations() {
        for (RotateTransition animation : loadingAnimations) {
            if (animation != null) {
                animation.stop();
            }
        }
        loadingAnimations.clear();
    }
    
    /**
     * Check if currently loading
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Get current loading task
     */
    public CompletableFuture<Void> getLoadingTask() {
        return loadingTask;
    }
    
    /**
     * Cleanup loading resources
     */
    public void cleanup() {
        cancelCurrentLoading();
        stopLoadingAnimations();
    }
}