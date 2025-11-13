package com.noteflix.pcm.rag.pipeline.core;

import com.noteflix.pcm.rag.api.RAGService;
import com.noteflix.pcm.rag.vectorstore.api.VectorStore;
import com.noteflix.pcm.rag.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Default RAG service implementation.
 *
 * <p>Uses vector store for retrieval (can be swapped). Can integrate with LLM for answer
 * generation.
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DefaultRAGService implements RAGService {

  private final VectorStore vectorStore;
  private final boolean useLLM;

  public DefaultRAGService(VectorStore vectorStore) {
    this(vectorStore, false);
  }

  public DefaultRAGService(VectorStore vectorStore, boolean useLLM) {
    this.vectorStore = vectorStore;
    this.useLLM = useLLM;
    log.info("RAG service initialized (LLM: {})", useLLM);
  }

  @Override
  public RAGResponse query(String query) {
    return query(query, RetrievalOptions.defaults());
  }

  @Override
  public RAGResponse query(String query, RetrievalOptions options) {
    long startTime = System.currentTimeMillis();

    try {
      // 1. Retrieve relevant documents
      long retrievalStart = System.currentTimeMillis();
      List<ScoredDocument> scoredDocs = vectorStore.search(query, options);
      long retrievalTime = System.currentTimeMillis() - retrievalStart;

      // 2. Build contexts
      List<RAGContext> contexts = buildContexts(scoredDocs, query);

      // 3. Generate answer
      long generationStart = System.currentTimeMillis();
      String answer = generateAnswer(query, contexts);
      long generationTime = System.currentTimeMillis() - generationStart;

      // 4. Calculate confidence
      double confidence = calculateConfidence(contexts);

      long totalTime = System.currentTimeMillis() - startTime;

      RAGResponse response =
          RAGResponse.builder()
              .query(query)
              .answer(answer)
              .sources(contexts)
              .processingTimeMs(totalTime)
              .retrievalTimeMs(retrievalTime)
              .generationTimeMs(generationTime)
              .documentsRetrieved(scoredDocs.size())
              .confidence(confidence)
              .build();

      log.info(
          "Query '{}' processed in {}ms (retrieval: {}ms, generation: {}ms, docs: {})",
          query,
          totalTime,
          retrievalTime,
          generationTime,
          scoredDocs.size());

      return response;

    } catch (Exception e) {
      log.error("Query failed: {}", query, e);

      // Return error response
      return RAGResponse.builder()
          .query(query)
          .answer("Error processing query: " + e.getMessage())
          .sources(new ArrayList<>())
          .processingTimeMs(System.currentTimeMillis() - startTime)
          .documentsRetrieved(0)
          .confidence(0.0)
          .build();
    }
  }

  @Override
  public void indexDocument(RAGDocument document) {
    vectorStore.indexDocument(document);
  }

  @Override
  public void indexDocuments(List<RAGDocument> documents) {
    vectorStore.indexDocuments(documents);
  }

  @Override
  public long getDocumentCount() {
    return vectorStore.getDocumentCount();
  }

  @Override
  public void clear() {
    vectorStore.clear();
  }

  @Override
  public void close() {
    vectorStore.close();
  }

  // ========== Helper Methods ==========

  private List<RAGContext> buildContexts(List<ScoredDocument> scoredDocs, String query) {
    return scoredDocs.stream()
        .map(
            scoredDoc ->
                RAGContext.builder()
                    .scoredDocument(scoredDoc)
                    .chunk(
                        scoredDoc.getSnippet() != null
                            ? scoredDoc.getSnippet()
                            : scoredDoc.getDocument().getContent())
                    .chunkIndex(0)
                    .reason("BM25 relevance score: " + String.format("%.3f", scoredDoc.getScore()))
                    .build())
        .collect(Collectors.toList());
  }

  private String generateAnswer(String query, List<RAGContext> contexts) {
    if (contexts.isEmpty()) {
      return "No relevant documents found for your query.";
    }

    if (useLLM) {
      // TODO: Integrate with LLM provider
      return generateAnswerWithLLM(query, contexts);
    } else {
      // Simple answer without LLM
      return generateSimpleAnswer(query, contexts);
    }
  }

  private String generateSimpleAnswer(String query, List<RAGContext> contexts) {
    StringBuilder answer = new StringBuilder();
    answer.append("Found ").append(contexts.size()).append(" relevant document(s):\n\n");

    int count = 1;
    for (RAGContext ctx : contexts) {
      RAGDocument doc = ctx.getDocument();

      answer.append(count++).append(". ");

      if (doc.getTitle() != null) {
        answer.append(doc.getTitle()).append("\n");
      }

      answer.append("   Type: ").append(doc.getType()).append("\n");

      if (doc.getSourcePath() != null) {
        answer.append("   Source: ").append(doc.getSourcePath()).append("\n");
      }

      answer
          .append("   Relevance: ")
          .append(String.format("%.1f%%", ctx.getScore() * 100))
          .append("\n");

      if (ctx.getChunk() != null && !ctx.getChunk().isEmpty()) {
        String snippet = ctx.getChunk();
        if (snippet.length() > 200) {
          snippet = snippet.substring(0, 200) + "...";
        }
        answer.append("   Excerpt: ").append(snippet).append("\n");
      }

      answer.append("\n");
    }

    return answer.toString();
  }

  private String generateAnswerWithLLM(String query, List<RAGContext> contexts) {
    // TODO: Implement LLM integration
    // 1. Build prompt with contexts
    // 2. Call LLM provider
    // 3. Return generated answer

    log.warn("LLM integration not yet implemented, using simple answer");
    return generateSimpleAnswer(query, contexts);
  }

  private double calculateConfidence(List<RAGContext> contexts) {
    if (contexts.isEmpty()) {
      return 0.0;
    }

    // Average score of top contexts
    double avgScore = contexts.stream().mapToDouble(RAGContext::getScore).average().orElse(0.0);

    return avgScore;
  }
}
