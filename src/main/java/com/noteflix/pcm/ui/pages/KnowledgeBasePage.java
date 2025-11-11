package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Knowledge Base page - Single Responsibility Principle
 * Manages knowledge base content and search functionality
 */
@Slf4j
public class KnowledgeBasePage extends BasePage {
    
    private TextField searchField;
    private VBox searchResults;
    
    public KnowledgeBasePage() {
        super(
            "Knowledge Base",
            "Browse, search, and manage your organization's knowledge repository",
            new FontIcon(Feather.BOOK_OPEN)
        );
    }
    
    @Override
    protected VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.getStyleClass().add("knowledge-base-content");
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Search section
        mainContent.getChildren().add(createSearchSection());
        
        // Categories section
        mainContent.getChildren().add(createCategoriesSection());
        
        // Recent articles
        mainContent.getChildren().add(createRecentArticlesSection());
        
        return mainContent;
    }
    
    private VBox createSearchSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(24));
        
        Label searchTitle = new Label("Search Knowledge Base");
        searchTitle.getStyleClass().addAll(Styles.TITLE_3);
        
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search for documentation, guides, best practices...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button searchButton = new Button("Search");
        searchButton.setGraphic(new FontIcon(Feather.SEARCH));
        searchButton.getStyleClass().addAll(Styles.ACCENT);
        searchButton.setOnAction(e -> handleSearch());
        
        searchField.setOnAction(e -> handleSearch());
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Search results area
        searchResults = new VBox(8);
        searchResults.getStyleClass().add("search-results");
        
        section.getChildren().addAll(searchTitle, searchBox, searchResults);
        return section;
    }
    
    private VBox createCategoriesSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label categoriesTitle = new Label("Browse by Category");
        categoriesTitle.getStyleClass().addAll(Styles.TITLE_3);
        
        // Categories grid
        VBox categoriesGrid = new VBox(12);
        
        HBox row1 = new HBox(16);
        row1.getChildren().addAll(
            createCategoryCard("Development Guidelines", "Best practices, coding standards, and development workflows", Feather.CODE, "12 articles"),
            createCategoryCard("System Architecture", "Design patterns, architecture decisions, and technical documentation", Feather.LAYERS, "8 articles"),
            createCategoryCard("Database Guide", "Schema design, query optimization, and maintenance procedures", Feather.DATABASE, "15 articles")
        );
        
        HBox row2 = new HBox(16);
        row2.getChildren().addAll(
            createCategoryCard("Security Policies", "Security guidelines, compliance requirements, and procedures", Feather.SHIELD, "6 articles"),
            createCategoryCard("Deployment", "CI/CD processes, deployment guides, and environment management", Feather.UPLOAD, "10 articles"),
            createCategoryCard("Troubleshooting", "Common issues, debugging guides, and solution documentation", Feather.TOOL, "22 articles")
        );
        
        categoriesGrid.getChildren().addAll(row1, row2);
        section.getChildren().addAll(categoriesTitle, categoriesGrid);
        return section;
    }
    
    private VBox createCategoryCard(String title, String description, Feather icon, String articleCount) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("category-card", "interactive-card");
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPrefHeight(120);
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(20);
        iconNode.getStyleClass().add("category-icon");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "category-title");
        
        header.getChildren().addAll(iconNode, titleLabel);
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        descriptionLabel.setWrapText(true);
        
        Label countLabel = new Label(articleCount);
        countLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "accent-text");
        
        card.getChildren().addAll(header, descriptionLabel, countLabel);
        
        card.setOnMouseClicked(e -> handleCategoryClick(title));
        
        return card;
    }
    
    private VBox createRecentArticlesSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label recentTitle = new Label("Recently Updated");
        recentTitle.getStyleClass().addAll(Styles.TITLE_3);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewAllButton = new Button("View All");
        viewAllButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
        
        headerRow.getChildren().addAll(recentTitle, spacer, viewAllButton);
        
        VBox articlesList = new VBox(12);
        articlesList.getChildren().addAll(
            createArticleItem("Database Migration Best Practices", "Updated procedures for safe database migrations", "2 hours ago", Feather.DATABASE),
            createArticleItem("New Security Guidelines", "Updated security policies and implementation guidelines", "1 day ago", Feather.SHIELD),
            createArticleItem("Code Review Checklist", "Comprehensive checklist for thorough code reviews", "3 days ago", Feather.CHECK_CIRCLE),
            createArticleItem("Performance Monitoring Guide", "How to effectively monitor and optimize system performance", "1 week ago", Feather.ACTIVITY)
        );
        
        section.getChildren().addAll(headerRow, articlesList);
        return section;
    }
    
    private HBox createArticleItem(String title, String description, String updated, Feather icon) {
        HBox item = new HBox(16);
        item.getStyleClass().addAll("article-item", "interactive-item");
        item.setPadding(new Insets(16));
        item.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon iconNode = new FontIcon(icon);
        iconNode.setIconSize(16);
        iconNode.getStyleClass().add("article-icon");
        
        VBox content = new VBox(4);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "article-title");
        
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        
        content.getChildren().addAll(titleLabel, descriptionLabel);
        
        Label updatedLabel = new Label(updated);
        updatedLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        
        item.getChildren().addAll(iconNode, content, updatedLabel);
        
        item.setOnMouseClicked(e -> handleArticleClick(title));
        
        return item;
    }
    
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            log.info("Searching knowledge base for: {}", query);
            
            searchResults.getChildren().clear();
            
            // Simulate search results
            searchResults.getChildren().addAll(
                createSearchResultItem("Database Best Practices", "Comprehensive guide covering database design and optimization...", "Development Guidelines"),
                createSearchResultItem("Security Implementation Guide", "Step-by-step security implementation procedures...", "Security Policies"),
                createSearchResultItem("Code Review Standards", "Standards and checklists for effective code reviews...", "Development Guidelines")
            );
        }
    }
    
    private VBox createSearchResultItem(String title, String snippet, String category) {
        VBox item = new VBox(8);
        item.getStyleClass().addAll("search-result-item", "interactive-item");
        item.setPadding(new Insets(16));
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "result-title");
        
        Label snippetLabel = new Label(snippet);
        snippetLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        snippetLabel.setWrapText(true);
        
        Label categoryLabel = new Label("Category: " + category);
        categoryLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "accent-text");
        
        item.getChildren().addAll(titleLabel, snippetLabel, categoryLabel);
        
        item.setOnMouseClicked(e -> handleArticleClick(title));
        
        return item;
    }
    
    private void handleCategoryClick(String category) {
        log.info("Category clicked: {}", category);
        // Navigate to category view or filter articles
    }
    
    private void handleArticleClick(String article) {
        log.info("Article clicked: {}", article);
        // Navigate to article detail view
    }
    
    @Override
    public void onPageActivated() {
        super.onPageActivated();
        if (searchField != null) {
            searchField.requestFocus();
        }
    }
}