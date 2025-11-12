# 🤖 RAG Implementation Plan for PCM Desktop

## 📋 Executive Summary

**Retrieval-Augmented Generation (RAG)** sẽ được tích hợp vào PCM Desktop để:
- Cho phép người dùng query hệ thống bằng ngôn ngữ tự nhiên
- Cung cấp câu trả lời chính xác dựa trên dữ liệu thực tế của hệ thống
- Tự động phân tích source code, database schema, và business logic

---

## 🎯 Objectives

### Primary Goals
1. ✅ **Natural Language Queries** - Users can ask questions in plain language
2. ✅ **Accurate Answers** - Responses based on actual system data
3. ✅ **Context-Aware** - Understand relationships between components
4. ✅ **Code Analysis** - Analyze and explain source code
5. ✅ **Real-time** - Fast response times (<5 seconds)

### Success Metrics
- Query accuracy > 90%
- Response time < 5 seconds
- User satisfaction > 4.5/5
- Coverage of all system components

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     User Interface Layer                     │
│  ┌────────────────────────────────────────────────────┐     │
│  │  AI Query Interface (JavaFX)                        │     │
│  │  - Text input for queries                           │     │
│  │  - Display formatted responses                      │     │
│  │  - Show source documents/code                       │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                   RAG Orchestration Layer                    │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │   Query    │  │  Retrieval │  │  Response  │            │
│  │ Processing │→ │   Engine   │→ │ Generation │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                    Vector Storage Layer                      │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Vector Database (Chroma / Qdrant / PGVector)    │       │
│  │  - Code embeddings                                │       │
│  │  - Documentation embeddings                       │       │
│  │  - Schema embeddings                              │       │
│  └──────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      Data Sources Layer                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  SQLite  │  │  Oracle  │  │  Source  │  │Knowledge │   │
│  │(Metadata)│  │   (DB)   │  │  Code    │  │   Base   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                      LLM Integration                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │  OpenAI    │  │  Anthropic │  │   Local    │            │
│  │  GPT-4     │  │   Claude   │  │   LLM      │   ...      │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

---

## 📚 Technology Stack for RAG

### Core RAG Framework (Choose One)

#### Option 1: LangChain4j ⭐ (Recommended)
- **Pros:** 
  - Native Java implementation
  - Great documentation
  - Active community
  - Easy to integrate with Spring
- **Cons:**
  - Newer framework
- **JAR URL:** https://repo1.maven.org/maven2/dev/langchain4j/langchain4j/0.35.0/langchain4j-0.35.0.jar

#### Option 2: Spring AI
- **Pros:**
  - Official Spring project
  - Seamless Spring Boot integration
  - Growing ecosystem
- **Cons:**
  - Requires Spring Boot migration
  - Heavier framework

#### Option 3: Semantic Kernel for Java
- **Pros:**
  - Microsoft-backed
  - Multi-model support
- **Cons:**
  - Less Java-focused

### Vector Database Options

#### Option 1: Chroma (Embedded) ⭐ (Recommended)
- **Type:** Embedded / Client-server
- **Pros:**
  - Easy to embed in Java app
  - Open source
  - Good performance
  - Python API (can use via REST)
- **Setup:** Run Chroma server locally, access via REST API

#### Option 2: Qdrant (Self-hosted)
- **Type:** Client-server
- **Pros:**
  - High performance
  - Rich features
  - Good Java client
- **Setup:** Docker container

#### Option 3: PostgreSQL + pgvector
- **Type:** Extension for existing DB
- **Pros:**
  - Use existing PostgreSQL knowledge
  - Mature and stable
- **Cons:**
  - Need PostgreSQL setup

#### Option 4: Apache Lucene (Java Native)
- **Type:** Embedded
- **Pros:**
  - Pure Java
  - Battle-tested
  - No external dependencies
- **Cons:**
  - Not specialized for vectors
  - More manual setup

### Embedding Models

#### Option 1: OpenAI Embeddings ⭐ (Recommended for start)
- **Model:** text-embedding-3-small or text-embedding-3-large
- **Pros:** High quality, easy to use
- **Cons:** Cost, API calls

#### Option 2: Local Embeddings (ONNX)
- **Models:** all-MiniLM-L6-v2, BGE-small-en
- **Pros:** No API costs, privacy
- **Cons:** Setup complexity

#### Option 3: Sentence Transformers (via API)
- **Models:** Various open-source models
- **Pros:** Free, customizable
- **Cons:** Need to host

### LLM Options

#### For Production:
1. **OpenAI GPT-4 Turbo** - Best quality
2. **Anthropic Claude 3.5 Sonnet** - Great for code
3. **Azure OpenAI** - Enterprise support

#### For Development/Testing:
1. **Ollama** (Local) - llama3, codellama, phi-3
2. **GPT-3.5 Turbo** - Cheaper than GPT-4

---

## 📦 Required Libraries

### Core RAG Libraries

```java
// LangChain4j (Main framework)
langchain4j-core-0.35.0.jar
langchain4j-embeddings-all-minilm-l6-v2-0.35.0.jar  // Local embeddings
langchain4j-open-ai-0.35.0.jar                       // OpenAI integration

// HTTP Client for API calls
okhttp-4.12.0.jar
okhttp-logging-interceptor-4.12.0.jar

// JSON processing (already have Jackson)
// jackson-databind-2.18.2.jar ✅

// Apache HttpClient (alternative)
httpclient5-5.3.1.jar
httpcore5-5.2.5.jar

// Vector Database Client
qdrant-client-1.9.1.jar  // If using Qdrant
// OR
postgresql-42.7.3.jar    // If using PostgreSQL + pgvector
pgvector-0.1.4.jar

// ONNX Runtime for local embeddings
onnxruntime-1.17.1.jar

// Optional: For advanced NLP
opennlp-tools-2.3.2.jar
```

### Download URLs

```bash
# LangChain4j
https://repo1.maven.org/maven2/dev/langchain4j/langchain4j/0.35.0/langchain4j-0.35.0.jar
https://repo1.maven.org/maven2/dev/langchain4j/langchain4j-open-ai/0.35.0/langchain4j-open-ai-0.35.0.jar
https://repo1.maven.org/maven2/dev/langchain4j/langchain4j-embeddings-all-minilm-l6-v2/0.35.0/langchain4j-embeddings-all-minilm-l6-v2-0.35.0.jar

# OkHttp
https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar
https://repo1.maven.org/maven2/com/squareup/okhttp3/logging-interceptor/4.12.0/logging-interceptor-4.12.0.jar

# Kotlin stdlib (required by OkHttp)
https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.22/kotlin-stdlib-1.9.22.jar

# PostgreSQL + pgvector (if chosen)
https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar
https://repo1.maven.org/maven2/com/pgvector/pgvector/0.1.4/pgvector-0.1.4.jar

# ONNX Runtime
https://repo1.maven.org/maven2/com/microsoft/onnxruntime/onnxruntime/1.17.1/onnxruntime-1.17.1.jar
```

---

## 🔄 Implementation Phases

### Phase 1: Foundation (Weeks 1-2) 🏗️

#### Week 1: Setup & Infrastructure
- [ ] **Day 1-2:** Research & technology selection
  - Finalize vector database choice
  - Choose embedding model
  - Select LLM provider
  
- [ ] **Day 3-4:** Environment setup
  - Download and configure vector database
  - Set up LLM API accounts (OpenAI/Anthropic)
  - Download required JAR files
  
- [ ] **Day 5-7:** Basic integration
  - Create RAG module structure
  - Implement embedding generation
  - Test vector storage and retrieval

**Deliverables:**
- Working vector database
- Ability to create and store embeddings
- Basic similarity search working

#### Week 2: Core RAG Pipeline
- [ ] **Day 1-2:** Document ingestion
  - Create document chunking logic
  - Implement embedding pipeline
  - Store embeddings in vector DB
  
- [ ] **Day 3-4:** Retrieval system
  - Implement semantic search
  - Build ranking and filtering
  - Test retrieval accuracy
  
- [ ] **Day 5-7:** LLM integration
  - Connect to LLM API
  - Implement prompt templates
  - Test end-to-end query flow

**Deliverables:**
- Documents can be ingested and embedded
- Relevant documents can be retrieved
- LLM can generate responses with context

### Phase 2: PCM Integration (Weeks 3-4) 🔗

#### Week 3: Data Source Integration
- [ ] **Day 1-2:** SQLite metadata indexing
  - Index screens, subsystems, projects
  - Index batch jobs information
  - Index workflow data
  
- [ ] **Day 3-4:** Database schema indexing
  - Connect to Oracle database
  - Extract schema information
  - Index table/view/procedure metadata
  
- [ ] **Day 5-7:** Code analysis
  - Parse Java source files
  - Extract class/method signatures
  - Index code documentation

**Deliverables:**
- All PCM metadata indexed
- Database schema fully searchable
- Source code indexed and searchable

#### Week 4: Advanced Features
- [ ] **Day 1-2:** Relationship mapping
  - Map screens to source code
  - Map screens to database objects
  - Map workflows to components
  
- [ ] **Day 3-4:** Context enhancement
  - Build context graphs
  - Implement multi-hop retrieval
  - Add cross-reference capabilities
  
- [ ] **Day 5-7:** Query optimization
  - Implement query rewriting
  - Add caching layer
  - Optimize response times

**Deliverables:**
- Complex relationships can be queried
- Context-aware responses
- Fast query performance

### Phase 3: UI & UX (Week 5) 🎨

- [ ] **Day 1-2:** Query interface
  - Build chat-like UI in JavaFX
  - Add query history
  - Implement streaming responses
  
- [ ] **Day 3-4:** Results presentation
  - Format code snippets nicely
  - Add syntax highlighting
  - Show source references
  
- [ ] **Day 5-7:** Advanced UI features
  - Add filters and options
  - Implement feedback mechanism
  - Add keyboard shortcuts

**Deliverables:**
- User-friendly query interface
- Well-formatted responses
- Interactive result exploration

### Phase 4: Testing & Optimization (Week 6) 🧪

- [ ] **Day 1-2:** Unit testing
  - Test embedding generation
  - Test retrieval accuracy
  - Test LLM integration
  
- [ ] **Day 3-4:** Integration testing
  - End-to-end query tests
  - Performance testing
  - Load testing
  
- [ ] **Day 5-7:** Optimization
  - Tune retrieval parameters
  - Optimize prompt templates
  - Implement caching strategies

**Deliverables:**
- Comprehensive test suite
- Performance benchmarks
- Optimized system

### Phase 5: Production Readiness (Week 7-8) 🚀

#### Week 7: Error Handling & Monitoring
- [ ] Error handling and fallbacks
- [ ] Logging and monitoring
- [ ] Rate limiting and quotas
- [ ] Security measures (API key management)

#### Week 8: Documentation & Training
- [ ] User documentation
- [ ] Developer documentation
- [ ] Example queries and use cases
- [ ] Training materials

**Deliverables:**
- Production-ready system
- Complete documentation
- Training materials

---

## 💾 Data Indexing Strategy

### What to Index

#### 1. System Metadata (Priority: HIGH)
```
- Subsystems: name, description, owner, dependencies
- Projects: name, description, screens, features
- Screens: ID, name, type, events, business logic
- Database Objects: tables, views, procedures, functions, packages
- Batch Jobs: name, schedule, code, dependencies
- Workflows: steps, actors, conditions
```

#### 2. Source Code (Priority: HIGH)
```
- Java classes: full source, methods, fields
- SQL scripts: DDL, DML, procedures
- Configuration files: XML, properties, YAML
- Documentation: JavaDoc, inline comments
```

#### 3. Knowledge Base (Priority: MEDIUM)
```
- Business rules
- Technical specifications
- Architecture documents
- Troubleshooting guides
```

### Chunking Strategy

```python
# Pseudo-code for chunking
chunks = []

# For code files
if file_type == "java":
    # Chunk by class/method
    chunks = split_by_method(file, overlap=50_lines)
    
# For database procedures
elif file_type == "sql_procedure":
    # Chunk by procedure/function
    chunks = split_by_procedure(file)
    
# For documentation
elif file_type == "markdown":
    # Chunk by section (headers)
    chunks = split_by_header(file, chunk_size=1000_chars)

# Add metadata to each chunk
for chunk in chunks:
    chunk.metadata = {
        "source": file_path,
        "type": file_type,
        "entity_id": entity_id,
        "timestamp": now()
    }
```

---

## 🔍 Example Queries & Expected Behavior

### Query 1: Screen Analysis
```
User: "Màn hình customer registration gọi những stored procedures nào?"

RAG Process:
1. Retrieve: Find "customer registration" screen metadata
2. Retrieve: Get associated source code
3. Analyze: Parse code for database calls
4. Retrieve: Get procedure definitions
5. Generate: Format response with details

Response:
"Màn hình Customer Registration (SCREEN_001) gọi các stored procedures sau:

1. P_VALIDATE_CUSTOMER
   - Location: CUSTOMER_PKG
   - Purpose: Validate customer data before insert
   - Called from: CustomerService.java:125

2. P_INSERT_CUSTOMER
   - Location: CUSTOMER_PKG
   - Purpose: Insert new customer record
   - Called from: CustomerRepository.java:89

3. P_LOG_AUDIT_TRAIL
   - Location: AUDIT_PKG
   - Purpose: Log registration activity
   - Called from: AuditService.java:45

Source files:
- CustomerRegistrationController.java
- CustomerService.java
- CustomerRepository.java"
```

### Query 2: Batch Job
```
User: "Batch job nào chạy lúc 2 giờ sáng?"

Response:
"Có 3 batch jobs được schedule chạy lúc 2:00 AM:

1. Daily Customer Data Import (JOB_001)
   - Schedule: 0 2 * * * (Every day 2:00 AM)
   - Runtime: ~15 minutes
   - Dependencies: File Arrival Check Job
   - Database: PROD_DB (READ: STAGING_CUSTOMERS, WRITE: CUSTOMERS)

2. Order Reconciliation Job (JOB_005)
   - Schedule: 0 2 * * * (Every day 2:00 AM)
   - Runtime: ~30 minutes
   - Dependencies: None
   - Database: PROD_DB (READ/WRITE: ORDERS, ORDER_AUDIT)

3. Inventory Sync Job (JOB_012)
   - Schedule: 0 2 * * * (Every day 2:00 AM)
   - Runtime: ~10 minutes
   - Dependencies: None
   - Database: PROD_DB, WMS_DB"
```

### Query 3: Impact Analysis
```
User: "Nếu tôi thay đổi cột ADDRESS trong table CUSTOMERS thì ảnh hưởng gì?"

Response:
"Thay đổi cột ADDRESS trong table CUSTOMERS sẽ ảnh hưởng đến:

📱 Screens (4):
- Customer Registration Form (SCREEN_001)
- Customer Profile Update (SCREEN_015)
- Customer Search (SCREEN_023)
- Address Maintenance (SCREEN_045)

📊 Database Objects (7):
- View: V_CUSTOMER_SUMMARY (uses CUSTOMERS.ADDRESS)
- View: V_CUSTOMER_MAILING_LIST (uses CUSTOMERS.ADDRESS)
- Procedure: P_UPDATE_CUSTOMER (parameter: p_address)
- Procedure: P_FORMAT_ADDRESS (reads ADDRESS)
- Function: F_GET_CUSTOMER_FULL_INFO (returns ADDRESS)
- Trigger: TRG_CUSTOMERS_AUDIT (logs ADDRESS changes)
- Package: CUSTOMER_PKG (multiple procedures use ADDRESS)

⚙️ Batch Jobs (2):
- Customer Data Export (reads ADDRESS for export)
- Address Validation Batch (validates ADDRESS format)

💻 Source Code (12 files):
- CustomerEntity.java (field: address)
- CustomerDTO.java (field: address)
- CustomerMapper.java (maps address)
- CustomerValidator.java (validates address)
... (8 more files)

⚠️ Recommendation:
1. Update all entity/DTO classes
2. Review and update validation logic
3. Test all affected screens
4. Update database procedures
5. Coordinate with batch job schedule"
```

---

## 🔒 Security Considerations

### API Key Management
```java
// Store in environment variables or encrypted config
String openaiKey = System.getenv("OPENAI_API_KEY");
String anthropicKey = System.getenv("ANTHROPIC_API_KEY");

// Never commit keys to Git
// Use .env file for local development
```

### Data Privacy
- Don't send sensitive production data to external LLMs
- Consider using local/self-hosted LLMs for sensitive code
- Implement data masking for PII in examples

### Access Control
- Implement role-based access to RAG queries
- Log all queries for audit purposes
- Rate limit queries per user

---

## 📊 Monitoring & Metrics

### Key Metrics to Track

```java
// Query Metrics
- Total queries per day
- Average response time
- Successful vs. failed queries
- User satisfaction ratings

// Retrieval Metrics
- Number of documents retrieved
- Average relevance score
- Cache hit rate

// Cost Metrics
- API calls to LLM
- Token usage
- Embedding generation cost

// System Metrics
- Vector DB size
- Index freshness
- Memory usage
```

---

## 💰 Cost Estimation

### LLM API Costs (Monthly)

**Scenario: 1000 queries/month**

| Provider | Model | Input (1K tokens) | Output (1K tokens) | Est. Monthly Cost |
|----------|-------|-------------------|---------------------|-------------------|
| OpenAI | GPT-4 Turbo | $0.01 | $0.03 | ~$50-100 |
| OpenAI | GPT-3.5 Turbo | $0.0005 | $0.0015 | ~$5-10 |
| Anthropic | Claude 3.5 Sonnet | $0.003 | $0.015 | ~$25-50 |

**Embedding Costs:**
- OpenAI text-embedding-3-small: $0.00002/1K tokens
- Monthly for 10,000 documents: ~$5-10

### Infrastructure Costs

| Component | Option | Monthly Cost |
|-----------|--------|--------------|
| Vector DB | Chroma (self-hosted) | $0 (local) or $20-50 (cloud) |
| Vector DB | Qdrant Cloud | $0 (free tier) or $25+ |
| Compute | Local | $0 |

**Total Estimated Cost:**
- Development: $0-20/month (using free tiers)
- Production (small scale): $50-150/month
- Production (medium scale): $200-500/month

---

## 🎯 Success Criteria

### Technical Metrics
- [ ] Query response time < 5 seconds (95th percentile)
- [ ] Retrieval precision > 80%
- [ ] Retrieval recall > 70%
- [ ] System uptime > 99%

### Business Metrics
- [ ] 90% of queries answered correctly
- [ ] User satisfaction > 4.5/5
- [ ] 50% reduction in time to find information
- [ ] 80% adoption rate among target users

### Quality Metrics
- [ ] LLM responses are factually accurate
- [ ] Code references are correct
- [ ] No hallucinations in responses
- [ ] Consistent response quality

---

## 📚 Resources & Learning

### Documentation
- [LangChain4j Docs](https://docs.langchain4j.dev/)
- [OpenAI API](https://platform.openai.com/docs)
- [Anthropic Claude](https://docs.anthropic.com/)
- [Chroma DB](https://docs.trychroma.com/)
- [Qdrant](https://qdrant.tech/documentation/)

### Tutorials
- Building RAG Applications in Java
- Vector Database Best Practices
- Prompt Engineering for Code Analysis

### Community
- LangChain4j GitHub Issues
- Stack Overflow: [langchain4j] tag
- Discord: LangChain community

---

## 🚀 Getting Started

### Quick Start Guide

1. **Install Vector Database**
   ```bash
   # Option 1: Chroma (Docker)
   docker run -p 8000:8000 chromadb/chroma
   
   # Option 2: Qdrant (Docker)
   docker run -p 6333:6333 qdrant/qdrant
   ```

2. **Get API Keys**
   - OpenAI: https://platform.openai.com/api-keys
   - Anthropic: https://console.anthropic.com/

3. **Download Libraries**
   ```bash
   cd /Users/nguyencong/Workspace/pcm-desktop
   ./download-rag-libs.sh
   ```

4. **Run First Example**
   - See: `examples/SimpleRAGExample.java`

---

## 📝 Next Steps

1. **Week 1:** Review this plan with team
2. **Week 1:** Make technology decisions (vector DB, LLM provider)
3. **Week 1:** Set up development environment
4. **Week 2:** Start Phase 1 implementation
5. **Week 3-8:** Follow implementation phases
6. **Week 9:** Launch beta to internal users
7. **Week 10+:** Gather feedback and iterate

---

**Document Version:** 1.0
**Last Updated:** November 11, 2025
**Owner:** PCM Development Team
**Status:** 📋 Planning Phase

