# PCM Desktop - Detailed Design Specification

This document provides a comprehensive design specification for the PCM (Project Configuration Management) Desktop application, expanding on the function calling specification with detailed implementation guidelines, API contracts, and system architecture.

## Table of Contents

1. [System Architecture Overview](#system-architecture-overview)
2. [Core Service Functions](#core-service-functions)
3. [API Specifications](#api-specifications)
4. [Data Models](#data-models)
5. [Implementation Guidelines](#implementation-guidelines)
6. [Security Considerations](#security-considerations)
7. [Performance Requirements](#performance-requirements)
8. [Testing Strategy](#testing-strategy)

---

## System Architecture Overview

### Component Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI Layer      │    │ Business Logic  │    │ Data Layer      │
│                 │    │                 │    │                 │
│ • JavaFX UI     │◄──►│ • Service APIs  │◄──►│ • SQLite DB     │
│ • Theme Manager │    │ • RAG Engine    │    │ • Qdrant Vector │
│ • Navigation    │    │ • LLM Integration│    │ • File System   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Technology Stack

- **Frontend**: JavaFX 21, AtlantaFX (Material Design)
- **Backend**: Java 21, Spring Framework
- **Database**: SQLite (primary), Qdrant (vector search)
- **LLM Integration**: OpenAI API, Local models (ONNX)
- **Search**: Lucene (FTS), Custom hybrid retrieval
- **Logging**: SLF4J + Logback

---

## Core Service Functions

### 1. System Management Services

#### 1.1 System Registration Service

**Function**: `registerSystem`

**Purpose**: Create and manage hierarchical system structures for project organization.

**Implementation Details**:

```java
@Service
public class SystemRegistrationService {
    
    public SystemRegistrationResponse registerSystem(SystemRegistrationRequest request) {
        // Validate hierarchy structure
        validateHierarchy(request);
        
        // Upsert system entities
        SystemEntity system = upsertSystem(request.getSystem());
        List<SubsystemEntity> subsystems = upsertSubsystems(request.getSubsystems(), system.getId());
        List<ProjectEntity> projects = upsertProjects(request.getProjects(), subsystems);
        List<BatchEntity> batches = upsertBatches(request.getBatches(), projects);
        
        // Build normalized response
        return buildHierarchyResponse(system, subsystems, projects, batches);
    }
    
    private void validateHierarchy(SystemRegistrationRequest request) {
        // Check for circular dependencies
        // Validate naming conventions
        // Ensure parent-child constraints
    }
}
```

**Request Schema**:
```json
{
  "system": {
    "id": "optional-uuid",
    "name": "string",
    "description": "string",
    "version": "string"
  },
  "subsystems": [
    {
      "id": "optional-uuid",
      "name": "string",
      "system_id": "uuid",
      "description": "string"
    }
  ],
  "projects": [
    {
      "id": "optional-uuid", 
      "name": "string",
      "subsystem_id": "uuid",
      "type": "enum[web,desktop,mobile,library]",
      "status": "enum[active,archived,deprecated]"
    }
  ],
  "batches": [
    {
      "id": "optional-uuid",
      "name": "string", 
      "project_id": "uuid",
      "version": "string"
    }
  ]
}
```

#### 1.2 Source Attachment Service

**Function**: `attachSourceRoot`

**Purpose**: Link projects to source code repositories and file systems.

**Implementation Details**:

```java
@Service
public class SourceAttachmentService {
    
    public SourceAttachmentResponse attachSourceRoot(SourceAttachmentRequest request) {
        // Validate path accessibility
        validateSourcePath(request.getRootPath());
        
        // Create source mapping
        ProjectSourceEntity source = new ProjectSourceEntity();
        source.setProjectId(request.getProjectId());
        source.setRootPath(request.getRootPath());
        source.setVcsType(request.getVcsType());
        source.setBranch(request.getBranch());
        source.setCommitHash(request.getCommit());
        source.setStatus(SourceStatus.PENDING);
        source.setCreatedAt(Instant.now());
        
        ProjectSourceEntity saved = projectSourceRepository.save(source);
        
        // Schedule initial scan
        sourceScanScheduler.scheduleImmediate(saved.getId());
        
        return SourceAttachmentResponse.builder()
            .sourceId(saved.getId())
            .status(saved.getStatus())
            .build();
    }
}
```

### 2. Source Code Analysis Services

#### 2.1 Source Scanning Service

**Function**: `triggerSourceScan`

**Purpose**: Analyze source code changes, rebuild AST, and update search indices.

**Implementation Architecture**:

```java
@Service
public class SourceScanningService {
    
    @Async
    public CompletableFuture<ScanResult> triggerSourceScan(ScanRequest request) {
        ScanResult result = new ScanResult();
        
        try {
            // Phase 1: File Discovery & Checksum Validation
            List<SourceFileEntity> changedFiles = discoverChangedFiles(request);
            result.setFilesProcessed(changedFiles.size());
            
            // Phase 2: AST Generation
            for (SourceFileEntity file : changedFiles) {
                processFileAST(file);
            }
            
            // Phase 3: Vector Embedding Generation
            generateEmbeddings(changedFiles);
            
            // Phase 4: Search Index Update
            updateSearchIndices(changedFiles);
            
            result.setStatus(ScanStatus.COMPLETED);
            
        } catch (Exception e) {
            result.setStatus(ScanStatus.FAILED);
            result.setError(e.getMessage());
            log.error("Scan failed for source: " + request.getSourceId(), e);
        }
        
        return CompletableFuture.completedFuture(result);
    }
    
    private void processFileAST(SourceFileEntity file) {
        try {
            // Use JavaParser for Java files
            if (file.getPath().endsWith(".java")) {
                CompilationUnit cu = JavaParser.parse(new File(file.getAbsolutePath()));
                
                AstSnapshotEntity snapshot = new AstSnapshotEntity();
                snapshot.setFileId(file.getId());
                snapshot.setContent(cu.toString());
                snapshot.setChecksum(calculateChecksum(cu.toString()));
                snapshot.setCreatedAt(Instant.now());
                
                astSnapshotRepository.save(snapshot);
                
                // Extract nodes and relationships
                extractAstNodes(cu, file.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to parse AST for file: " + file.getPath(), e);
        }
    }
}
```

### 3. Knowledge Management Services

#### 3.1 CHM Ingestion Service

**Function**: `ingestChmPackage`

**Purpose**: Extract and index Microsoft CHM help files.

**Implementation Details**:

```java
@Service
public class ChmIngestionService {
    
    public ChmIngestionResponse ingestChmPackage(ChmIngestionRequest request) {
        // Create import record
        ChmImportEntity importEntity = new ChmImportEntity();
        importEntity.setProjectId(request.getProjectId());
        importEntity.setSubsystemId(request.getSubsystemId());
        importEntity.setChmPath(request.getChmPath());
        importEntity.setStatus(ImportStatus.PROCESSING);
        importEntity.setStartedAt(Instant.now());
        
        ChmImportEntity saved = chmImportRepository.save(importEntity);
        
        try {
            // Extract CHM contents using jCHM library
            CHMFile chmFile = new CHMFile(request.getChmPath());
            List<CHMEntry> entries = chmFile.getAllEntries();
            
            int documentCount = 0;
            for (CHMEntry entry : entries) {
                if (entry.getType() == CHMEntryType.HTML) {
                    processChmDocument(entry, saved.getId());
                    documentCount++;
                }
            }
            
            // Update import status
            saved.setStatus(ImportStatus.COMPLETED);
            saved.setDocumentCount(documentCount);
            saved.setCompletedAt(Instant.now());
            chmImportRepository.save(saved);
            
            return ChmIngestionResponse.builder()
                .importId(saved.getId())
                .documentCount(documentCount)
                .status(ImportStatus.COMPLETED)
                .build();
                
        } catch (Exception e) {
            saved.setStatus(ImportStatus.FAILED);
            saved.setErrorMessage(e.getMessage());
            chmImportRepository.save(saved);
            throw new ChmProcessingException("Failed to process CHM file", e);
        }
    }
    
    private void processChmDocument(CHMEntry entry, UUID importId) {
        // Extract HTML content
        String htmlContent = entry.getContent();
        String textContent = htmlToText(htmlContent);
        
        // Create document entity
        ChmDocumentEntity document = new ChmDocumentEntity();
        document.setImportId(importId);
        document.setPath(entry.getPath());
        document.setTitle(extractTitle(htmlContent));
        document.setContent(textContent);
        document.setHtmlContent(htmlContent);
        
        ChmDocumentEntity saved = chmDocumentRepository.save(document);
        
        // Create searchable knowledge chunk
        upsertKnowledgeChunk(UpsertKnowledgeRequest.builder()
            .sourceType(SourceType.CHM)
            .projectId(/* from import */)
            .label(document.getTitle())
            .content(textContent)
            .metadata(Map.of(
                "chm_path", entry.getPath(),
                "import_id", importId.toString()
            ))
            .build());
    }
}
```

### 4. Search and Retrieval Services

#### 4.1 Semantic Search Service

**Function**: `runSemanticSearch`

**Purpose**: Execute full-text search using BM25 algorithm.

```java
@Service
public class SemanticSearchService {
    
    private final IndexSearcher luceneSearcher;
    private final Analyzer analyzer;
    
    public SemanticSearchResponse runSemanticSearch(SemanticSearchRequest request) {
        try {
            // Build Lucene query
            Query luceneQuery = buildLuceneQuery(request);
            
            // Execute search
            TopDocs topDocs = luceneSearcher.search(luceneQuery, request.getLimit());
            
            List<SearchResult> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = luceneSearcher.doc(scoreDoc.doc);
                
                SearchResult result = SearchResult.builder()
                    .corpusId(UUID.fromString(doc.get("corpus_id")))
                    .filePath(doc.get("file_path"))
                    .label(doc.get("label"))
                    .score(scoreDoc.score)
                    .snippet(generateSnippet(doc.get("content"), request.getQuery()))
                    .sourceType(doc.get("source_type"))
                    .build();
                    
                results.add(result);
            }
            
            return SemanticSearchResponse.builder()
                .query(request.getQuery())
                .results(results)
                .totalHits(topDocs.totalHits.value)
                .executionTime(/* measure */)
                .build();
                
        } catch (Exception e) {
            throw new SearchException("Semantic search failed", e);
        }
    }
    
    private Query buildLuceneQuery(SemanticSearchRequest request) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        
        // Main content query
        QueryParser parser = new QueryParser("content", analyzer);
        Query contentQuery = parser.parse(QueryParser.escape(request.getQuery()));
        builder.add(contentQuery, BooleanClause.Occur.MUST);
        
        // Project filter
        if (request.getProjectFilter() != null) {
            Query projectQuery = new TermQuery(new Term("project_id", request.getProjectFilter().toString()));
            builder.add(projectQuery, BooleanClause.Occur.FILTER);
        }
        
        // Source type filter
        if (request.getSourceType() != null) {
            Query sourceQuery = new TermQuery(new Term("source_type", request.getSourceType()));
            builder.add(sourceQuery, BooleanClause.Occur.FILTER);
        }
        
        return builder.build();
    }
}
```

#### 4.2 Vector Search Service

**Function**: `runVectorSearch`

**Purpose**: Execute semantic similarity search using Qdrant vector database.

```java
@Service
public class VectorSearchService {
    
    private final QdrantClient qdrantClient;
    
    public VectorSearchResponse runVectorSearch(VectorSearchRequest request) {
        try {
            // Build Qdrant search request
            SearchRequest searchRequest = SearchRequest.builder()
                .collection(VECTOR_COLLECTION_NAME)
                .vector(request.getEmbedding())
                .limit(request.getLimit())
                .filter(buildQdrantFilter(request.getFilters()))
                .withPayload(true)
                .withVector(false)
                .build();
            
            // Execute search
            List<SearchResult> results = qdrantClient.search(searchRequest);
            
            // Convert to application format
            List<VectorResult> vectorResults = results.stream()
                .map(this::convertToVectorResult)
                .collect(Collectors.toList());
            
            return VectorSearchResponse.builder()
                .results(vectorResults)
                .executionTime(/* measure */)
                .build();
                
        } catch (Exception e) {
            throw new VectorSearchException("Vector search failed", e);
        }
    }
    
    private VectorResult convertToVectorResult(SearchResult qdrantResult) {
        Map<String, Object> payload = qdrantResult.getPayload();
        
        return VectorResult.builder()
            .chunkId(UUID.fromString((String) payload.get("chunk_id")))
            .filePath((String) payload.get("file_path"))
            .startLine((Integer) payload.get("start_line"))
            .endLine((Integer) payload.get("end_line"))
            .content((String) payload.get("content"))
            .distance(qdrantResult.getDistance())
            .score(1.0 - qdrantResult.getDistance()) // Convert distance to similarity score
            .build();
    }
}
```

#### 4.3 Hybrid Retrieval Service

**Function**: `hybridRetrieve`

**Purpose**: Combine semantic and vector search results with intelligent ranking.

```java
@Service
public class HybridRetrievalService {
    
    public HybridRetrievalResponse hybridRetrieve(HybridRetrievalRequest request) {
        // Execute both searches concurrently
        CompletableFuture<SemanticSearchResponse> semanticFuture = CompletableFuture
            .supplyAsync(() -> semanticSearchService.runSemanticSearch(
                SemanticSearchRequest.builder()
                    .query(request.getQuery())
                    .projectFilter(request.getProjectId())
                    .limit(request.getTopKSemantic())
                    .build()));
        
        CompletableFuture<VectorSearchResponse> vectorFuture = CompletableFuture
            .supplyAsync(() -> {
                // Generate embedding for query
                float[] embedding = embeddingService.generateEmbedding(request.getQuery());
                
                return vectorSearchService.runVectorSearch(
                    VectorSearchRequest.builder()
                        .embedding(embedding)
                        .limit(request.getTopKVector())
                        .filters(Map.of("project_id", request.getProjectId()))
                        .build());
            });
        
        // Wait for both to complete
        SemanticSearchResponse semanticResults = semanticFuture.join();
        VectorSearchResponse vectorResults = vectorFuture.join();
        
        // Fuse results using reciprocal rank fusion
        List<HybridResult> fusedResults = fuseResults(semanticResults, vectorResults, request);
        
        return HybridRetrievalResponse.builder()
            .query(request.getQuery())
            .results(fusedResults)
            .semanticCount(semanticResults.getResults().size())
            .vectorCount(vectorResults.getResults().size())
            .fusedCount(fusedResults.size())
            .build();
    }
    
    private List<HybridResult> fuseResults(SemanticSearchResponse semantic, 
                                          VectorSearchResponse vector,
                                          HybridRetrievalRequest request) {
        Map<String, HybridResult> resultMap = new HashMap<>();
        
        // Process semantic results
        for (int i = 0; i < semantic.getResults().size(); i++) {
            SearchResult result = semantic.getResults().get(i);
            String key = generateResultKey(result);
            
            double rrfScore = 1.0 / (60 + i + 1); // RRF with k=60
            
            HybridResult hybrid = HybridResult.builder()
                .filePath(result.getFilePath())
                .content(result.getSnippet())
                .semanticScore(result.getScore())
                .semanticRank(i + 1)
                .rrfScore(rrfScore * request.getSemanticWeight())
                .sources(Set.of("semantic"))
                .build();
                
            resultMap.put(key, hybrid);
        }
        
        // Process vector results
        for (int i = 0; i < vector.getResults().size(); i++) {
            VectorResult result = vector.getResults().get(i);
            String key = result.getFilePath() + ":" + result.getStartLine();
            
            double rrfScore = 1.0 / (60 + i + 1);
            
            HybridResult existing = resultMap.get(key);
            if (existing != null) {
                // Merge with existing semantic result
                existing.setVectorScore(result.getScore());
                existing.setVectorRank(i + 1);
                existing.setRrfScore(existing.getRrfScore() + (rrfScore * request.getVectorWeight()));
                existing.getSources().add("vector");
            } else {
                // New vector-only result
                HybridResult hybrid = HybridResult.builder()
                    .filePath(result.getFilePath())
                    .content(result.getContent())
                    .vectorScore(result.getScore())
                    .vectorRank(i + 1)
                    .rrfScore(rrfScore * request.getVectorWeight())
                    .sources(Set.of("vector"))
                    .build();
                    
                resultMap.put(key, hybrid);
            }
        }
        
        // Sort by final RRF score and return top results
        return resultMap.values().stream()
            .sorted((a, b) -> Double.compare(b.getRrfScore(), a.getRrfScore()))
            .limit(request.getMaxResults())
            .collect(Collectors.toList());
    }
}
```

### 5. LLM Integration Services

#### 5.1 Agent Response Service

**Function**: `streamAgentResponse`

**Purpose**: Generate AI responses with streaming support and context tracking.

```java
@Service
public class AgentResponseService {
    
    public Flux<AgentResponseChunk> streamAgentResponse(AgentResponseRequest request) {
        return Flux.create(sink -> {
            try {
                // Retrieve context using hybrid search
                HybridRetrievalResponse context = hybridRetrievalService.hybridRetrieve(
                    HybridRetrievalRequest.builder()
                        .query(request.getQuery())
                        .projectId(request.getProjectId())
                        .topKSemantic(10)
                        .topKVector(10)
                        .maxResults(20)
                        .build());
                
                // Build prompt with context
                String prompt = buildPromptWithContext(request, context);
                
                // Create response record
                AgentResponseEntity response = new AgentResponseEntity();
                response.setRequestId(request.getRequestId());
                response.setPrompt(prompt);
                response.setStatus(ResponseStatus.STREAMING);
                response.setStartedAt(Instant.now());
                
                AgentResponseEntity saved = agentResponseRepository.save(response);
                
                // Store context artifacts
                storeContextArtifacts(saved.getId(), context.getResults());
                
                // Stream LLM response
                StringBuilder fullResponse = new StringBuilder();
                
                llmClient.streamCompletion(prompt, chunk -> {
                    fullResponse.append(chunk.getContent());
                    
                    AgentResponseChunk responseChunk = AgentResponseChunk.builder()
                        .responseId(saved.getId())
                        .content(chunk.getContent())
                        .isComplete(chunk.isFinish())
                        .timestamp(Instant.now())
                        .build();
                    
                    sink.next(responseChunk);
                    
                    if (chunk.isFinish()) {
                        // Finalize response
                        saved.setContent(fullResponse.toString());
                        saved.setStatus(ResponseStatus.COMPLETED);
                        saved.setCompletedAt(Instant.now());
                        agentResponseRepository.save(saved);
                        
                        sink.complete();
                    }
                });
                
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
    
    private String buildPromptWithContext(AgentResponseRequest request, HybridRetrievalResponse context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("# Context\n");
        prompt.append("You are an AI assistant helping with software project analysis.\n\n");
        
        prompt.append("## Retrieved Context:\n");
        for (HybridResult result : context.getResults()) {
            prompt.append("### ").append(result.getFilePath()).append("\n");
            prompt.append(result.getContent()).append("\n\n");
        }
        
        prompt.append("## User Question:\n");
        prompt.append(request.getQuery()).append("\n\n");
        
        prompt.append("## Instructions:\n");
        prompt.append("- Provide a helpful and accurate response based on the context provided\n");
        prompt.append("- Cite specific files and line numbers when referencing code\n");
        prompt.append("- If the context doesn't contain enough information, say so clearly\n");
        
        return prompt.toString();
    }
}
```

---

## API Specifications

### REST Endpoints

All endpoints follow RESTful conventions with JSON request/response bodies.

**Base URL**: `/api/v1`

**Authentication**: Bearer token authentication

**Common Response Format**:
```json
{
  "success": true,
  "data": { /* response payload */ },
  "error": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "requestId": "uuid"
}
```

### WebSocket Events

**Endpoint**: `/ws/agent-responses`

**Events**:
- `response.chunk` - Streaming response content
- `response.complete` - Response finished
- `response.error` - Error occurred

---

## Performance Requirements

### Response Time Targets

- **Search Operations**: < 500ms for 95th percentile
- **File Scanning**: < 60 seconds for 10,000 files
- **LLM Responses**: First token < 2 seconds
- **UI Navigation**: < 200ms page transitions

### Scalability Targets

- **Concurrent Users**: 50-100 simultaneous users
- **Project Size**: Up to 1M files per project
- **Search Index**: 10M+ documents
- **Vector Storage**: 100M+ embeddings

---

## Security Considerations

### Data Protection

- **Encryption at Rest**: SQLite database encryption
- **Encryption in Transit**: HTTPS/WSS for all communications
- **API Key Management**: Secure storage for LLM API keys
- **Access Control**: Role-based permissions

### Input Validation

- **SQL Injection**: Parameterized queries only
- **Path Traversal**: Strict path validation for file operations
- **XSS Prevention**: Input sanitization in UI
- **Rate Limiting**: API endpoint throttling

---

## Testing Strategy

### Unit Testing
- **Coverage Target**: 85%+ line coverage
- **Framework**: JUnit 5 + Mockito
- **Test Doubles**: Mock external services (LLM, Qdrant)

### Integration Testing
- **Database**: In-memory H2 for testing
- **Search**: Embedded Lucene index
- **End-to-End**: TestFX for UI testing

### Performance Testing
- **Load Testing**: JMeter for API endpoints
- **Memory Profiling**: Monitor for memory leaks
- **Search Performance**: Benchmark with large datasets

---

This detailed specification provides the foundation for implementing a robust, scalable PCM Desktop application with comprehensive AI-powered analysis capabilities.