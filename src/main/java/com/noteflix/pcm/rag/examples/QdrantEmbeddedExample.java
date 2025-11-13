package com.noteflix.pcm.rag.examples;

import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.api.VectorStoreConfig;
import com.noteflix.pcm.rag.api.VectorStoreFactory;
import com.noteflix.pcm.rag.embedded.QdrantEmbeddedManager;

/**
 * Example: Running Qdrant embedded with JavaFX.
 * 
 * This shows how to start/stop Qdrant binary as separate process.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
public class QdrantEmbeddedExample {
    
    public static void main(String[] args) {
        System.out.println("=== Qdrant Embedded Example ===\n");
        
        QdrantEmbeddedManager qdrant = null;
        
        try {
            // 1. Start Qdrant
            System.out.println("🚀 Starting Qdrant...");
            qdrant = new QdrantEmbeddedManager();
            qdrant.start();
            
            System.out.println("✅ Qdrant started: " + qdrant.getUrl());
            System.out.println("   Web UI: " + qdrant.getUrl() + "/dashboard\n");
            
            // 2. Create vector store
            System.out.println("📦 Creating Qdrant vector store...");
            VectorStore store = VectorStoreFactory.create(
                VectorStoreConfig.qdrant(
                    "localhost",
                    qdrant.getPort(),
                    null // No API key for local
                )
            );
            
            System.out.println("✅ Vector store created\n");
            
            // 3. Use vector store
            System.out.println("📊 Vector store status:");
            System.out.println("   Documents: " + store.getDocumentCount());
            System.out.println("   Type: Qdrant\n");
            
            // 4. Keep running for demo
            System.out.println("Press Ctrl+C to stop...\n");
            Thread.sleep(60000); // Run for 1 minute
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            // 5. Stop Qdrant
            if (qdrant != null) {
                System.out.println("\n🛑 Stopping Qdrant...");
                qdrant.stop();
                System.out.println("✅ Qdrant stopped");
            }
        }
        
        System.out.println("\n✅ Example completed!");
    }
}

