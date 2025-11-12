package com.noteflix.pcm.ui.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * ViewModel for Knowledge Base Page
 *
 * <p>Manages knowledge base search and content state
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class KnowledgeBaseViewModel extends BaseViewModel {

  // Search state
  private final StringProperty searchQuery = new SimpleStringProperty("");
  private final ObservableList<KnowledgeArticle> searchResults = FXCollections.observableArrayList();
  private final ObservableList<String> categories = FXCollections.observableArrayList();
  private final ObservableList<KnowledgeArticle> recentArticles = FXCollections.observableArrayList();

  /** Constructor */
  public KnowledgeBaseViewModel() {
    loadInitialData();
    log.info("KnowledgeBaseViewModel initialized");
  }

  /** Load initial data */
  private void loadInitialData() {
    // Load categories
    categories.addAll(
        "System Architecture",
        "Database Schema",
        "API Documentation",
        "Business Processes",
        "Troubleshooting");

    // Load recent articles (mock data)
    recentArticles.add(
        new KnowledgeArticle(
            "Getting Started Guide", "System Architecture", "Introduction to the system"));
    recentArticles.add(
        new KnowledgeArticle("Database Schema", "Database Schema", "Database structure overview"));
    recentArticles.add(
        new KnowledgeArticle(
            "API Reference", "API Documentation", "Complete API documentation"));

    log.debug("Loaded {} categories and {} recent articles", categories.size(), recentArticles.size());
  }

  /** Perform search */
  public void search() {
    String query = searchQuery.get();
    if (query == null || query.trim().isEmpty()) {
      searchResults.clear();
      return;
    }

    setBusy(true);
    clearError();

    // TODO: Implement actual search
    // For now, filter recent articles
    searchResults.clear();
    recentArticles.stream()
        .filter(
            article ->
                article.title.toLowerCase().contains(query.toLowerCase())
                    || article.content.toLowerCase().contains(query.toLowerCase()))
        .forEach(searchResults::add);

    setBusy(false);
    log.info("Search completed: {} results for query '{}'", searchResults.size(), query);
  }

  /** Clear search */
  public void clearSearch() {
    searchQuery.set("");
    searchResults.clear();
  }

  // Property accessors
  public StringProperty searchQueryProperty() {
    return searchQuery;
  }

  public String getSearchQuery() {
    return searchQuery.get();
  }

  public void setSearchQuery(String query) {
    searchQuery.set(query);
  }

  public ObservableList<KnowledgeArticle> getSearchResults() {
    return searchResults;
  }

  public ObservableList<String> getCategories() {
    return categories;
  }

  public ObservableList<KnowledgeArticle> getRecentArticles() {
    return recentArticles;
  }

  /** Knowledge Article model */
  public static class KnowledgeArticle {
    public final String title;
    public final String category;
    public final String content;

    public KnowledgeArticle(String title, String category, String content) {
      this.title = title;
      this.category = category;
      this.content = content;
    }
  }
}

