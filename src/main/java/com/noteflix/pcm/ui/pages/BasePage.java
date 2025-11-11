package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Abstract base class for all pages in the application
 * Follows SOLID principles - Single Responsibility, Open/Closed
 */
@Slf4j
@Getter
public abstract class BasePage extends VBox {
    
    protected final String pageTitle;
    protected final String pageDescription;
    protected final FontIcon pageIcon;
    
    protected BasePage(String title, String description, FontIcon icon) {
        this.pageTitle = title;
        this.pageDescription = description;
        this.pageIcon = icon;
        
        initializeLayout();
        buildContent();
    }
    
    /**
     * Initialize base layout structure
     */
    private void initializeLayout() {
        setSpacing(20);
        setPadding(new Insets(32, 24, 24, 24));
        getStyleClass().add("page-container");
        VBox.setVgrow(this, Priority.ALWAYS);
        
        log.debug("Initialized layout for page: {}", pageTitle);
    }
    
    /**
     * Build the page content - template method pattern
     */
    private void buildContent() {
        // Header section (if provided)
        var header = createPageHeader();
        if (header != null) {
            getChildren().add(header);
        }
        
        // Main content - implemented by subclasses
        getChildren().add(createMainContent());
        
        // Footer section (if needed)
        var footer = createPageFooter();
        if (footer != null) {
            getChildren().add(footer);
        }
    }
    
    /**
     * Creates the page header with title and description
     */
    protected VBox createPageHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("page-header");
        
        // Title with icon
        Label titleLabel = new Label(pageTitle);
        titleLabel.setGraphic(pageIcon);
        titleLabel.getStyleClass().addAll(Styles.TITLE_1, "page-title");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-graphic-text-gap: 12px;");
        
        // Description
        Label descriptionLabel = new Label(pageDescription);
        descriptionLabel.getStyleClass().addAll(Styles.TEXT_MUTED, "page-description");
        descriptionLabel.setWrapText(true);
        
        header.getChildren().addAll(titleLabel, descriptionLabel);
        return header;
    }
    
    /**
     * Abstract method for creating main content - must be implemented by subclasses
     * Follows Open/Closed principle
     */
    protected abstract VBox createMainContent();
    
    /**
     * Optional footer creation - can be overridden by subclasses
     */
    protected VBox createPageFooter() {
        return null; // No footer by default
    }
    
    /**
     * Lifecycle method called when page becomes active
     */
    public void onPageActivated() {
        log.info("Page activated: {}", pageTitle);
        // Can be overridden by subclasses for specific behavior
    }
    
    /**
     * Lifecycle method called when page becomes inactive
     */
    public void onPageDeactivated() {
        log.debug("Page deactivated: {}", pageTitle);
        // Can be overridden by subclasses for cleanup
    }
}