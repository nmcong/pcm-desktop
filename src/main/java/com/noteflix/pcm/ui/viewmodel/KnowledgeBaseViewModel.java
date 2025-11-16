package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.core.i18n.I18n;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KnowledgeBaseViewModel extends BaseViewModel {

    public final StringProperty searchKeyword = new SimpleStringProperty("");
    public final StringProperty selectedCategory =
            new SimpleStringProperty(I18n.get("kb.category.all"));

    public KnowledgeBaseViewModel() {
        log.debug("KnowledgeBaseViewModel initialized");
    }

    public void searchArticles() {
        setBusy(true);
        clearError();
        log.info(
                "Searching knowledge base for: '{}' in category: '{}'",
                searchKeyword.get(),
                selectedCategory.get());

        runAsync(
                () -> {
                    Thread.sleep(1000); // Simulate network/DB call
                    if (searchKeyword.get().contains("error")) {
                        throw new RuntimeException("Failed to search articles.");
                    }
                    return "Search results for " + searchKeyword.get();
                },
                result -> {
                    log.info("Search completed: {}", result);
                    // Update observable list of articles (not implemented in this simple VM)
                },
                error -> {
                    setError("Search failed: " + error.getMessage(), error);
                    log.error("Error during knowledge base search", error);
                })
                .whenComplete((r, ex) -> setBusy(false));
    }

    public void filterByCategory(String category) {
        setSelectedCategory(category);
        searchArticles(); // Re-run search with new filter
    }

    public String getSearchKeyword() {
        return searchKeyword.get();
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword.set(searchKeyword);
    }

    public StringProperty searchKeywordProperty() {
        return searchKeyword;
    }

    public String getSelectedCategory() {
        return selectedCategory.get();
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory.set(selectedCategory);
    }

    public StringProperty selectedCategoryProperty() {
        return selectedCategory;
    }
}
