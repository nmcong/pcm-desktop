# Architecture Layers - PCM Desktop

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Mục đích:** Mô tả chi tiết kiến trúc code: Services, Repositories, Entities, DTOs, VOs

---

## 1. Architecture Overview

PCM Desktop follow **Clean Architecture** pattern với 4 layers chính:

```
┌─────────────────────────────────────┐
│          UI Layer                   │  (JavaFX Views + ViewModels)
│  ┌──────────────────────────────┐  │
│  │ Views, ViewModels, Controllers│  │
│  └──────────────────────────────┘  │
└────────────┬────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────┐
│      Application Layer              │  (Use Cases, Commands)
│  ┌──────────────────────────────┐  │
│  │ Use Cases, DTOs, Mappers     │  │
│  └──────────────────────────────┘  │
└────────────┬────────────────────────┘
             │ depends on
             ▼
┌─────────────────────────────────────┐
│        Domain Layer                 │  (Entities, Business Logic)
│  ┌──────────────────────────────┐  │
│  │ Entities, VOs, Repositories  │  │
│  │ (interfaces only)            │  │
│  └──────────────────────────────┘  │
└────────────▲────────────────────────┘
             │ implemented by
             │
┌────────────┴────────────────────────┐
│    Infrastructure Layer             │  (Adapters, External Services)
│  ┌──────────────────────────────┐  │
│  │ Repository Impls, DB Access, │  │
│  │ External APIs, File I/O      │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

**Dependency Rules:**
- UI → Application → Domain
- Infrastructure → Domain (implements interfaces)
- Domain depends on nothing (pure business logic)

---

## 2. Package Structure

```
com.noteflix.pcm/
├─ application/          # Use Cases layer
│  ├─ dto/               # Data Transfer Objects
│  ├─ mapper/            # Entity ↔ DTO mappers
│  ├─ request/           # User request use cases
│  ├─ retrieval/         # Search & retrieval use cases
│  ├─ ingestion/         # Source scanning use cases
│  ├─ review/            # Code review use cases
│  └─ testcase/          # Test generation use cases
│
├─ domain/               # Domain layer (pure business logic)
│  ├─ entity/            # Domain entities
│  ├─ vo/                # Value Objects
│  ├─ repository/        # Repository interfaces
│  ├─ service/           # Domain services (interfaces)
│  └─ exception/         # Domain exceptions
│
├─ infrastructure/       # Infrastructure layer (adapters)
│  ├─ persistence/       # Database implementation
│  │  ├─ sqlite/         # SQLite repositories
│  │  ├─ mapper/         # DB row ↔ Entity mappers
│  │  └─ migration/      # DB migrations
│  ├─ vectorstore/       # Qdrant implementation
│  ├─ search/            # FTS5 search implementation
│  ├─ ingestion/         # File scanning, AST parsing
│  ├─ llm/               # LLM providers
│  └─ embedding/         # Embedding generators
│
├─ ui/                   # UI layer (JavaFX)
│  ├─ page/              # Main pages
│  ├─ component/         # Reusable components
│  ├─ viewmodel/         # ViewModels (MVVM)
│  ├─ dialog/            # Dialogs & modals
│  └─ util/              # UI utilities
│
└─ shared/               # Shared utilities
   ├─ config/            # Configuration
   ├─ util/              # Common utilities
   ├─ constant/          # Constants
   └─ event/             # Event bus
```

---

## 3. Domain Layer

### 3.1 Entities

Entities represent core domain concepts with identity.

#### **System Entity**
```java
@Entity
@Table(name = "systems")
public class System {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long systemId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 255)
    private String owner;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters, setters, equals, hashCode
}
```

#### **Subsystem Entity**
```java
@Entity
@Table(name = "subsystems",
       uniqueConstraints = @UniqueConstraint(columnNames = {"system_id", "code"}))
public class Subsystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subsystemId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "system_id", nullable = false)
    private System system;
    
    @Column(nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 255)
    private String techStack;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubsystemStatus status = SubsystemStatus.ACTIVE;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "subsystem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();
    
    @OneToMany(mappedBy = "subsystem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Batch> batches = new ArrayList<>();
}
```

#### **Project Entity**
```java
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "subsystem_id", nullable = false)
    private Subsystem subsystem;
    
    @Column(nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 255)
    private String lead;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status = ProjectStatus.DRAFT;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "project")
    private List<ProjectSource> sources = new ArrayList<>();
    
    @OneToMany(mappedBy = "project")
    private List<UserRequest> requests = new ArrayList<>();
}
```

#### **ProjectSource Entity**
```java
@Entity
@Table(name = "project_sources")
public class ProjectSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sourceId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(nullable = false, length = 500)
    private String rootPath;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private VcsType vcsType = VcsType.GIT;
    
    @Column(length = 100)
    private String defaultBranch;
    
    @Column(length = 100)
    private String currentCommit;
    
    @Column(length = 50)
    private String language;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus scanStatus = ScanStatus.PENDING;
    
    private LocalDateTime lastScannedAt;
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "source")
    private List<SourceFile> files = new ArrayList<>();
    
    @OneToMany(mappedBy = "source")
    private List<AstSnapshot> snapshots = new ArrayList<>();
}
```

#### **SourceFile Entity**
```java
@Entity
@Table(name = "source_files",
       uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "relative_path"}))
public class SourceFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private ProjectSource source;
    
    @Column(nullable = false, length = 500)
    private String relativePath;
    
    @Column(length = 50)
    private String language;
    
    private Long sizeBytes;
    
    @Column(length = 64)
    private String checksum;
    
    private LocalDateTime lastModified;
    
    @Column(nullable = false)
    private Boolean isBinary = false;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "file")
    private List<AstNode> astNodes = new ArrayList<>();
}
```

#### **AstSnapshot Entity**
```java
@Entity
@Table(name = "ast_snapshots")
public class AstSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private ProjectSource source;
    
    @Column(length = 100)
    private String commitHash;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(length = 100)
    private String toolVersion;
    
    @Column(length = 64)
    private String rootChecksum;
    
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL)
    private List<AstNode> nodes = new ArrayList<>();
}
```

#### **AstNode Entity**
```java
@Entity
@Table(name = "ast_nodes")
@Indexed
public class AstNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nodeId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false)
    private AstSnapshot snapshot;
    
    @ManyToOne
    @JoinColumn(name = "file_id")
    private SourceFile file;
    
    @Column(nullable = false, length = 50)
    private String nodeType;
    
    @Column(length = 255)
    private String name;
    
    @Column(length = 500)
    private String fqName;
    
    private Integer startLine;
    private Integer endLine;
    
    @Column(length = 64)
    private String checksum;
    
    @Column(columnDefinition = "TEXT")
    private String payload; // JSON
    
    // Relationships (parent/child via ast_relationships table)
}
```

#### **UserRequest Entity**
```java
@Entity
@Table(name = "user_requests")
public class UserRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    
    @Column(length = 100)
    private String userId;
    
    @ManyToOne
    @JoinColumn(name = "subsystem_id")
    private Subsystem subsystem;
    
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @Column(length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestType requestType = RequestType.QUESTION;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority = Priority.NORMAL;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.RECEIVED;
    
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<AgentResponse> responses = new ArrayList<>();
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<RequestArtifact> artifacts = new ArrayList<>();
}
```

#### **AgentResponse Entity**
```java
@Entity
@Table(name = "agent_responses")
public class AgentResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private UserRequest request;
    
    @Column(columnDefinition = "TEXT")
    private String answerText;
    
    @Column(columnDefinition = "TEXT")
    private String reasoning;
    
    @Column(columnDefinition = "TEXT")
    private String citedSources; // JSON array
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ResponseStatus status = ResponseStatus.DRAFT;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL)
    private List<AnswerFeedback> feedbacks = new ArrayList<>();
}
```

**Complete Entity List (Total: 20+ entities):**

1. System
2. Subsystem
3. Project
4. Batch
5. ProjectSource
6. SourceFile
7. FileDependency
8. AstSnapshot
9. AstNode
10. AstRelationship
11. SearchCorpus
12. VectorDocument
13. ChmImport
14. ChmDocument
15. ChmAsset
16. UserRequest
17. RequestArtifact
18. AgentResponse
19. AnswerFeedback
20. ReviewComment
21. TestCatalog
22. TestRecommendation

---

### 3.2 Value Objects (VOs)

Value Objects are immutable objects defined by their attributes, not identity.

```java
// Value Object for embedding vector
public record EmbeddingVector(float[] values, int dimensions) {
    public EmbeddingVector {
        if (values.length != dimensions) {
            throw new IllegalArgumentException("Vector size mismatch");
        }
    }
    
    public double cosineSimilarity(EmbeddingVector other) {
        // Calculate cosine similarity
    }
}

// Value Object for file path
public record FilePath(String path) {
    public FilePath {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
    }
    
    public String getFileName() {
        return Paths.get(path).getFileName().toString();
    }
    
    public String getExtension() {
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : "";
    }
}

// Value Object for chunk metadata
public record ChunkMetadata(
    String chunkId,
    int startLine,
    int endLine,
    String language,
    int tokenCount
) implements Serializable {}

// Value Object for search result score
public record SearchScore(double value, String source) implements Comparable<SearchScore> {
    public SearchScore {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Score must be 0.0-1.0");
        }
    }
    
    @Override
    public int compareTo(SearchScore other) {
        return Double.compare(other.value, this.value); // Descending
    }
}
```

---

### 3.3 Repository Interfaces

```java
// Base repository interface
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void delete(ID id);
}

// System repository
public interface SystemRepository extends Repository<System, Long> {
    Optional<System> findByCode(String code);
    boolean existsByCode(String code);
    List<System> findByOwner(String owner);
}

// Project repository
public interface ProjectRepository extends Repository<Project, Long> {
    List<Project> findBySubsystemId(Long subsystemId);
    Optional<Project> findBySubsystemIdAndCode(Long subsystemId, String code);
    List<Project> findByStatus(ProjectStatus status);
    Page<Project> findAll(Pageable pageable);
}

// ProjectSource repository
public interface ProjectSourceRepository extends Repository<ProjectSource, Long> {
    List<ProjectSource> findByProjectId(Long projectId);
    List<ProjectSource> findByScanStatus(ScanStatus status);
    Optional<ProjectSource> findByRootPath(String rootPath);
}

// SourceFile repository
public interface SourceFileRepository extends Repository<SourceFile, Long> {
    List<SourceFile> findBySourceId(Long sourceId);
    Optional<SourceFile> findBySourceIdAndRelativePath(Long sourceId, String relativePath);
    List<SourceFile> findByLanguage(String language);
    List<SourceFile> findByChecksumChanged(Long sourceId, Map<String, String> checksums);
}

// AstNode repository
public interface AstNodeRepository extends Repository<AstNode, Long> {
    List<AstNode> findBySnapshotId(Long snapshotId);
    List<AstNode> findByFileId(Long fileId);
    Optional<AstNode> findByFqName(String fqName);
    List<AstNode> searchByName(String query, Long projectId);
    List<AstNode> findByNodeType(String nodeType);
}

// UserRequest repository
public interface UserRequestRepository extends Repository<UserRequest, Long> {
    List<UserRequest> findByUserId(String userId);
    List<UserRequest> findByProjectId(Long projectId);
    Page<UserRequest> search(RequestSearchCriteria criteria, Pageable pageable);
    List<UserRequest> findRecent(int limit);
}

// SearchCorpus repository
public interface SearchCorpusRepository extends Repository<SearchCorpus, Long> {
    List<SearchCorpus> findByProjectId(Long projectId);
    List<SearchCorpus> findBySourceType(String sourceType);
    Optional<SearchCorpus> findByChecksum(String checksum);
}

// VectorDocument repository
public interface VectorDocumentRepository extends Repository<VectorDocument, Long> {
    Optional<VectorDocument> findByChunkId(String chunkId);
    List<VectorDocument> findByProjectId(Long projectId);
    List<VectorDocument> findByFileId(Long fileId);
}

// ChmImport repository
public interface ChmImportRepository extends Repository<ChmImport, Long> {
    List<ChmImport> findByProjectId(Long projectId);
    List<ChmImport> findByStatus(String status);
    Optional<ChmImport> findByChmChecksum(String checksum);
}
```

---

## 4. Application Layer

### 4.1 Data Transfer Objects (DTOs)

DTOs transfer data between layers, decoupling internal entities from external APIs/UI.

```java
// System DTO
public record SystemDTO(
    Long systemId,
    String code,
    String name,
    String description,
    String owner,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int subsystemCount
) {}

// Create System request
public record CreateSystemRequest(
    @NotBlank @Size(min = 2, max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @Size(max = 2000) String description,
    @Size(max = 255) String owner
) {}

// Project DTO
public record ProjectDTO(
    Long projectId,
    Long subsystemId,
    String subsystemName,
    String code,
    String name,
    String description,
    String lead,
    ProjectStatus status,
    LocalDate startDate,
    LocalDate endDate,
    int sourceCount
) {}

// User Request DTO
public record UserRequestDTO(
    Long requestId,
    String userId,
    Long projectId,
    String projectName,
    String title,
    String description,
    RequestType requestType,
    Priority priority,
    RequestStatus status,
    LocalDateTime createdAt,
    LocalDateTime resolvedAt,
    List<AgentResponseSummary> responses
) {}

// Agent Response summary
public record AgentResponseSummary(
    Long responseId,
    String preview, // First 200 chars
    int citationCount,
    Double averageRating,
    LocalDateTime createdAt
) {}

// Search result DTO
public record SearchResultDTO(
    Long corpusId,
    String sourceType,
    String label,
    String content,
    String filePath,
    Integer startLine,
    Integer endLine,
    Double score,
    String highlightedSnippet
) {}

// AST Node DTO
public record AstNodeDTO(
    Long nodeId,
    String nodeType,
    String name,
    String fqName,
    String fileName,
    Integer startLine,
    Integer endLine,
    Map<String, Object> properties,
    List<RelationshipDTO> relationships
) {}

// Chunk Hit DTO (retrieval result)
public record ChunkHitDTO(
    String chunkId,
    String content,
    String filePath,
    Integer startLine,
    Integer endLine,
    String language,
    Double vectorScore,
    Double lexicalScore,
    Double fusedScore,
    Map<String, String> metadata
) {}

// Code snippet DTO
public record CodeSnippetDTO(
    String fileName,
    String filePath,
    int startLine,
    int endLine,
    String language,
    List<String> lines,
    String fqName,
    String nodeType
) {}
```

---

### 4.2 Mappers

Mappers convert between entities and DTOs.

```java
@Component
public class SystemMapper {
    public SystemDTO toDTO(System entity) {
        return new SystemDTO(
            entity.getSystemId(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getOwner(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getSubsystems().size()
        );
    }
    
    public System toEntity(CreateSystemRequest request) {
        System system = new System();
        system.setCode(request.code().toUpperCase());
        system.setName(request.name());
        system.setDescription(request.description());
        system.setOwner(request.owner());
        system.setCreatedAt(LocalDateTime.now());
        system.setUpdatedAt(LocalDateTime.now());
        return system;
    }
}

@Component
public class ProjectMapper {
    @Autowired
    private SubsystemRepository subsystemRepo;
    
    public ProjectDTO toDTO(Project entity) {
        return new ProjectDTO(
            entity.getProjectId(),
            entity.getSubsystem().getSubsystemId(),
            entity.getSubsystem().getName(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getLead(),
            entity.getStatus(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getSources().size()
        );
    }
    
    public Project toEntity(CreateProjectRequest request) {
        Subsystem subsystem = subsystemRepo.findById(request.subsystemId())
            .orElseThrow(() -> new EntityNotFoundException("Subsystem not found"));
        
        Project project = new Project();
        project.setSubsystem(subsystem);
        project.setCode(request.code().toUpperCase());
        project.setName(request.name());
        project.setDescription(request.description());
        project.setLead(request.lead());
        project.setStatus(ProjectStatus.DRAFT);
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }
}
```

---

### 4.3 Use Cases

Use cases orchestrate business workflows.

```java
// Submit User Request Use Case
@Service
public class SubmitUserRequestUseCase {
    @Autowired private UserRequestRepository requestRepo;
    @Autowired private RequestMapper requestMapper;
    @Autowired private RetrievalService retrievalService;
    @Autowired private EventBus eventBus;
    
    public UserRequestDTO execute(CreateRequestCommand command) {
        // Validate
        validateCommand(command);
        
        // Create entity
        UserRequest request = new UserRequest();
        request.setUserId(command.userId());
        request.setProjectId(command.projectId());
        request.setTitle(command.title());
        request.setDescription(command.description());
        request.setRequestType(command.requestType());
        request.setPriority(command.priority());
        request.setStatus(RequestStatus.RECEIVED);
        request.setCreatedAt(LocalDateTime.now());
        
        // Save
        request = requestRepo.save(request);
        
        // Trigger async processing
        eventBus.publish(new RequestCreatedEvent(request.getRequestId()));
        
        return requestMapper.toDTO(request);
    }
    
    private void validateCommand(CreateRequestCommand command) {
        if (command.description() == null || command.description().isBlank()) {
            throw new ValidationException("Description is required");
        }
    }
}

// Hybrid Retrieval Use Case
@Service
public class HybridRetrievalUseCase {
    @Autowired private VectorSearchService vectorSearch;
    @Autowired private LexicalSearchService lexicalSearch;
    @Autowired private FusionService fusion;
    @Autowired private MetricsService metrics;
    
    public List<ChunkHitDTO> execute(RetrievalQuery query) {
        long startTime = System.currentTimeMillis();
        
        // Vector search
        List<ChunkHit> vectorHits = vectorSearch.search(
            query.text(),
            query.projectId(),
            query.topKVector()
        );
        
        // Lexical search
        List<ChunkHit> lexicalHits = lexicalSearch.search(
            query.text(),
            query.projectId(),
            query.topKLexical()
        );
        
        // Fuse results
        List<ChunkHit> fused = fusion.fuse(vectorHits, lexicalHits);
        
        // Record metrics
        long duration = System.currentTimeMillis() - startTime;
        metrics.recordRetrieval(
            duration,
            vectorHits.size(),
            lexicalHits.size(),
            fused.size()
        );
        
        // Map to DTOs
        return fused.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}

// Scan Source Use Case
@Service
public class ScanProjectSourceUseCase {
    @Autowired private ProjectSourceRepository sourceRepo;
    @Autowired private SourceFileRepository fileRepo;
    @Autowired private FileSystemScanner scanner;
    @Autowired private ChecksumService checksumService;
    
    public ScanResult execute(Long sourceId) {
        ProjectSource source = sourceRepo.findById(sourceId)
            .orElseThrow(() -> new EntityNotFoundException("Source not found"));
        
        // Update status
        source.setScanStatus(ScanStatus.SCANNING);
        sourceRepo.save(source);
        
        try {
            ScanResult result = new ScanResult();
            
            // Scan file system
            List<Path> files = scanner.scanDirectory(Paths.get(source.getRootPath()));
            
            // Get existing files
            List<SourceFile> existingFiles = fileRepo.findBySourceId(sourceId);
            Map<String, SourceFile> fileMap = existingFiles.stream()
                .collect(Collectors.toMap(SourceFile::getRelativePath, f -> f));
            
            // Process each file
            for (Path file : files) {
                processFile(file, source, fileMap, result);
            }
            
            // Mark deleted files
            for (SourceFile existing : fileMap.values()) {
                fileRepo.delete(existing.getFileId());
                result.incrementDeleted();
            }
            
            // Update source
            source.setScanStatus(ScanStatus.COMPLETE);
            source.setLastScannedAt(LocalDateTime.now());
            sourceRepo.save(source);
            
            return result;
            
        } catch (Exception e) {
            source.setScanStatus(ScanStatus.FAILED);
            sourceRepo.save(source);
            throw new ScanException("Scan failed", e);
        }
    }
    
    private void processFile(Path file, ProjectSource source, 
                            Map<String, SourceFile> fileMap, 
                            ScanResult result) {
        String relativePath = source.getRootPath().relativize(file).toString();
        String checksum = checksumService.compute(file);
        
        SourceFile existing = fileMap.remove(relativePath);
        
        if (existing == null) {
            // New file
            SourceFile newFile = new SourceFile();
            newFile.setSource(source);
            newFile.setRelativePath(relativePath);
            newFile.setChecksum(checksum);
            // ... set other fields
            fileRepo.save(newFile);
            result.incrementNew();
        } else if (!checksum.equals(existing.getChecksum())) {
            // Modified file
            existing.setChecksum(checksum);
            // ... update fields
            fileRepo.save(existing);
            result.incrementModified();
        } else {
            // Unchanged
            result.incrementUnchanged();
        }
    }
}
```

---

## 5. Infrastructure Layer

### 5.1 Repository Implementations

```java
@Repository
public class SqliteSystemRepository implements SystemRepository {
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EntityMapper mapper;
    
    @Override
    public Optional<System> findById(Long id) {
        String sql = "SELECT * FROM systems WHERE system_id = ?";
        try {
            System system = jdbcTemplate.queryForObject(sql, mapper::mapSystem, id);
            return Optional.of(system);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<System> findAll() {
        String sql = "SELECT * FROM systems ORDER BY name";
        return jdbcTemplate.query(sql, mapper::mapSystem);
    }
    
    @Override
    public System save(System entity) {
        if (entity.getSystemId() == null) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }
    
    private System insert(System entity) {
        String sql = """
            INSERT INTO systems (code, name, description, owner, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getCode());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getDescription());
            ps.setString(4, entity.getOwner());
            ps.setTimestamp(5, Timestamp.valueOf(entity.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(entity.getUpdatedAt()));
            return ps;
        }, keyHolder);
        
        entity.setSystemId(keyHolder.getKey().longValue());
        return entity;
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM systems WHERE system_id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public Optional<System> findByCode(String code) {
        String sql = "SELECT * FROM systems WHERE code = ?";
        try {
            System system = jdbcTemplate.queryForObject(sql, mapper::mapSystem, code);
            return Optional.of(system);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM systems WHERE code = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }
}
```

---

### 5.2 Services

Services contain business logic and coordinate between repositories.

```java
// System Management Service
@Service
@Transactional
public class SystemManagementService {
    @Autowired private SystemRepository systemRepo;
    @Autowired private SubsystemRepository subsystemRepo;
    @Autowired private SystemMapper mapper;
    
    public SystemDTO createSystem(CreateSystemRequest request) {
        // Validate uniqueness
        if (systemRepo.existsByCode(request.code())) {
            throw new DuplicateException("System code already exists");
        }
        
        // Create entity
        System system = mapper.toEntity(request);
        system = systemRepo.save(system);
        
        return mapper.toDTO(system);
    }
    
    public List<SystemDTO> getAllSystems() {
        return systemRepo.findAll().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public void deleteSystem(Long systemId) {
        // Check if has subsystems
        List<Subsystem> subsystems = subsystemRepo.findBySystemId(systemId);
        if (!subsystems.isEmpty()) {
            throw new DeletionException(
                "Cannot delete system with subsystems. Delete subsystems first.");
        }
        
        systemRepo.delete(systemId);
    }
}

// Embedding Service
@Service
public class EmbeddingService {
    @Autowired private EmbeddingProvider provider;
    @Autowired private EmbeddingCacheRepository cacheRepo;
    @Autowired private ChecksumService checksumService;
    
    public float[] generate(String text) {
        // Check cache
        String hash = checksumService.computeHash(text);
        Optional<EmbeddingVector> cached = cacheRepo.findByHash(hash);
        
        if (cached.isPresent()) {
            return cached.get().values();
        }
        
        // Generate new embedding
        float[] embedding = provider.embed(text);
        
        // Cache it
        cacheRepo.save(new EmbeddingCache(hash, embedding));
        
        return embedding;
    }
    
    public List<float[]> generateBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        List<String> uncached = new ArrayList<>();
        
        // Check cache first
        for (String text : texts) {
            String hash = checksumService.computeHash(text);
            Optional<EmbeddingVector> cached = cacheRepo.findByHash(hash);
            
            if (cached.isPresent()) {
                results.add(cached.get().values());
            } else {
                uncached.add(text);
            }
        }
        
        // Generate uncached
        if (!uncached.isEmpty()) {
            List<float[]> generated = provider.embedBatch(uncached);
            
            // Cache them
            for (int i = 0; i < uncached.size(); i++) {
                String hash = checksumService.computeHash(uncached.get(i));
                cacheRepo.save(new EmbeddingCache(hash, generated.get(i)));
                results.add(generated.get(i));
            }
        }
        
        return results;
    }
}

// Fusion Service (combines search results)
@Service
public class FusionService {
    private static final double RRF_K = 60.0;
    
    public List<ChunkHit> fuse(List<ChunkHit> vectorHits, List<ChunkHit> lexicalHits) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, ChunkHit> hitMap = new HashMap<>();
        
        // RRF scoring from vector search
        for (int i = 0; i < vectorHits.size(); i++) {
            ChunkHit hit = vectorHits.get(i);
            double score = 1.0 / (RRF_K + i + 1);
            scores.merge(hit.getChunkId(), score, Double::sum);
            hitMap.putIfAbsent(hit.getChunkId(), hit);
        }
        
        // RRF scoring from lexical search
        for (int i = 0; i < lexicalHits.size(); i++) {
            ChunkHit hit = lexicalHits.get(i);
            double score = 1.0 / (RRF_K + i + 1);
            scores.merge(hit.getChunkId(), score, Double::sum);
            hitMap.putIfAbsent(hit.getChunkId(), hit);
        }
        
        // Sort by fused score
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(entry -> {
                ChunkHit hit = hitMap.get(entry.getKey());
                hit.setFusedScore(entry.getValue());
                return hit;
            })
            .collect(Collectors.toList());
    }
}
```

---

## 6. Summary Tables

### 6.1 Entity Summary

| Entity | Package | Purpose | Key Relationships |
|--------|---------|---------|-------------------|
| System | domain.entity | Top-level hierarchy | → Subsystem (1:N) |
| Subsystem | domain.entity | Mid-level grouping | → Project (1:N), → Batch (1:N) |
| Project | domain.entity | Work item | → ProjectSource (1:N), → UserRequest (1:N) |
| ProjectSource | domain.entity | Code repository | → SourceFile (1:N), → AstSnapshot (1:N) |
| SourceFile | domain.entity | File metadata | → AstNode (1:N) |
| AstNode | domain.entity | Code symbol | → AstRelationship (N:N) |
| UserRequest | domain.entity | User question | → AgentResponse (1:N), → RequestArtifact (1:N) |
| AgentResponse | domain.entity | AI answer | → AnswerFeedback (1:N) |

### 6.2 DTO Summary

| DTO | Purpose | Used By |
|-----|---------|---------|
| SystemDTO | System data transfer | UI, API |
| CreateSystemRequest | System creation | Use cases |
| ProjectDTO | Project data transfer | UI, API |
| UserRequestDTO | Request data transfer | UI, API |
| ChunkHitDTO | Search result | Retrieval use cases |
| CodeSnippetDTO | Code display | UI |
| AstNodeDTO | AST node data | UI, AST Explorer |

### 6.3 Service Summary

| Service | Layer | Purpose |
|---------|-------|---------|
| SystemManagementService | Application | CRUD for hierarchy |
| ScanProjectSourceUseCase | Application | Source scanning |
| HybridRetrievalUseCase | Application | Search orchestration |
| EmbeddingService | Infrastructure | Generate/cache embeddings |
| VectorSearchService | Infrastructure | Qdrant integration |
| LexicalSearchService | Infrastructure | FTS5 integration |
| FusionService | Infrastructure | Combine search results |
| AstParserService | Infrastructure | Parse source to AST |

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Next:** Implementation Guide

