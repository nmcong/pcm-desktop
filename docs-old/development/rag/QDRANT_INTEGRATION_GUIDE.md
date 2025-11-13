# Qdrant Integration Guide

## 🎯 Tổng quan

Qdrant là vector database hiệu suất cao, tối ưu cho semantic search với embeddings.

**So sánh Lucene vs Qdrant:**

| Feature | Lucene (Hiện tại) | Qdrant |
|---------|-------------------|--------|
| **Search Type** | Keyword (BM25) | Semantic (Vector) |
| **Deployment** | Embedded | Server hoặc Embedded |
| **Setup** | ✅ Zero config | Cần setup |
| **Performance** | Fast (text search) | Very fast (vector search) |
| **Use Case** | Exact/keyword match | Semantic similarity |
| **Offline** | ✅ 100% | ✅ (local mode) |

---

## 📋 Các phương án sử dụng Qdrant

### Phương án 1: Qdrant Docker (RECOMMENDED) ⭐

**Ưu điểm:**
- ✅ Full-featured, production-ready
- ✅ Easy setup
- ✅ Web UI for monitoring
- ✅ 100% offline (chạy local)
- ✅ Không ảnh hưởng JavaFX app

**Nhược điểm:**
- ⚠️ Cần Docker installed
- ⚠️ Separate process (không embedded)

**Setup:**
```bash
# 1. Pull Qdrant image
docker pull qdrant/qdrant

# 2. Run Qdrant local
docker run -p 6333:6333 -p 6334:6334 \
    -v $(pwd)/data/qdrant:/qdrant/storage \
    qdrant/qdrant

# 3. Access Web UI
open http://localhost:6333/dashboard
```

**Usage trong app:**
```java
// Create Qdrant vector store (local)
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrantLocal()
);

// Hoặc remote
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrant("localhost", 6333, null)
);
```

---

### Phương án 2: Qdrant Binary (Lightweight)

**Ưu điểm:**
- ✅ Không cần Docker
- ✅ Single binary file
- ✅ JavaFX có thể start/stop via `ProcessBuilder`
- ✅ 100% offline

**Nhược điểm:**
- ⚠️ Cần download binary cho từng OS
- ⚠️ Quản lý process lifecycle

**Setup:**

1. **Download Qdrant binary:**
```bash
# macOS (ARM)
wget https://github.com/qdrant/qdrant/releases/download/v1.7.0/qdrant-aarch64-apple-darwin.tar.gz
tar -xzf qdrant-aarch64-apple-darwin.tar.gz

# macOS (Intel)
wget https://github.com/qdrant/qdrant/releases/download/v1.7.0/qdrant-x86_64-apple-darwin.tar.gz

# Linux
wget https://github.com/qdrant/qdrant/releases/download/v1.7.0/qdrant-x86_64-unknown-linux-gnu.tar.gz

# Windows
# Download from GitHub releases
```

2. **Start Qdrant from JavaFX:**
```java
public class QdrantEmbeddedManager {
    private Process qdrantProcess;
    
    public void start() throws IOException {
        String binary = getQdrantBinaryPath();
        
        ProcessBuilder pb = new ProcessBuilder(
            binary,
            "--storage-path", "data/qdrant-storage"
        );
        
        pb.redirectErrorStream(true);
        qdrantProcess = pb.start();
        
        // Wait for Qdrant to start
        waitForQdrant("http://localhost:6333");
        
        log.info("Qdrant started successfully");
    }
    
    public void stop() {
        if (qdrantProcess != null) {
            qdrantProcess.destroy();
        }
    }
    
    private String getQdrantBinaryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return "bin/qdrant-macos";
        } else if (os.contains("linux")) {
            return "bin/qdrant-linux";
        } else if (os.contains("win")) {
            return "bin/qdrant-windows.exe";
        }
        throw new IllegalStateException("Unsupported OS: " + os);
    }
    
    private void waitForQdrant(String url) throws IOException {
        // Poll until Qdrant is ready
        for (int i = 0; i < 30; i++) {
            try {
                URL healthUrl = new URL(url + "/health");
                HttpURLConnection conn = (HttpURLConnection) healthUrl.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() == 200) {
                    return; // Ready!
                }
            } catch (Exception e) {
                // Not ready yet
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IOException("Interrupted while waiting for Qdrant");
            }
        }
        
        throw new IOException("Qdrant failed to start within 30 seconds");
    }
}
```

3. **Integration với JavaFX lifecycle:**
```java
public class PCMApplication extends Application {
    private QdrantEmbeddedManager qdrantManager;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start Qdrant before app
        qdrantManager = new QdrantEmbeddedManager();
        qdrantManager.start();
        
        // Initialize app
        // ...
    }
    
    @Override
    public void stop() throws Exception {
        // Stop Qdrant when app closes
        if (qdrantManager != null) {
            qdrantManager.stop();
        }
        
        super.stop();
    }
}
```

---

### Phương án 3: Qdrant Cloud (Remote)

**Ưu điểm:**
- ✅ No setup required
- ✅ Scalable
- ✅ Automatic backups

**Nhược điểm:**
- ❌ NOT offline
- ❌ Requires internet
- ❌ Monthly cost

**Usage:**
```java
VectorStore store = VectorStoreFactory.create(
    VectorStoreConfig.qdrant(
        "xyz-example.qdrant.io",
        6333,
        "your-api-key"
    )
);
```

---

### ⚠️ Phương án 4: Qdrant Embedded (KHÔNG KHẢ DỤNG)

**Lý do:**
- ❌ Qdrant **KHÔNG có** embedded Java version
- ❌ Qdrant core viết bằng Rust
- ❌ Không có JNI bindings official

**Alternative:** Use Lucene (đã implement) hoặc Apache Cassandra với vector search.

---

## 🎨 Implementation - QdrantVectorStore

### 1. Add Qdrant Java Client

**Download:**
```bash
# Qdrant Java client
cd lib/rag
wget https://repo1.maven.org/maven2/io/qdrant/client/1.7.0/client-1.7.0.jar

# Dependencies
wget https://repo1.maven.org/maven2/io/grpc/grpc-netty-shaded/1.59.0/grpc-netty-shaded-1.59.0.jar
wget https://repo1.maven.org/maven2/io/grpc/grpc-protobuf/1.59.0/grpc-protobuf-1.59.0.jar
wget https://repo1.maven.org/maven2/io/grpc/grpc-stub/1.59.0/grpc-stub-1.59.0.jar
```

### 2. Implement QdrantVectorStore

```java
package com.noteflix.pcm.rag.core;

import com.noteflix.pcm.rag.api.VectorStore;
import com.noteflix.pcm.rag.model.*;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.*;
import io.qdrant.client.grpc.Points.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class QdrantVectorStore implements VectorStore {
    
    private final QdrantClient client;
    private final String collectionName;
    private final int vectorDimension;
    
    public QdrantVectorStore(String host, int port, String apiKey, String collectionName) {
        this.collectionName = collectionName != null ? collectionName : "rag_documents";
        this.vectorDimension = 384; // Default for all-MiniLM-L6-v2
        
        // Create client
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, false);
        
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.withApiKey(apiKey);
        }
        
        this.client = new QdrantClient(builder.build());
        
        // Initialize collection
        initializeCollection();
        
        log.info("Qdrant vector store initialized: {}:{}, collection: {}", 
            host, port, this.collectionName);
    }
    
    private void initializeCollection() {
        try {
            // Check if collection exists
            boolean exists = client.collectionExistsAsync(collectionName).get();
            
            if (!exists) {
                // Create collection
                VectorParams vectorParams = VectorParams.newBuilder()
                    .setSize(vectorDimension)
                    .setDistance(Distance.Cosine)
                    .build();
                
                CreateCollection createCollection = CreateCollection.newBuilder()
                    .setCollectionName(collectionName)
                    .setVectorsConfig(VectorsConfig.newBuilder()
                        .setParams(vectorParams)
                        .build())
                    .build();
                
                client.createCollectionAsync(createCollection).get();
                log.info("Created Qdrant collection: {}", collectionName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Qdrant collection", e);
            throw new RuntimeException("Failed to initialize Qdrant", e);
        }
    }
    
    @Override
    public void indexDocument(RAGDocument document) {
        // TODO: Implement using Qdrant client
        // Need embeddings first!
        throw new UnsupportedOperationException("Qdrant requires embeddings - not yet implemented");
    }
    
    @Override
    public List<ScoredDocument> search(String query, RetrievalOptions options) {
        // TODO: Implement vector search
        throw new UnsupportedOperationException("Qdrant requires embeddings - not yet implemented");
    }
    
    // ... other methods
}
```

---

## 🚀 RECOMMENDED APPROACH

### Cho PCM Desktop

**Phương án tốt nhất: Lucene + Qdrant Hybrid**

```java
public class HybridRAGService implements RAGService {
    
    private final VectorStore keywordStore;  // Lucene
    private final VectorStore semanticStore; // Qdrant (optional)
    
    public HybridRAGService() {
        // Always use Lucene (embedded, zero config)
        this.keywordStore = VectorStoreFactory.create(
            VectorStoreConfig.lucene("data/rag/lucene-index")
        );
        
        // Try to use Qdrant if available
        try {
            this.semanticStore = VectorStoreFactory.create(
                VectorStoreConfig.qdrantLocal()
            );
            log.info("Qdrant available - using hybrid search");
        } catch (Exception e) {
            this.semanticStore = null;
            log.info("Qdrant not available - using Lucene only");
        }
    }
    
    @Override
    public RAGResponse query(String query, RetrievalOptions options) {
        List<ScoredDocument> results;
        
        if (semanticStore != null && options.getSearchMode() == SearchMode.SEMANTIC) {
            // Semantic search with Qdrant
            results = semanticStore.search(query, options);
        } else if (semanticStore != null && options.getSearchMode() == SearchMode.HYBRID) {
            // Hybrid: combine Lucene + Qdrant
            List<ScoredDocument> keywordResults = keywordStore.search(query, options);
            List<ScoredDocument> semanticResults = semanticStore.search(query, options);
            results = mergeResults(keywordResults, semanticResults);
        } else {
            // Keyword search with Lucene (fallback)
            results = keywordStore.search(query, options);
        }
        
        return buildResponse(query, results);
    }
}
```

---

## 📊 So sánh phương án

| Phương án | Setup | Offline | Performance | Recommended |
|-----------|-------|---------|-------------|-------------|
| **Lucene (Hiện tại)** | ✅ Zero | ✅ Yes | ⚡ Fast | ✅ Always use |
| **Qdrant Docker** | ⚠️ Medium | ✅ Yes | ⚡⚡⚡ Very fast | ✅ For advanced users |
| **Qdrant Binary** | ⚠️ Medium | ✅ Yes | ⚡⚡⚡ Very fast | ✅ For distribution |
| **Qdrant Cloud** | ✅ Easy | ❌ No | ⚡⚡⚡ Very fast | ❌ Not for PCM |
| **Qdrant Embedded** | N/A | N/A | N/A | ❌ Not available |

---

## 🎯 Recommendation cho PCM Desktop

### Phase 1 (Hiện tại): ✅ DONE
- ✅ Use Lucene (embedded, zero config)
- ✅ Fast keyword search
- ✅ 100% offline
- ✅ Production ready

### Phase 2 (Optional): Future Enhancement
1. **Thêm Qdrant Docker support:**
   - Detect if Qdrant is running on localhost:6333
   - Auto-switch to hybrid mode if available
   - Fallback to Lucene if not

2. **Bundle Qdrant binary:**
   - Include Qdrant binary trong distribution
   - Auto-start via `ProcessBuilder`
   - Manage lifecycle với JavaFX

3. **Add embeddings:**
   - Local embedding model (e.g., all-MiniLM-L6-v2)
   - Generate vectors for documents
   - Store in Qdrant

---

## 💡 Quick Start (Qdrant Docker)

### 1. Start Qdrant
```bash
docker run -d --name qdrant \
    -p 6333:6333 \
    -v $(pwd)/data/qdrant:/qdrant/storage \
    qdrant/qdrant
```

### 2. Update code
```java
// In AIService or RAG initialization
try {
    VectorStore qdrant = VectorStoreFactory.create(
        VectorStoreConfig.qdrantLocal()
    );
    log.info("Using Qdrant for semantic search");
} catch (Exception e) {
    VectorStore lucene = VectorStoreFactory.create(
        VectorStoreConfig.lucene("data/rag/index")
    );
    log.info("Qdrant not available, using Lucene");
}
```

### 3. Check Qdrant
```bash
# Web UI
open http://localhost:6333/dashboard

# Health check
curl http://localhost:6333/health
```

---

## ✅ Summary

### Trả lời câu hỏi:

**"Làm sao để chạy Qdrant embedded bên trong JavaFX?"**

**Trả lời:**
1. ❌ **TRUE embedded (trong JVM)**: KHÔNG KHẢ DỤNG
   - Qdrant không có Java/JNI version
   
2. ✅ **Embedded (separate process)**: KHẢ DỤNG
   - Download Qdrant binary
   - Start via `ProcessBuilder` khi JavaFX start
   - Stop khi JavaFX exit
   - 100% offline, tự động

3. ✅ **Docker (recommended)**: KHẢ DỤNG
   - Chạy Qdrant trong Docker
   - JavaFX connect via HTTP
   - Dễ setup, production-ready

**Recommendation:**
- **Hiện tại**: Dùng Lucene (đã implement, works great!)
- **Tương lai**: Add Qdrant Docker support (optional)
- **Advanced**: Bundle Qdrant binary (cho distribution)

---

**PCM Desktop hiện tại đã có RAG system hoàn chỉnh với Lucene!** ✅

Qdrant là enhancement tùy chọn cho semantic search. 🚀

