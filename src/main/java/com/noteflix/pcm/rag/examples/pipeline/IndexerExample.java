package com.noteflix.pcm.rag.examples.pipeline;

import com.noteflix.pcm.rag.api.RAGService;
import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.api.VectorStoreConfig;
import com.noteflix.pcm.rag.api.VectorStoreFactory;
import com.noteflix.pcm.rag.core.DefaultRAGService;
import com.noteflix.pcm.rag.indexer.DocumentIndexer;
import com.noteflix.pcm.rag.model.RAGResponse;
import com.noteflix.pcm.rag.model.RetrievalOptions;
import java.nio.file.Paths;

/**
 * Example: Indexing documents with DocumentIndexer. Shows how to: 1. Create indexer 2. Index
 * directory 3. Query indexed documents
 *
 * @author PCM Team
 * @version 1.0.0
 */
public class IndexerExample {

  public static void main(String[] args) {
    System.out.println("=== Document Indexer Example ===\n");

    try {
      // 1. Create vector store (in-memory for demo)
      System.out.println("📦 Creating vector store...");
      VectorStore store = VectorStoreFactory.create(VectorStoreConfig.inMemory());

      // 2. Create RAG service
      System.out.println("🤖 Creating RAG service...");
      RAGService rag = new DefaultRAGService(store);

      // 3. Create indexer
      System.out.println("📝 Creating indexer...\n");
      DocumentIndexer indexer = new DocumentIndexer(rag);

      // 4. Index RAG source code
      System.out.println("=== Indexing RAG Package ===\n");
      DocumentIndexer.IndexingProgress progress =
          indexer.indexDirectory(
              Paths.get("src/main/java/com/noteflix/pcm/rag"), true // recursive
              );

      System.out.println("\n" + progress);
      System.out.println();

      // 5. Show indexed documents
      System.out.println("📊 Total documents indexed: " + rag.getDocumentCount());
      System.out.println();

      // 6. Query examples
      System.out.println("=== Query Examples ===\n");

      // Query 1: RAG service
      queryExample(rag, "RAG service implementation");

      // Query 2: Document parser
      queryExample(rag, "document parser for Java files");

      // Query 3: Vector store
      queryExample(rag, "vector store interface");

      // 7. Cleanup
      System.out.println("\n🧹 Cleaning up...");
      store.close();

      System.out.println("\n✅ Example completed!");

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void queryExample(RAGService rag, String query) {
    System.out.println("🔍 Query: \"" + query + "\"");

    long start = System.currentTimeMillis();

    RAGResponse response =
        rag.query(query, RetrievalOptions.builder().maxResults(3).minScore(0.0).build());

    long time = System.currentTimeMillis() - start;

    System.out.println("   Found: " + response.getDocumentsRetrieved() + " documents");
    System.out.println("   Time: " + time + "ms");

    if (!response.getSources().isEmpty()) {
      System.out.println(
          "   Top result: "
              + response.getSources().getFirst().getDocument().getTitle()
              + " ("
              + String.format("%.1f%%", response.getSources().getFirst().getScore() * 100)
              + ")");
    }

    System.out.println();
  }
}
