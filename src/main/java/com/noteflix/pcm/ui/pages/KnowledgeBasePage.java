package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.di.Injector;
import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.viewmodel.KnowledgeBaseViewModel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Knowledge Base page - Modern MVVM Architecture
 * 
 * Uses KnowledgeBaseViewModel for state management and business logic.
 * Refactored to follow UI_GUIDE.md best practices with:
 * - BaseView template pattern
 * - UI constants and factory methods  
 * - Proper component structure
 */
@Slf4j
public class KnowledgeBasePage extends BaseView {

  private final KnowledgeBaseViewModel viewModel;
  private TextField searchField;
  private VBox searchResults;

  public KnowledgeBasePage() {
    this(initializeViewModel());
  }
  
  private KnowledgeBasePage(KnowledgeBaseViewModel viewModel) {
    super(
        I18n.get("page.kb.title"), 
        I18n.get("page.kb.subtitle"), 
        new FontIcon(Octicons.BOOK_24)
    );
    this.viewModel = viewModel;
    log.debug("KnowledgeBasePage initialized with ViewModel");
  }
  
  private static KnowledgeBaseViewModel initializeViewModel() {
    try {
      KnowledgeBaseViewModel vm = Injector.getInstance().get(KnowledgeBaseViewModel.class);
      if (vm == null) {
        log.error("Failed to inject KnowledgeBaseViewModel - got null");
        throw new RuntimeException("KnowledgeBaseViewModel injection failed");
      }
      return vm;
    } catch (Exception e) {
      log.error("Failed to initialize KnowledgeBaseViewModel", e);
      throw new RuntimeException("Failed to initialize KnowledgeBaseViewModel", e);
    }
  }

  @Override
  protected Node createMainContent() {
    if (viewModel == null) {
      log.error("ViewModel is null when creating main content");
      VBox errorContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
      errorContainer.getChildren().add(UIFactory.createMutedLabel("Error: Unable to load knowledge base view"));
      return errorContainer;
    }
    
    VBox mainContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    mainContent.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    LayoutHelper.setVGrow(mainContent);

    // Search section
    mainContent.getChildren().add(createSearchSection());

    // Categories section
    mainContent.getChildren().add(createCategoriesSection());

    // Recent articles
    mainContent.getChildren().add(createRecentArticlesSection());

    return mainContent;
  }

  private VBox createSearchSection() {
    VBox section = UIFactory.createCard();
    section.setPadding(LayoutConstants.PADDING_SPACIOUS);

    Label searchTitle = UIFactory.createSectionTitle(I18n.get("kb.search.title"));

    HBox searchBox = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_MD);

    searchField = new TextField();
    searchField.setPromptText(I18n.get("kb.search.placeholder"));
    searchField.getStyleClass().addAll(StyleConstants.SEARCH_INPUT);
    if (viewModel != null && viewModel.searchKeywordProperty() != null) {
      searchField.textProperty().bindBidirectional(viewModel.searchKeywordProperty());
    }
    LayoutHelper.setHGrow(searchField);

    Button searchButton = UIFactory.createPrimaryButton(
        I18n.get("kb.search.button"), 
        () -> {
          if (viewModel != null) {
            viewModel.searchArticles();
          }
        }
    );
    searchButton.setGraphic(new FontIcon(Octicons.SEARCH_24));

    searchField.setOnAction(e -> {
      if (viewModel != null) {
        viewModel.searchArticles();
      }
    });

    searchBox.getChildren().addAll(searchField, searchButton);

    // Search results area
    searchResults = LayoutHelper.createVBox(LayoutConstants.SPACING_SM);
    searchResults.getStyleClass().add("search-results");

    section.getChildren().addAll(searchTitle, searchBox, searchResults);
    return section;
  }

  private VBox createCategoriesSection() {
    VBox section = UIFactory.createCard();
    section.setPadding(LayoutConstants.PADDING_DEFAULT);

    Label categoriesTitle = UIFactory.createSectionTitle("Browse by Category");

    // Categories grid
    VBox categoriesGrid = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);

    HBox row1 = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_LG);
    row1.getChildren()
        .addAll(
            createCategoryCard(
                "Development Guidelines",
                "Best practices, coding standards, and development workflows",
                Octicons.CODE_24,
                "12 articles"),
            createCategoryCard(
                "System Architecture",
                "Design patterns, architecture decisions, and technical documentation",
                Octicons.PACKAGE_24,
                "8 articles"),
            createCategoryCard(
                "Database Guide",
                "Schema design, query optimization, and maintenance procedures",
                Octicons.DATABASE_24,
                "15 articles"));

    HBox row2 = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_LG);
    row2.getChildren()
        .addAll(
            createCategoryCard(
                "Security Policies",
                "Security guidelines, compliance requirements, and procedures",
                Octicons.SHIELD_24,
                "6 articles"),
            createCategoryCard(
                "Deployment",
                "CI/CD processes, deployment guides, and environment management",
                Octicons.UPLOAD_24,
                "10 articles"),
            createCategoryCard(
                "Troubleshooting",
                "Common issues, debugging guides, and solution documentation",
                Octicons.TOOLS_24,
                "22 articles"));

    categoriesGrid.getChildren().addAll(row1, row2);
    section.getChildren().addAll(categoriesTitle, categoriesGrid);
    return section;
  }

  private VBox createCategoryCard(
      String title, String description, Octicons icon, String articleCount) {
    VBox card = UIFactory.createCard();
    card.getStyleClass().addAll("category-card", "interactive-card");
    card.setPadding(LayoutConstants.PADDING_DEFAULT);
    card.setAlignment(Pos.TOP_LEFT);
    LayoutHelper.setHGrow(card);
    card.setPrefHeight(120);

    HBox header = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_MD);

    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(LayoutConstants.ICON_SIZE_MD);
    iconNode.getStyleClass().add("category-icon");

    Label titleLabel = UIFactory.createBoldLabel(title);
    titleLabel.getStyleClass().add("category-title");

    header.getChildren().addAll(iconNode, titleLabel);

    Label descriptionLabel = UIFactory.createMutedLabel(description);
    descriptionLabel.getStyleClass().add(Styles.TEXT_SMALL);
    descriptionLabel.setWrapText(true);

    Label countLabel = new Label(articleCount);
    countLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "accent-text");

    card.getChildren().addAll(header, descriptionLabel, countLabel);

    card.setOnMouseClicked(
        e -> {
          log.info("Category clicked: {}", title);
          if (viewModel != null) {
            viewModel.filterByCategory(title);
          }
        });

    return card;
  }

  private VBox createRecentArticlesSection() {
    VBox section = UIFactory.createCard();
    section.setPadding(LayoutConstants.PADDING_DEFAULT);

    HBox headerRow = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_SM);

    Label recentTitle = UIFactory.createSectionTitle("Recently Updated");

    Region spacer = UIFactory.createHorizontalSpacer();

    Button viewAllButton = UIFactory.createSecondaryButton("View All", () -> {});
    viewAllButton.getStyleClass().add(Styles.SMALL);

    headerRow.getChildren().addAll(recentTitle, spacer, viewAllButton);

    VBox articlesList = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    articlesList
        .getChildren()
        .addAll(
            createArticleItem(
                "Database Migration Best Practices",
                "Updated procedures for safe database migrations",
                "2 hours ago",
                Octicons.DATABASE_24),
            createArticleItem(
                "New Security Guidelines",
                "Updated security policies and implementation guidelines",
                "1 day ago",
                Octicons.SHIELD_24),
            createArticleItem(
                "Code Review Checklist",
                "Comprehensive checklist for thorough code reviews",
                "3 days ago",
                Octicons.CHECK_CIRCLE_24),
            createArticleItem(
                "Performance Monitoring Guide",
                "How to effectively monitor and optimize system performance",
                "1 week ago",
                Octicons.PULSE_24));

    section.getChildren().addAll(headerRow, articlesList);
    return section;
  }

  private HBox createArticleItem(String title, String description, String updated, Octicons icon) {
    HBox item = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_LG);
    item.getStyleClass().addAll("article-item", "interactive-item");
    item.setPadding(LayoutConstants.PADDING_DEFAULT);

    FontIcon iconNode = new FontIcon(icon);
    iconNode.setIconSize(LayoutConstants.ICON_SIZE_SM);
    iconNode.getStyleClass().add("article-icon");

    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    LayoutHelper.setHGrow(content);

    Label titleLabel = UIFactory.createBoldLabel(title);
    titleLabel.getStyleClass().add("article-title");

    Label descriptionLabel = UIFactory.createMutedLabel(description);
    descriptionLabel.getStyleClass().add(Styles.TEXT_SMALL);

    content.getChildren().addAll(titleLabel, descriptionLabel);

    Label updatedLabel = UIFactory.createMutedLabel(updated);
    updatedLabel.getStyleClass().add(Styles.TEXT_SMALL);

    item.getChildren().addAll(iconNode, content, updatedLabel);

    item.setOnMouseClicked(
        e -> {
          log.info("Article clicked: {}", title);
          // Navigate to article detail view
        });

    return item;
  }

  @Override
  public void onActivate() {
    super.onActivate();
    if (searchField != null) {
      searchField.requestFocus();
    }
  }
}
