package com.noteflix.pcm.rag.core;

import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lucene-based vector store (100% offline).
 *
 * Uses Apache Lucene for full-text search with BM25 ranking.
 * No external dependencies, runs completely offline.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LuceneVectorStore implements VectorStore {

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriterConfig config;
    private IndexWriter writer;
    private SearcherManager searcherManager;

    // Field names
    private static final String FIELD_ID = "id";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_SOURCE_PATH = "sourcePath";
    private static final String FIELD_INDEXED_AT = "indexedAt";
    private static final String FIELD_METADATA_PREFIX = "meta_";

    public LuceneVectorStore(String indexPath) throws IOException {
        Path path = Paths.get(indexPath);
        Files.createDirectories(path);

        this.directory = FSDirectory.open(path);
        this.analyzer = new StandardAnalyzer();
        this.config = new IndexWriterConfig(analyzer);
        this.config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        initializeWriter();

        log.info("Lucene vector store initialized at: {}", indexPath);
    }

    private void initializeWriter() throws IOException {
        this.writer = new IndexWriter(directory, config);
        this.searcherManager = new SearcherManager(writer, null);
    }

    @Override
    public void indexDocument(RAGDocument document) {
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
            throw new RuntimeException("Failed to index document", e);
        }
    }

    @Override
    public void indexDocuments(List<RAGDocument> documents) {
        try {
            for (RAGDocument doc : documents) {
                Document luceneDoc = convertToLuceneDocument(doc);
                Term idTerm = new Term(FIELD_ID, doc.getId());
                writer.updateDocument(idTerm, luceneDoc);
            }

            writer.commit();
            searcherManager.maybeRefresh();

            log.info("Indexed {} documents", documents.size());

        } catch (IOException e) {
            log.error("Failed to batch index documents", e);
            throw new RuntimeException("Failed to batch index", e);
        }
    }

    @Override
    public List<ScoredDocument> search(String query, RetrievalOptions options) {
        try {
            IndexSearcher searcher = searcherManager.acquire();

            try {
                // Build query
                Query luceneQuery = buildQuery(query, options);

                // Search
                TopDocs topDocs = searcher.search(luceneQuery, options.getMaxResults());

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

                    String snippet = options.isIncludeSnippets()
                        ? extractSnippet(ragDoc.getContent(), query)
                        : null;

                    ScoredDocument scoredDoc = ScoredDocument.builder()
                        .document(ragDoc)
                        .score(normalizedScore)
                        .rank(rank++)
                        .snippet(snippet)
                        .build();

                    results.add(scoredDoc);
                }

                log.debug("Search for '{}' returned {} results", query, results.size());
                return results;

            } finally {
                searcherManager.release(searcher);
            }

        } catch (Exception e) {
            log.error("Search failed for query: {}", query, e);
            throw new RuntimeException("Search failed", e);
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        try {
            Term idTerm = new Term(FIELD_ID, documentId);
            writer.deleteDocuments(idTerm);
            writer.commit();
            searcherManager.maybeRefresh();

            log.debug("Deleted document: {}", documentId);

        } catch (IOException e) {
            log.error("Failed to delete document: {}", documentId, e);
            throw new RuntimeException("Failed to delete document", e);
        }
    }

    @Override
    public void deleteDocuments(List<String> documentIds) {
        try {
            Term[] terms = documentIds.stream()
                .map(id -> new Term(FIELD_ID, id))
                .toArray(Term[]::new);

            writer.deleteDocuments(terms);
            writer.commit();
            searcherManager.maybeRefresh();

            log.info("Deleted {} documents", documentIds.size());

        } catch (IOException e) {
            log.error("Failed to delete documents", e);
            throw new RuntimeException("Failed to delete documents", e);
        }
    }

    @Override
    public void clear() {
        try {
            writer.deleteAll();
            writer.commit();
            searcherManager.maybeRefresh();

            log.info("Cleared all documents");

        } catch (IOException e) {
            log.error("Failed to clear index", e);
            throw new RuntimeException("Failed to clear index", e);
        }
    }

    @Override
    public long getDocumentCount() {
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                return searcher.getIndexReader().numDocs();
            } finally {
                searcherManager.release(searcher);
            }
        } catch (IOException e) {
            log.error("Failed to get document count", e);
            return 0;
        }
    }

    @Override
    public boolean exists(String documentId) {
        return getDocument(documentId) != null;
    }

    @Override
    public RAGDocument getDocument(String documentId) {
        try {
            IndexSearcher searcher = searcherManager.acquire();

            try {
                TermQuery query = new TermQuery(new Term(FIELD_ID, documentId));
                TopDocs topDocs = searcher.search(query, 1);

                if (topDocs.totalHits.value > 0) {
                    Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
                    return convertFromLuceneDocument(doc);
                }

                return null;

            } finally {
                searcherManager.release(searcher);
            }

        } catch (IOException e) {
            log.error("Failed to get document: {}", documentId, e);
            return null;
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
                doc.add(new StringField(
                    FIELD_METADATA_PREFIX + entry.getKey(),
                    entry.getValue(),
                    Field.Store.YES
                ));
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

    private Query buildQuery(String queryString, RetrievalOptions options) throws Exception {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        // Main content query
        QueryParser parser = new QueryParser(FIELD_CONTENT, analyzer);
        Query contentQuery = parser.parse(QueryParser.escape(queryString));
        builder.add(contentQuery, BooleanClause.Occur.MUST);

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
                TermQuery filterQuery = new TermQuery(
                    new Term(FIELD_METADATA_PREFIX + filter.getKey(), filter.getValue())
                );
                builder.add(filterQuery, BooleanClause.Occur.FILTER);
            }
        }

        return builder.build();
    }

    private double normalizeScore(float score) {
        // Simple normalization (can be improved)
        return Math.min(1.0, score / 10.0);
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

