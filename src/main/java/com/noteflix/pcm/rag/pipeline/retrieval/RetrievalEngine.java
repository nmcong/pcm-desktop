package com.noteflix.pcm.rag.pipeline.retrieval;

import com.noteflix.pcm.rag.model.RetrievalOptions;
import com.noteflix.pcm.rag.model.ScoredDocument;
import com.noteflix.pcm.rag.vectorstore.api.VectorStore;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Advanced retrieval engine with reranking and filtering.
 *
 * <p>Features: - Query expansion - Result reranking - Diversity-based filtering - Score
 * normalization
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class RetrievalEngine {

    private final VectorStore vectorStore;
    private final boolean enableQueryExpansion;
    private final boolean enableReranking;

    public RetrievalEngine(VectorStore vectorStore) {
        this(vectorStore, false, false);
    }

    public RetrievalEngine(
            VectorStore vectorStore, boolean enableQueryExpansion, boolean enableReranking) {
        this.vectorStore = vectorStore;
        this.enableQueryExpansion = enableQueryExpansion;
        this.enableReranking = enableReranking;
    }

    /**
     * Retrieve documents for query.
     *
     * @param query   Search query
     * @param options Retrieval options
     * @return Ranked list of documents
     */
    public List<ScoredDocument> retrieve(String query, RetrievalOptions options) {
        log.debug(
                "Retrieving documents for query: '{}' with {} max results", query, options.getMaxResults());

        // 1. Optionally expand query
        List<String> queries =
                enableQueryExpansion ? expandQuery(query) : Collections.singletonList(query);

        // 2. Retrieve from vector store
        List<ScoredDocument> results = new ArrayList<>();
        for (String q : queries) {
            List<ScoredDocument> batch = vectorStore.search(q, options);
            results.addAll(batch);
        }

        // 3. Deduplicate by document ID
        results = deduplicateResults(results);

        // 4. Optionally rerank
        if (enableReranking) {
            results = rerank(query, results);
        }

        // 5. Apply diversity filtering
        results = applyDiversityFilter(results, options);

        // 6. Limit results
        if (results.size() > options.getMaxResults()) {
            results = results.subList(0, options.getMaxResults());
        }

        // 7. Update ranks
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }

        log.debug("Retrieved {} documents", results.size());
        return results;
    }

    /**
     * Expand query with synonyms and variations.
     */
    private List<String> expandQuery(String query) {
        List<String> expanded = new ArrayList<>();
        expanded.add(query);

        // Simple expansion: add variations
        // TODO: Use thesaurus or word embeddings for better expansion

        // Add lowercase variant
        if (!query.equals(query.toLowerCase())) {
            expanded.add(query.toLowerCase());
        }

        // Add common variations
        if (query.contains("validate")) {
            expanded.add(query.replace("validate", "check"));
            expanded.add(query.replace("validate", "verify"));
        }
        if (query.contains("customer")) {
            expanded.add(query.replace("customer", "user"));
            expanded.add(query.replace("customer", "client"));
        }

        return expanded;
    }

    /**
     * Deduplicate results by document ID.
     */
    private List<ScoredDocument> deduplicateResults(List<ScoredDocument> results) {
        Map<String, ScoredDocument> uniqueResults = new LinkedHashMap<>();

        for (ScoredDocument doc : results) {
            String docId = doc.getDocument().getId();

            // Keep highest score for duplicates
            if (!uniqueResults.containsKey(docId)
                    || doc.getScore() > uniqueResults.get(docId).getScore()) {
                uniqueResults.put(docId, doc);
            }
        }

        return new ArrayList<>(uniqueResults.values());
    }

    /**
     * Rerank results using advanced scoring.
     */
    private List<ScoredDocument> rerank(String query, List<ScoredDocument> results) {
        // Simple reranking: boost documents with query terms in title
        String[] queryTerms = query.toLowerCase().split("\\s+");

        for (ScoredDocument doc : results) {
            double boost = 1.0;

            // Title match boost
            String title = doc.getDocument().getTitle();
            if (title != null) {
                String titleLower = title.toLowerCase();
                for (String term : queryTerms) {
                    if (titleLower.contains(term)) {
                        boost += 0.1;
                    }
                }
            }

            // Apply boost
            doc.setScore(doc.getScore() * boost);
        }

        // Re-sort by score
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return results;
    }

    /**
     * Apply diversity filtering to avoid redundant results.
     */
    private List<ScoredDocument> applyDiversityFilter(
            List<ScoredDocument> results, RetrievalOptions options) {
        if (results.size() <= 3) {
            return results; // Too few to filter
        }

        List<ScoredDocument> diverse = new ArrayList<>();
        Set<String> seenPackages = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();

        for (ScoredDocument doc : results) {
            // Check diversity by package
            String pkg = doc.getDocument().getMetadata("package");
            String title = doc.getDocument().getTitle();

            // Always include high-scoring documents
            if (doc.getScore() > 0.8) {
                diverse.add(doc);
                if (pkg != null) seenPackages.add(pkg);
                if (title != null) seenTitles.add(title);
                continue;
            }

            // Check diversity
            boolean isDiverse = true;

            if (pkg != null && seenPackages.contains(pkg)) {
                // Same package, less diverse
                if (seenPackages.size() > 2) {
                    isDiverse = false;
                }
            }

            if (title != null) {
                // Similar title
                for (String seenTitle : seenTitles) {
                    if (areSimilarTitles(title, seenTitle)) {
                        isDiverse = false;
                        break;
                    }
                }
            }

            if (isDiverse || diverse.size() < options.getMaxResults() / 2) {
                diverse.add(doc);
                if (pkg != null) seenPackages.add(pkg);
                if (title != null) seenTitles.add(title);
            }
        }

        return diverse;
    }

    /**
     * Check if two titles are similar.
     */
    private boolean areSimilarTitles(String title1, String title2) {
        String[] words1 = title1.toLowerCase().split("\\W+");
        String[] words2 = title2.toLowerCase().split("\\W+");

        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));

        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        double similarity = (double) intersection.size() / union.size();

        return similarity > 0.6; // 60% similar
    }
}
