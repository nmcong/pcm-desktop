package com.noteflix.pcm.ui.layout;

import com.noteflix.pcm.core.constants.AppConstants;
import com.noteflix.pcm.core.navigation.DefaultPageNavigator;
import com.noteflix.pcm.core.navigation.PageNavigator;
import com.noteflix.pcm.ui.components.SidebarView;
import com.noteflix.pcm.ui.pages.AIAssistantPage;
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
    private PageNavigator pageNavigator;
    
    public MainLayer() {
        createView();
    }
    
    /**
     * Creates the view with 2-part layout (similar to AtlantaFX Sampler MainLayer line 90)
     */
    private void createView() {
        // LEFT PART: Sidebar with fixed width (full height)
        sidebar = new SidebarView();
        sidebar.setMinWidth(AppConstants.SIDEBAR_WIDTH);
        sidebar.setMaxWidth(AppConstants.SIDEBAR_WIDTH);
        sidebar.getStyleClass().add("sidebar-full-height");
        
        // CENTER PART: Main content area (flexible width) - will contain navbar + content
        mainContentPane = new StackPane();
        mainContentPane.getStyleClass().add("content-area");
        HBox.setHgrow(mainContentPane, Priority.ALWAYS);
        
        // Initialize navigation
        initializeNavigation();
        
        // Setup main layout - sidebar takes full height, content area is on the right
        setId("main");
        setLeft(sidebar);        // LEFT PART: Sidebar (full height)
        setCenter(mainContentPane); // CENTER PART: Content area (navbar + main content)
        
        log.info("MainLayer initialized with sidebar full height layout");
    }
    
    /**
     * Initializes navigation system
     */
    private void initializeNavigation() {
        // Create page navigator
        pageNavigator = new DefaultPageNavigator(mainContentPane);
        
        // Inject navigator into sidebar
        sidebar.setPageNavigator(pageNavigator);
        
        // Navigate to default page (AI Assistant)
        pageNavigator.navigateToPage(AIAssistantPage.class);
        
        log.info("Navigation system initialized");
    }
    
    /**
     * Switches the main content to a new view
     */
    public void setMainContent(javafx.scene.Node content) {
        mainContentPane.getChildren().setAll(content);
        log.info("Main content updated");
    }

}