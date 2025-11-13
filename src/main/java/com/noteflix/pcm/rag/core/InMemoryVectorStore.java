package com.noteflix.pcm.rag.core;

import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory vector store for testing.
 *
 * <p>Simple implementation that stores documents in memory. Good for unit tests and prototyping.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class InMemoryVectorStore implements VectorStore {

  private final Map<String, RAGDocument> documents = new ConcurrentHashMap<>();

  public InMemoryVectorStore() {
    log.info("InMemoryVectorStore initialized");
  }

  @Override
  public void indexDocument(RAGDocument document) {
    documents.put(document.getId(), document);
    log.debug("Indexed document: {}", document.getId());
  }

  @Override
  public void indexDocuments(List<RAGDocument> docs) {
    for (RAGDocument doc : docs) {
      documents.put(doc.getId(), doc);
    }
    log.info("Indexed {} documents", docs.size());
  }

  @Override
  public List<ScoredDocument> search(String query, RetrievalOptions options) {
    List<ScoredDocument> results = new ArrayList<>();
    String queryLower = query.toLowerCase();
    int rank = 1;

    // Simple keyword matching
    for (RAGDocument doc : documents.values()) {
      // Type filter
      if (options.getTypes() != null
          && !options.getTypes().isEmpty()
          && !options.getTypes().contains(doc.getType())) {
        continue;
      }

      // Metadata filters
      if (options.getFilters() != null) {
        boolean matchesFilters = true;
        for (Map.Entry<String, String> filter : options.getFilters().entrySet()) {
          String metaValue = doc.getMetadata(filter.getKey());
          if (!filter.getValue().equals(metaValue)) {
            matchesFilters = false;
            break;
          }
        }
        if (!matchesFilters) {
          continue;
        }
      }

      // Calculate simple relevance score
      String contentLower = doc.getContent().toLowerCase();
      double score = calculateScore(contentLower, queryLower);

      if (score >= options.getMinScore()) {
        String snippet = extractSnippet(doc.getContent(), query, options.isIncludeSnippets());

        ScoredDocument scoredDoc =
            ScoredDocument.builder()
                .document(doc)
                .score(score)
                .rank(rank++)
                .snippet(snippet)
                .build();

        results.add(scoredDoc);
      }
    }

    // Sort by score (descending)
    results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

    // Limit results
    if (results.size() > options.getMaxResults()) {
      results = results.subList(0, options.getMaxResults());
    }

    // Update ranks
    for (int i = 0; i < results.size(); i++) {
      results.get(i).setRank(i + 1);
    }

    log.debug("Search for '{}' returned {} results", query, results.size());
    return results;
  }

  @Override
  public void deleteDocument(String documentId) {
    documents.remove(documentId);
    log.debug("Deleted document: {}", documentId);
  }

  @Override
  public void deleteDocuments(List<String> documentIds) {
    for (String id : documentIds) {
      documents.remove(id);
    }
    log.info("Deleted {} documents", documentIds.size());
  }

  @Override
  public void clear() {
    documents.clear();
    log.info("Cleared all documents");
  }

  @Override
  public long getDocumentCount() {
    return documents.size();
  }

  @Override
  public boolean exists(String documentId) {
    return documents.containsKey(documentId);
  }

  @Override
  public RAGDocument getDocument(String documentId) {
    return documents.get(documentId);
  }

  @Override
  public void close() {
    clear();
    log.info("InMemoryVectorStore closed");
  }

  // ========== Helper Methods ==========

  private double calculateScore(String content, String query) {
    // Simple TF scoring
    String[] queryTerms = query.split("\\s+");
    int matches = 0;

    for (String term : queryTerms) {
      if (content.contains(term)) {
        // Count occurrences
        int count = (content.length() - content.replace(term, "").length()) / term.length();
        matches += count;
      }
    }

    // Normalize by document length
    double score = (double) matches / Math.max(1, content.length() / 100);
    return Math.min(1.0, score);
  }

  private String extractSnippet(String content, String query, boolean include) {
    if (!include || content == null || content.isEmpty()) {
      return null;
    }

    int snippetLength = 200;
    if (content.length() <= snippetLength) {
      return content;
    }

    // Try to find query in content
    String lowerContent = content.toLowerCase();
    String lowerQuery = query.toLowerCase();
    int index = lowerContent.indexOf(lowerQuery);

    if (index >= 0) {
      int start = Math.max(0, index - 50);
      int end = Math.min(content.length(), index + query.length() + 150);
      return "..." + content.substring(start, end) + "...";
    }

    return content.substring(0, snippetLength) + "...";
  }
}
