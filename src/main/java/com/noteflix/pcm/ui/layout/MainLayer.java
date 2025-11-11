package com.noteflix.pcm.ui.layout;

import com.noteflix.pcm.ui.components.SidebarView;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Main layout following AtlantaFX Sampler pattern
 * Divided into 2 parts: Sidebar (left) and Main Content (center)
 */
@Getter
@Slf4j
public class MainLayer extends BorderPane {
    
    private static final double SIDEBAR_WIDTH = 280;

    /**
     * -- GETTER --
     *  Gets the sidebar for external manipulation
     */
    private SidebarView sidebar;
    /**
     * -- GETTER --
     *  Gets the main content pane for external manipulation
     */
    private StackPane mainContentPane;
    
    public MainLayer() {
        createView();
    }
    
    /**
     * Creates the view with 2-part layout (similar to AtlantaFX Sampler MainLayer line 90)
     */
    private void createView() {
        // LEFT PART: Sidebar with fixed width (full height)
        sidebar = new SidebarView();
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebar.getStyleClass().add("sidebar-full-height");
        
        // CENTER PART: Main content area (flexible width) - will contain navbar + content
        mainContentPane = new StackPane();
        mainContentPane.getStyleClass().add("content-area");
        HBox.setHgrow(mainContentPane, Priority.ALWAYS);
        
        // Set initial content
        setDefaultContent();
        
        // Setup main layout - sidebar takes full height, content area is on the right
        setId("main");
        setLeft(sidebar);        // LEFT PART: Sidebar (full height)
        setCenter(mainContentPane); // CENTER PART: Content area (navbar + main content)
        
        log.info("MainLayer initialized with sidebar full height layout");
    }
    
    /**
     * Sets default content in the main area
     */
    private void setDefaultContent() {
        VBox defaultContent = new VBox(20);
        defaultContent.getStyleClass().add("content-wrapper");
        defaultContent.setStyle("-fx-alignment: center; -fx-padding: 40px;");
        
        Label welcomeLabel = new Label("Welcome to PCM Desktop");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label instructionLabel = new Label("Select an item from the sidebar to get started");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
        
        defaultContent.getChildren().addAll(welcomeLabel, instructionLabel);
        mainContentPane.getChildren().setAll(defaultContent);
    }
    
    /**
     * Switches the main content to a new view
     */
    public void setMainContent(javafx.scene.Node content) {
        mainContentPane.getChildren().setAll(content);
        log.info("Main content updated");
    }

}