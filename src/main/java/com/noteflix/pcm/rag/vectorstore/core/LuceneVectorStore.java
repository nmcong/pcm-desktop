package com.noteflix.pcm.rag.vectorstore.core;

import com.noteflix.pcm.rag.vectorstore.api.VectorStore;
import com.noteflix.pcm.rag.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Custom exception for VectorStore operations.
 */
class VectorStoreException extends RuntimeException {
  public VectorStoreException(String message) {
    super(message);
  }
  
  public VectorStoreException(String message, Throwable cause) {
    super(message, cause);
  }
}

/**
 * Lucene-based vector store (100% offline).
 *
 * <p>Uses Apache Lucene for full-text search with BM25 ranking. No external dependencies, runs
 * completely offline.
 *
 * @author PCM Team
 * @version 1.1.0
 */
@Slf4j
public class LuceneVectorStore implements VectorStore {

  private final Directory directory;
  private final Analyzer analyzer;
  private final IndexWriterConfig config;
  private IndexWriter writer;
  private SearcherManager searcherManager;
  private volatile double maxScoreSeen = 1.0; // For dynamic score normalization

  // Field names
  private static final String FIELD_ID = "id";
  private static final String FIELD_CONTENT = "content";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_SOURCE_PATH = "sourcePath";
  private static final String FIELD_INDEXED_AT = "indexedAt";
  private static final String FIELD_METADATA_PREFIX = "meta_";

  public LuceneVectorStore(String indexPath) throws VectorStoreException {
    if (indexPath == null || indexPath.trim().isEmpty()) {
      throw new VectorStoreException("Index path cannot be null or empty");
    }
    
    try {
      Path path = Paths.get(indexPath);
      Files.createDirectories(path);

      this.directory = FSDirectory.open(path);
      this.analyzer = new StandardAnalyzer();
      this.config = new IndexWriterConfig(analyzer);
      this.config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

      initializeWriter();

      log.info("Lucene vector store initialized at: {}", indexPath);
    } catch (IOException e) {
      throw new VectorStoreException("Failed to initialize Lucene vector store at: " + indexPath, e);
    }
  }

  private void initializeWriter() throws IOException {
    this.writer = new IndexWriter(directory, config);
    this.searcherManager = new SearcherManager(writer, null);
  }

  @Override
  public void indexDocument(RAGDocument document) {
    if (document == null) {
      throw new VectorStoreException("Document cannot be null");
    }
    if (document.getId() == null || document.getId().trim().isEmpty()) {
      throw new VectorStoreException("Document ID cannot be null or empty");
    }
    if (document.getContent() == null) {
      throw new VectorStoreException("Document content cannot be null");
    }
    
    try {
      Document luceneDoc = convertToLuceneDocument(document);

      // Update if exists, otherwise add new
      Term idTerm = new Term(FIELD_ID, document.getId());
      writer.updateDocument(idTerm, luceneDoc);
      writer.commit();

      searcherManager.maybeRefresh();

      log.debug("Indexed document: {}", document.getId());

    } catch (IOException e) {
      log.error("Failed to index document: {}", document.getId(), e);
      throw new VectorStoreException("Failed to index document: " + document.getId(), e);
    }
  }

  @Override
  public void indexDocuments(List<RAGDocument> documents) {
    if (documents == null) {
      throw new VectorStoreException("Documents list cannot be null");
    }
    if (documents.isEmpty()) {
      log.debug("Empty documents list provided, skipping indexing");
      return;
    }
    
    try {
      for (RAGDocument doc : documents) {
        if (doc == null) {
          throw new VectorStoreException("Document in list cannot be null");
        }
        if (doc.getId() == null || doc.getId().trim().isEmpty()) {
          throw new VectorStoreException("Document ID cannot be null or empty");
        }
        if (doc.getContent() == null) {
          throw new VectorStoreException("Document content cannot be null");
        }
        
        Document luceneDoc = convertToLuceneDocument(doc);
        Term idTerm = new Term(FIELD_ID, doc.getId());
        writer.updateDocument(idTerm, luceneDoc);
      }

      writer.commit();
      searcherManager.maybeRefresh();

      log.info("Indexed {} documents", documents.size());

    } catch (IOException e) {
      log.error("Failed to batch index documents", e);
      throw new VectorStoreException("Failed to batch index documents", e);
    }
  }

  @Override
  public List<ScoredDocument> search(String query, RetrievalOptions options) {
    if (query == null || query.trim().isEmpty()) {
      throw new VectorStoreException("Query cannot be null or empty");
    }
    if (options == null) {
      throw new VectorStoreException("RetrievalOptions cannot be null");
    }
    
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();

      // Build query
      Query luceneQuery = buildQuery(query, options);

      // Search
      TopDocs topDocs = searcher.search(luceneQuery, options.getMaxResults());

      // Update max score for dynamic normalization
      if (topDocs.scoreDocs.length > 0) {
        float currentMaxScore = topDocs.scoreDocs[0].score;
        if (currentMaxScore > maxScoreSeen) {
          maxScoreSeen = currentMaxScore;
        }
      }

      // Convert results
      List<ScoredDocument> results = new ArrayList<>();
      int rank = 1;

      for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
        // Normalize score to 0-1 range
        double normalizedScore = normalizeScore(scoreDoc.score);

        if (normalizedScore < options.getMinScore()) {
          continue;
        }

        Document doc = searcher.doc(scoreDoc.doc);
        RAGDocument ragDoc = convertFromLuceneDocument(doc);

        String snippet =
            options.isIncludeSnippets() ? extractSnippet(ragDoc.getContent(), query) : null;

        ScoredDocument scoredDoc =
            ScoredDocument.builder()
                .document(ragDoc)
                .score(normalizedScore)
                .rank(rank++)
                .snippet(snippet)
                .build();

        results.add(scoredDoc);
      }

      log.debug("Search for '{}' returned {} results", query, results.size());
      return results;

    } catch (ParseException e) {
      log.error("Query parsing failed for: {}", query, e);
      throw new VectorStoreException("Invalid query syntax: " + query, e);
    } catch (IOException e) {
      log.error("Search failed for query: {}", query, e);
      throw new VectorStoreException("Search operation failed", e);
    } catch (Exception e) {
      log.error("Unexpected error during search: {}", query, e);
      throw new VectorStoreException("Search failed unexpectedly", e);
    } finally {
      if (searcher != null) {
        try {
          searcherManager.release(searcher);
        } catch (IOException e) {
          log.warn("Failed to release searcher", e);
        }
      }
    }
  }

  @Override
  public void deleteDocument(String documentId) {
    if (documentId == null || documentId.trim().isEmpty()) {
      throw new VectorStoreException("Document ID cannot be null or empty");
    }
    
    try {
      Term idTerm = new Term(FIELD_ID, documentId);
      writer.deleteDocuments(idTerm);
      writer.commit();
      searcherManager.maybeRefresh();

      log.debug("Deleted document: {}", documentId);

    } catch (IOException e) {
      log.error("Failed to delete document: {}", documentId, e);
      throw new VectorStoreException("Failed to delete document: " + documentId, e);
    }
  }

  @Override
  public void deleteDocuments(List<String> documentIds) {
    if (documentIds == null) {
      throw new VectorStoreException("Document IDs list cannot be null");
    }
    if (documentIds.isEmpty()) {
      log.debug("Empty document IDs list provided, skipping deletion");
      return;
    }
    
    for (String id : documentIds) {
      if (id == null || id.trim().isEmpty()) {
        throw new VectorStoreException("Document ID in list cannot be null or empty");
      }
    }
    
    try {
      Term[] terms = documentIds.stream().map(id -> new Term(FIELD_ID, id)).toArray(Term[]::new);

      writer.deleteDocuments(terms);
      writer.commit();
      searcherManager.maybeRefresh();

      log.info("Deleted {} documents", documentIds.size());

    } catch (IOException e) {
      log.error("Failed to delete documents", e);
      throw new VectorStoreException("Failed to delete documents", e);
    }
  }

  @Override
  public void clear() {
    try {
      writer.deleteAll();
      writer.commit();
      searcherManager.maybeRefresh();
      // Reset score normalization
      maxScoreSeen = 1.0;

      log.info("Cleared all documents");

    } catch (IOException e) {
      log.error("Failed to clear index", e);
      throw new VectorStoreException("Failed to clear index", e);
    }
  }

  @Override
  public long getDocumentCount() {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      return searcher.getIndexReader().numDocs();
    } catch (IOException e) {
      log.error("Failed to get document count", e);
      return 0;
    } finally {
      if (searcher != null) {
        try {
          searcherManager.release(searcher);
        } catch (IOException e) {
          log.warn("Failed to release searcher", e);
        }
      }
    }
  }

  @Override
  public boolean exists(String documentId) {
    return getDocument(documentId) != null;
  }

  @Override
  public RAGDocument getDocument(String documentId) {
    if (documentId == null || documentId.trim().isEmpty()) {
      throw new VectorStoreException("Document ID cannot be null or empty");
    }
    
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();

      TermQuery query = new TermQuery(new Term(FIELD_ID, documentId));
      TopDocs topDocs = searcher.search(query, 1);

      if (topDocs.totalHits.value > 0) {
        Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
        return convertFromLuceneDocument(doc);
      }

      return null;

    } catch (IOException e) {
      log.error("Failed to get document: {}", documentId, e);
      throw new VectorStoreException("Failed to retrieve document: " + documentId, e);
    } finally {
      if (searcher != null) {
        try {
          searcherManager.release(searcher);
        } catch (IOException e) {
          log.warn("Failed to release searcher", e);
        }
      }
    }
  }

  @Override
  public void close() {
    try {
      if (searcherManager != null) {
        searcherManager.close();
      }
      if (writer != null) {
        writer.close();
      }
      if (directory != null) {
        directory.close();
      }
      log.info("Lucene vector store closed");
    } catch (IOException e) {
      log.error("Error closing Lucene vector store", e);
    }
  }

  // ========== Helper Methods ==========

  private Document convertToLuceneDocument(RAGDocument ragDoc) {
    Document doc = new Document();

    // ID (stored, not indexed for search)
    doc.add(new StringField(FIELD_ID, ragDoc.getId(), Field.Store.YES));

    // Content (indexed and stored)
    doc.add(new TextField(FIELD_CONTENT, ragDoc.getContent(), Field.Store.YES));

    // Type
    doc.add(new StringField(FIELD_TYPE, ragDoc.getType().name(), Field.Store.YES));

    // Title
    if (ragDoc.getTitle() != null) {
      doc.add(new TextField(FIELD_TITLE, ragDoc.getTitle(), Field.Store.YES));
    }

    // Source path
    if (ragDoc.getSourcePath() != null) {
      doc.add(new StringField(FIELD_SOURCE_PATH, ragDoc.getSourcePath(), Field.Store.YES));
    }

    // Indexed at
    if (ragDoc.getIndexedAt() != null) {
      doc.add(new StringField(FIELD_INDEXED_AT, ragDoc.getIndexedAt().toString(), Field.Store.YES));
    }

    // Metadata
    if (ragDoc.getMetadata() != null) {
      for (Map.Entry<String, String> entry : ragDoc.getMetadata().entrySet()) {
        doc.add(
            new StringField(
                FIELD_METADATA_PREFIX + entry.getKey(), entry.getValue(), Field.Store.YES));
      }
    }

    return doc;
  }

  private RAGDocument convertFromLuceneDocument(Document doc) {
    Map<String, String> metadata = new HashMap<>();

    // Extract metadata
    for (IndexableField field : doc.getFields()) {
      String fieldName = field.name();
      if (fieldName.startsWith(FIELD_METADATA_PREFIX)) {
        String key = fieldName.substring(FIELD_METADATA_PREFIX.length());
        metadata.put(key, field.stringValue());
      }
    }

    LocalDateTime indexedAt = null;
    String indexedAtStr = doc.get(FIELD_INDEXED_AT);
    if (indexedAtStr != null) {
      try {
        indexedAt = LocalDateTime.parse(indexedAtStr);
      } catch (Exception e) {
        log.warn("Failed to parse indexed_at: {}", indexedAtStr);
      }
    }

    return RAGDocument.builder()
        .id(doc.get(FIELD_ID))
        .content(doc.get(FIELD_CONTENT))
        .type(DocumentType.valueOf(doc.get(FIELD_TYPE)))
        .title(doc.get(FIELD_TITLE))
        .sourcePath(doc.get(FIELD_SOURCE_PATH))
        .indexedAt(indexedAt)
        .metadata(metadata)
        .build();
  }

  private Query buildQuery(String queryString, RetrievalOptions options) throws ParseException {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    // Main content query with improved parsing
    QueryParser parser = new QueryParser(FIELD_CONTENT, analyzer);
    parser.setDefaultOperator(QueryParser.Operator.OR); // More flexible search
    
    Query contentQuery;
    try {
      // Try parsing as-is first for advanced queries
      contentQuery = parser.parse(queryString);
    } catch (ParseException e) {
      // Fallback to escaped query for simple searches
      try {
        contentQuery = parser.parse(QueryParser.escape(queryString));
      } catch (ParseException fallbackException) {
        // Last resort: simple term query
        contentQuery = new TermQuery(new Term(FIELD_CONTENT, queryString.toLowerCase()));
        log.warn("Query parsing failed, using simple term query for: {}", queryString);
      }
    }
    
    builder.add(contentQuery, BooleanClause.Occur.MUST);

    // Also search in title field for better relevance
    try {
      QueryParser titleParser = new QueryParser(FIELD_TITLE, analyzer);
      Query titleQuery = titleParser.parse(QueryParser.escape(queryString));
      builder.add(titleQuery, BooleanClause.Occur.SHOULD);
    } catch (ParseException e) {
      // Ignore title search if parsing fails
      log.debug("Title query parsing failed for: {}", queryString);
    }

    // Type filters
    if (options.getTypes() != null && !options.getTypes().isEmpty()) {
      BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder();
      for (DocumentType type : options.getTypes()) {
        TermQuery typeQuery = new TermQuery(new Term(FIELD_TYPE, type.name()));
        typeBuilder.add(typeQuery, BooleanClause.Occur.SHOULD);
      }
      builder.add(typeBuilder.build(), BooleanClause.Occur.FILTER);
    }

    // Metadata filters
    if (options.getFilters() != null) {
      for (Map.Entry<String, String> filter : options.getFilters().entrySet()) {
        TermQuery filterQuery =
            new TermQuery(new Term(FIELD_METADATA_PREFIX + filter.getKey(), filter.getValue()));
        builder.add(filterQuery, BooleanClause.Occur.FILTER);
      }
    }

    return builder.build();
  }

  private double normalizeScore(float score) {
    // Dynamic normalization based on observed maximum score
    if (maxScoreSeen <= 0) {
      return 0.0;
    }
    return Math.min(1.0, score / maxScoreSeen);
  }

  private String extractSnippet(String content, String query) {
    if (content == null || content.isEmpty()) {
      return "";
    }

    // Simple snippet extraction (can be improved with Lucene Highlighter)
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

    // Otherwise return beginning
    return content.substring(0, snippetLength) + "...";
  }
}
