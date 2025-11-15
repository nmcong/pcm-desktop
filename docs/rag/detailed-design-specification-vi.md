# PCM Desktop - Đặc tả thiết kế chi tiết

Tài liệu này cung cấp đặc tả thiết kế toàn diện cho ứng dụng PCM (Project Configuration Management) Desktop, mở rộng từ đặc tả function calling với hướng dẫn triển khai chi tiết, hợp đồng API và kiến trúc hệ thống.

## Mục lục

1. [Tổng quan kiến trúc hệ thống](#tổng-quan-kiến-trúc-hệ-thống)
2. [Các chức năng dịch vụ cốt lõi](#các-chức-năng-dịch-vụ-cốt-lõi)
3. [Đặc tả API](#đặc-tả-api)
4. [Mô hình dữ liệu](#mô-hình-dữ-liệu)
5. [Hướng dẫn triển khai](#hướng-dẫn-triển-khai)
6. [Xem xét bảo mật](#xem-xét-bảo-mật)
7. [Yêu cầu hiệu suất](#yêu-cầu-hiệu-suất)
8. [Chiến lược kiểm thử](#chiến-lược-kiểm-thử)

---

## Tổng quan kiến trúc hệ thống

### Kiến trúc thành phần

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Tầng UI       │    │ Logic nghiệp vụ │    │ Tầng dữ liệu    │
│                 │    │                 │    │                 │
│ • JavaFX UI     │◄──►│ • Service APIs  │◄──►│ • SQLite DB     │
│ • Theme Manager │    │ • RAG Engine    │    │ • Qdrant Vector │
│ • Navigation    │    │ • LLM Integration│    │ • File System   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Ngăn xếp công nghệ

- **Frontend**: JavaFX 21, AtlantaFX (Material Design)
- **Backend**: Java 21, Spring Framework
- **Database**: SQLite (chính), Qdrant (tìm kiếm vector)
- **LLM Integration**: OpenAI API, Mô hình local (ONNX)
- **Search**: Lucene (FTS), Hybrid retrieval tùy chỉnh
- **Logging**: SLF4J + Logback

---

## Các chức năng dịch vụ cốt lõi

### 1. Dịch vụ quản lý hệ thống

#### 1.1 Dịch vụ đăng ký hệ thống

**Chức năng**: `registerSystem`

**Mục đích**: Tạo và quản lý cấu trúc hệ thống phân cấp cho tổ chức dự án.

**Chi tiết triển khai**:

```java
@Service
public class SystemRegistrationService {
    
    public SystemRegistrationResponse registerSystem(SystemRegistrationRequest request) {
        // Xác thực cấu trúc phân cấp
        validateHierarchy(request);
        
        // Upsert các thực thể hệ thống
        SystemEntity system = upsertSystem(request.getSystem());
        List<SubsystemEntity> subsystems = upsertSubsystems(request.getSubsystems(), system.getId());
        List<ProjectEntity> projects = upsertProjects(request.getProjects(), subsystems);
        List<BatchEntity> batches = upsertBatches(request.getBatches(), projects);
        
        // Xây dựng phản hồi đã chuẩn hóa
        return buildHierarchyResponse(system, subsystems, projects, batches);
    }
    
    private void validateHierarchy(SystemRegistrationRequest request) {
        // Kiểm tra phụ thuộc vòng tròn
        // Xác thực quy ước đặt tên
        // Đảm bảo ràng buộc parent-child
    }
}
```

**Schema yêu cầu**:
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

#### 1.2 Dịch vụ đính kèm mã nguồn

**Chức năng**: `attachSourceRoot`

**Mục đích**: Liên kết dự án với kho mã nguồn và hệ thống tập tin.

**Chi tiết triển khai**:

```java
@Service
public class SourceAttachmentService {
    
    public SourceAttachmentResponse attachSourceRoot(SourceAttachmentRequest request) {
        // Xác thực khả năng truy cập đường dẫn
        validateSourcePath(request.getRootPath());
        
        // Tạo ánh xạ mã nguồn
        ProjectSourceEntity source = new ProjectSourceEntity();
        source.setProjectId(request.getProjectId());
        source.setRootPath(request.getRootPath());
        source.setVcsType(request.getVcsType());
        source.setBranch(request.getBranch());
        source.setCommitHash(request.getCommit());
        source.setStatus(SourceStatus.PENDING);
        source.setCreatedAt(Instant.now());
        
        ProjectSourceEntity saved = projectSourceRepository.save(source);
        
        // Lập lịch quét ban đầu
        sourceScanScheduler.scheduleImmediate(saved.getId());
        
        return SourceAttachmentResponse.builder()
            .sourceId(saved.getId())
            .status(saved.getStatus())
            .build();
    }
}
```

### 2. Dịch vụ phân tích mã nguồn

#### 2.1 Dịch vụ quét mã nguồn

**Chức năng**: `triggerSourceScan`

**Mục đích**: Phân tích thay đổi mã nguồn, xây dựng lại AST và cập nhật chỉ mục tìm kiếm.

**Kiến trúc triển khai**:

```java
@Service
public class SourceScanningService {
    
    @Async
    public CompletableFuture<ScanResult> triggerSourceScan(ScanRequest request) {
        ScanResult result = new ScanResult();
        
        try {
            // Giai đoạn 1: Phát hiện tập tin & Xác thực checksum
            List<SourceFileEntity> changedFiles = discoverChangedFiles(request);
            result.setFilesProcessed(changedFiles.size());
            
            // Giai đoạn 2: Tạo AST
            for (SourceFileEntity file : changedFiles) {
                processFileAST(file);
            }
            
            // Giai đoạn 3: Tạo Vector Embedding
            generateEmbeddings(changedFiles);
            
            // Giai đoạn 4: Cập nhật chỉ mục tìm kiếm
            updateSearchIndices(changedFiles);
            
            result.setStatus(ScanStatus.COMPLETED);
            
        } catch (Exception e) {
            result.setStatus(ScanStatus.FAILED);
            result.setError(e.getMessage());
            log.error("Quét thất bại cho nguồn: " + request.getSourceId(), e);
        }
        
        return CompletableFuture.completedFuture(result);
    }
    
    private void processFileAST(SourceFileEntity file) {
        try {
            // Sử dụng JavaParser cho tập tin Java
            if (file.getPath().endsWith(".java")) {
                CompilationUnit cu = JavaParser.parse(new File(file.getAbsolutePath()));
                
                AstSnapshotEntity snapshot = new AstSnapshotEntity();
                snapshot.setFileId(file.getId());
                snapshot.setContent(cu.toString());
                snapshot.setChecksum(calculateChecksum(cu.toString()));
                snapshot.setCreatedAt(Instant.now());
                
                astSnapshotRepository.save(snapshot);
                
                // Trích xuất nodes và relationships
                extractAstNodes(cu, file.getId());
            }
        } catch (Exception e) {
            log.warn("Thất bại phân tích AST cho tập tin: " + file.getPath(), e);
        }
    }
}
```

### 3. Dịch vụ quản lý kiến thức

#### 3.1 Dịch vụ nhập CHM

**Chức năng**: `ingestChmPackage`

**Mục đích**: Trích xuất và lập chỉ mục tập tin trợ giúp Microsoft CHM.

**Chi tiết triển khai**:

```java
@Service
public class ChmIngestionService {
    
    public ChmIngestionResponse ingestChmPackage(ChmIngestionRequest request) {
        // Tạo bản ghi import
        ChmImportEntity importEntity = new ChmImportEntity();
        importEntity.setProjectId(request.getProjectId());
        importEntity.setSubsystemId(request.getSubsystemId());
        importEntity.setChmPath(request.getChmPath());
        importEntity.setStatus(ImportStatus.PROCESSING);
        importEntity.setStartedAt(Instant.now());
        
        ChmImportEntity saved = chmImportRepository.save(importEntity);
        
        try {
            // Trích xuất nội dung CHM sử dụng thư viện jCHM
            CHMFile chmFile = new CHMFile(request.getChmPath());
            List<CHMEntry> entries = chmFile.getAllEntries();
            
            int documentCount = 0;
            for (CHMEntry entry : entries) {
                if (entry.getType() == CHMEntryType.HTML) {
                    processChmDocument(entry, saved.getId());
                    documentCount++;
                }
            }
            
            // Cập nhật trạng thái import
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
            throw new ChmProcessingException("Thất bại xử lý tập tin CHM", e);
        }
    }
    
    private void processChmDocument(CHMEntry entry, UUID importId) {
        // Trích xuất nội dung HTML
        String htmlContent = entry.getContent();
        String textContent = htmlToText(htmlContent);
        
        // Tạo thực thể tài liệu
        ChmDocumentEntity document = new ChmDocumentEntity();
        document.setImportId(importId);
        document.setPath(entry.getPath());
        document.setTitle(extractTitle(htmlContent));
        document.setContent(textContent);
        document.setHtmlContent(htmlContent);
        
        ChmDocumentEntity saved = chmDocumentRepository.save(document);
        
        // Tạo knowledge chunk có thể tìm kiếm
        upsertKnowledgeChunk(UpsertKnowledgeRequest.builder()
            .sourceType(SourceType.CHM)
            .projectId(/* từ import */)
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

### 4. Dịch vụ tìm kiếm và truy xuất

#### 4.1 Dịch vụ tìm kiếm ngữ nghĩa

**Chức năng**: `runSemanticSearch`

**Mục đích**: Thực hiện tìm kiếm toàn văn sử dụng thuật toán BM25.

```java
@Service
public class SemanticSearchService {
    
    private final IndexSearcher luceneSearcher;
    private final Analyzer analyzer;
    
    public SemanticSearchResponse runSemanticSearch(SemanticSearchRequest request) {
        try {
            // Xây dựng truy vấn Lucene
            Query luceneQuery = buildLuceneQuery(request);
            
            // Thực hiện tìm kiếm
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
                .executionTime(/* đo lường */)
                .build();
                
        } catch (Exception e) {
            throw new SearchException("Tìm kiếm ngữ nghĩa thất bại", e);
        }
    }
    
    private Query buildLuceneQuery(SemanticSearchRequest request) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        
        // Truy vấn nội dung chính
        QueryParser parser = new QueryParser("content", analyzer);
        Query contentQuery = parser.parse(QueryParser.escape(request.getQuery()));
        builder.add(contentQuery, BooleanClause.Occur.MUST);
        
        // Bộ lọc dự án
        if (request.getProjectFilter() != null) {
            Query projectQuery = new TermQuery(new Term("project_id", request.getProjectFilter().toString()));
            builder.add(projectQuery, BooleanClause.Occur.FILTER);
        }
        
        // Bộ lọc loại nguồn
        if (request.getSourceType() != null) {
            Query sourceQuery = new TermQuery(new Term("source_type", request.getSourceType()));
            builder.add(sourceQuery, BooleanClause.Occur.FILTER);
        }
        
        return builder.build();
    }
}
```

#### 4.2 Dịch vụ tìm kiếm vector

**Chức năng**: `runVectorSearch`

**Mục đích**: Thực hiện tìm kiếm tương tự ngữ nghĩa sử dụng cơ sở dữ liệu vector Qdrant.

```java
@Service
public class VectorSearchService {
    
    private final QdrantClient qdrantClient;
    
    public VectorSearchResponse runVectorSearch(VectorSearchRequest request) {
        try {
            // Xây dựng yêu cầu tìm kiếm Qdrant
            SearchRequest searchRequest = SearchRequest.builder()
                .collection(VECTOR_COLLECTION_NAME)
                .vector(request.getEmbedding())
                .limit(request.getLimit())
                .filter(buildQdrantFilter(request.getFilters()))
                .withPayload(true)
                .withVector(false)
                .build();
            
            // Thực hiện tìm kiếm
            List<SearchResult> results = qdrantClient.search(searchRequest);
            
            // Chuyển đổi sang định dạng ứng dụng
            List<VectorResult> vectorResults = results.stream()
                .map(this::convertToVectorResult)
                .collect(Collectors.toList());
            
            return VectorSearchResponse.builder()
                .results(vectorResults)
                .executionTime(/* đo lường */)
                .build();
                
        } catch (Exception e) {
            throw new VectorSearchException("Tìm kiếm vector thất bại", e);
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
            .score(1.0 - qdrantResult.getDistance()) // Chuyển đổi distance thành similarity score
            .build();
    }
}
```

#### 4.3 Dịch vụ truy xuất kết hợp

**Chức năng**: `hybridRetrieve`

**Mục đích**: Kết hợp kết quả tìm kiếm ngữ nghĩa và vector với xếp hạng thông minh.

```java
@Service
public class HybridRetrievalService {
    
    public HybridRetrievalResponse hybridRetrieve(HybridRetrievalRequest request) {
        // Thực hiện cả hai tìm kiếm đồng thời
        CompletableFuture<SemanticSearchResponse> semanticFuture = CompletableFuture
            .supplyAsync(() -> semanticSearchService.runSemanticSearch(
                SemanticSearchRequest.builder()
                    .query(request.getQuery())
                    .projectFilter(request.getProjectId())
                    .limit(request.getTopKSemantic())
                    .build()));
        
        CompletableFuture<VectorSearchResponse> vectorFuture = CompletableFuture
            .supplyAsync(() -> {
                // Tạo embedding cho truy vấn
                float[] embedding = embeddingService.generateEmbedding(request.getQuery());
                
                return vectorSearchService.runVectorSearch(
                    VectorSearchRequest.builder()
                        .embedding(embedding)
                        .limit(request.getTopKVector())
                        .filters(Map.of("project_id", request.getProjectId()))
                        .build());
            });
        
        // Đợi cả hai hoàn thành
        SemanticSearchResponse semanticResults = semanticFuture.join();
        VectorSearchResponse vectorResults = vectorFuture.join();
        
        // Kết hợp kết quả sử dụng reciprocal rank fusion
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
        
        // Xử lý kết quả ngữ nghĩa
        for (int i = 0; i < semantic.getResults().size(); i++) {
            SearchResult result = semantic.getResults().get(i);
            String key = generateResultKey(result);
            
            double rrfScore = 1.0 / (60 + i + 1); // RRF với k=60
            
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
        
        // Xử lý kết quả vector
        for (int i = 0; i < vector.getResults().size(); i++) {
            VectorResult result = vector.getResults().get(i);
            String key = result.getFilePath() + ":" + result.getStartLine();
            
            double rrfScore = 1.0 / (60 + i + 1);
            
            HybridResult existing = resultMap.get(key);
            if (existing != null) {
                // Hợp nhất với kết quả ngữ nghĩa hiện có
                existing.setVectorScore(result.getScore());
                existing.setVectorRank(i + 1);
                existing.setRrfScore(existing.getRrfScore() + (rrfScore * request.getVectorWeight()));
                existing.getSources().add("vector");
            } else {
                // Kết quả mới chỉ có vector
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
        
        // Sắp xếp theo điểm RRF cuối cùng và trả về kết quả top
        return resultMap.values().stream()
            .sorted((a, b) -> Double.compare(b.getRrfScore(), a.getRrfScore()))
            .limit(request.getMaxResults())
            .collect(Collectors.toList());
    }
}
```

### 5. Dịch vụ tích hợp LLM

#### 5.1 Dịch vụ phản hồi Agent

**Chức năng**: `streamAgentResponse`

**Mục đích**: Tạo phản hồi AI với hỗ trợ streaming và theo dõi ngữ cảnh.

```java
@Service
public class AgentResponseService {
    
    public Flux<AgentResponseChunk> streamAgentResponse(AgentResponseRequest request) {
        return Flux.create(sink -> {
            try {
                // Truy xuất ngữ cảnh sử dụng tìm kiếm kết hợp
                HybridRetrievalResponse context = hybridRetrievalService.hybridRetrieve(
                    HybridRetrievalRequest.builder()
                        .query(request.getQuery())
                        .projectId(request.getProjectId())
                        .topKSemantic(10)
                        .topKVector(10)
                        .maxResults(20)
                        .build());
                
                // Xây dựng prompt với ngữ cảnh
                String prompt = buildPromptWithContext(request, context);
                
                // Tạo bản ghi phản hồi
                AgentResponseEntity response = new AgentResponseEntity();
                response.setRequestId(request.getRequestId());
                response.setPrompt(prompt);
                response.setStatus(ResponseStatus.STREAMING);
                response.setStartedAt(Instant.now());
                
                AgentResponseEntity saved = agentResponseRepository.save(response);
                
                // Lưu trữ artifacts ngữ cảnh
                storeContextArtifacts(saved.getId(), context.getResults());
                
                // Stream phản hồi LLM
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
                        // Hoàn tất phản hồi
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
        
        prompt.append("# Ngữ cảnh\n");
        prompt.append("Bạn là một trợ lý AI giúp phân tích dự án phần mềm.\n\n");
        
        prompt.append("## Ngữ cảnh đã truy xuất:\n");
        for (HybridResult result : context.getResults()) {
            prompt.append("### ").append(result.getFilePath()).append("\n");
            prompt.append(result.getContent()).append("\n\n");
        }
        
        prompt.append("## Câu hỏi người dùng:\n");
        prompt.append(request.getQuery()).append("\n\n");
        
        prompt.append("## Hướng dẫn:\n");
        prompt.append("- Cung cấp phản hồi hữu ích và chính xác dựa trên ngữ cảnh được cung cấp\n");
        prompt.append("- Trích dẫn tập tin cụ thể và số dòng khi tham chiếu mã\n");
        prompt.append("- Nếu ngữ cảnh không chứa đủ thông tin, hãy nói rõ ràng\n");
        
        return prompt.toString();
    }
}
```

---

## Đặc tả API

### REST Endpoints

Tất cả endpoints tuân theo quy ước RESTful với JSON request/response bodies.

**Base URL**: `/api/v1`

**Authentication**: Bearer token authentication

**Định dạng phản hồi chung**:
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
- `response.chunk` - Nội dung phản hồi streaming
- `response.complete` - Phản hồi hoàn tất
- `response.error` - Có lỗi xảy ra

---

## Yêu cầu hiệu suất

### Mục tiêu thời gian phản hồi

- **Các thao tác tìm kiếm**: < 500ms cho phần trăm thứ 95
- **Quét tập tin**: < 60 giây cho 10,000 tập tin
- **Phản hồi LLM**: Token đầu tiên < 2 giây
- **Điều hướng UI**: < 200ms chuyển trang

### Mục tiêu khả năng mở rộng

- **Người dùng đồng thời**: 50-100 người dùng cùng lúc
- **Kích thước dự án**: Lên đến 1M tập tin mỗi dự án
- **Chỉ mục tìm kiếm**: 10M+ tài liệu
- **Lưu trữ vector**: 100M+ embeddings

---

## Xem xét bảo mật

### Bảo vệ dữ liệu

- **Mã hóa tại chỗ**: Mã hóa cơ sở dữ liệu SQLite
- **Mã hóa trong quá trình truyền**: HTTPS/WSS cho tất cả các giao tiếp
- **Quản lý API Key**: Lưu trữ an toàn cho LLM API keys
- **Kiểm soát truy cập**: Quyền dựa trên vai trò

### Xác thực đầu vào

- **SQL Injection**: Chỉ sử dụng truy vấn có tham số
- **Path Traversal**: Xác thực đường dẫn nghiêm ngặt cho các thao tác tập tin
- **Ngăn chặn XSS**: Làm sạch đầu vào trong UI
- **Giới hạn tốc độ**: Throttling endpoint API

---

## Chiến lược kiểm thử

### Unit Testing
- **Mục tiêu bao phủ**: 85%+ bao phủ dòng
- **Framework**: JUnit 5 + Mockito
- **Test Doubles**: Mock các dịch vụ bên ngoài (LLM, Qdrant)

### Integration Testing
- **Database**: H2 in-memory cho testing
- **Search**: Chỉ mục Lucene nhúng
- **End-to-End**: TestFX cho UI testing

### Performance Testing
- **Load Testing**: JMeter cho API endpoints
- **Memory Profiling**: Monitor rò rỉ bộ nhớ
- **Search Performance**: Benchmark với bộ dữ liệu lớn

---

Đặc tả chi tiết này cung cấp nền tảng để triển khai ứng dụng PCM Desktop mạnh mẽ, có thể mở rộng với khả năng phân tích được hỗ trợ AI toàn diện.