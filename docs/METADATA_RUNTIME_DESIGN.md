# PCM Desktop – Metadata & LLM Runtime Design (SQLite as Source of Truth)

Tài liệu này mô tả chi tiết kiến trúc runtime khi **SQLite là nguồn dữ liệu chuẩn** cho metadata màn hình/hệ thống, và LLM chỉ truy cập thông tin qua **Metadata Service + RAG + LLM Client**.

Tài liệu này bổ sung cho `docs/SCREEN_METADATA_SPEC.md` (định nghĩa schema logic + bảng SQLite/Index).

---

## 1. Mục tiêu & Phạm vi

**Mục tiêu chính**
- LLM có thể:
  - Hiểu mối quan hệ giữa **Screen ↔ Code ↔ Database ↔ Rules ↔ Events**.
  - Trả lời chính xác câu hỏi dạng:
    - “Approve diễn ra như nào, lưu vào cột nào?”
    - “Thay đổi cột ngày hết hạn thì ảnh hưởng đến nghiệp vụ nào (screen, batch, report, rules)?”
- Dữ liệu metadata được lưu **trực tiếp trong SQLite**, không phụ thuộc YAML.

**Phạm vi**
- Thiết kế **service layer** trong Java để đọc/ghi metadata.
- Thiết kế **RAG Index** dựa trên metadata + source code.
- Thiết kế luồng **trả lời câu hỏi** trong Chat UI sử dụng LLM Client hiện có.

---

## 2. Kiến trúc tổng thể

Các thành phần chính (logic):
- **SQLite Metadata DB**
  - Schema chi tiết tại `SCREEN_METADATA_SPEC.md` (mục 10, 11).
  - Lưu screens, workflows, db tables/columns, rules, events, impacts, KB, documents, embeddings.

- **Metadata Repository (DAO)**
  - Lớp Java đọc/ghi thẳng vào SQLite (JDBC hoặc abstraction hiện có).
  - Cung cấp các truy vấn mức thấp (per-table).

- **Metadata Service (Domain API)**
  - Cung cấp hàm domain-friendly cho phần còn lại của hệ thống (UI, RAG, LLM tools).

- **RAG Index Service**
  - Xây dựng các “fact documents” từ metadata + source code.
  - Quản lý index trong `rag_documents` + `rag_embeddings`.

- **LLM Orchestrator**
  - Đứng trước `LLMClient` hiện có.
  - Chịu trách nhiệm:
    - Lấy context từ RAG/Metadata.
    - Gộp context + câu hỏi → prompt.
    - (Option) cho LLM sử dụng tools để truy vấn thêm từ SQLite.

- **Chat UI**
  - Màn hình chat hiện có.
  - Cung cấp:
    - Context màn hình hiện tại (`currentScreenId`).
    - Các filter (dùng context màn hình, hay toàn hệ thống).
    - Hiển thị “nguồn tham chiếu” (screen, bảng/cột, class/method).

---

## 3. Thiết kế Service Layer (Java)

### 3.1. Package gợi ý

```text
src/main/java/com/noteflix/pcm/metadata/
    MetadataRepository.java
    MetadataService.java
    RagIndexRepository.java
    RagIndexService.java
    LlmContextBuilder.java
    LlmOrchestrator.java
```

### 3.2. MetadataRepository (DAO)

Trách nhiệm: truy vấn SQLite theo schema đã định nghĩa.

Gợi ý một số method:

```java
interface MetadataRepository {
    // Screens
    Optional<Screen> findScreenById(String screenId);
    Optional<Screen> findScreenByRoute(String route);
    List<Screen> listScreensByModule(String module);

    List<ScreenComponent> findComponentsByScreen(String screenId);

    // Workflows
    List<Workflow> findWorkflowsByScreen(String screenId);
    List<WorkflowStep> findStepsByWorkflow(String workflowId);

    // Database
    Optional<DbTable> findTable(String tableName);
    Optional<DbColumn> findColumn(String tableName, String columnName);
    List<ColumnUsage> findColumnUsage(String tableName, String columnName);
    List<ColumnImpact> findColumnImpacts(String tableName, String columnName);

    // Rules & Events
    List<BusinessRule> findRulesByScreen(String screenId);
    List<BusinessRule> findRulesByColumn(String tableName, String columnName);

    List<Event> findEventsPublishedBy(String className, String methodName);
    List<Event> findEventsConsumedBy(String className, String methodName);

    // Relations & KB
    List<Relation> findRelationsFrom(String type, String id);
    List<Relation> findRelationsTo(String type, String id);

    List<KbArticle> searchKbByTag(String tag);
}
```

### 3.3. MetadataService (Domain API)

Trách nhiệm: gom nhiều truy vấn DAO thành **câu trả lời domain** cho UI + LLM.

Ví dụ:

```java
interface MetadataService {

    ScreenDetails getScreenDetails(String screenId);

    ColumnDetails getColumnDetails(String tableName, String columnName);

    ImpactSummary getImpactsForColumn(String tableName, String columnName);

    WorkflowOverview getWorkflowsForScreen(String screenId);

    ApprovalFlowDetails getApprovalFlowForScreen(String screenId);
}
```

Gợi ý các DTO:
- `ScreenDetails`
  - `Screen screen`
  - `List<ScreenComponent> components`
  - `List<Workflow> workflows`
  - `List<BusinessRule> rules`

- `ColumnDetails`
  - `DbColumn column`
  - `List<ColumnUsage> reads`
  - `List<ColumnUsage> writes`
  - `List<BusinessRule> rules`

- `ImpactSummary`
  - `List<Screen> screens`
  - `List<String> batchIds`
  - `List<String> reportIds`
  - `List<BusinessRule> rules`

- `ApprovalFlowDetails`
  - `Screen screen`
  - `Workflow workflow`
  - `List<WorkflowStep> steps`
  - `List<ColumnUsage> statusUpdates` (ví dụ: `APPROVAL_STATUS`)
  - `List<AuditLog> auditLogs`
  - `List<Event> events`

---

## 4. RAG Index Service

### 4.1. RagIndexRepository

DAO cho bảng `rag_documents` + `rag_embeddings`.

```java
interface RagIndexRepository {
    long insertDocument(RagDocument doc);
    void insertEmbedding(long documentId, RagEmbedding embedding);

    void deleteAllDocuments();
    void deleteDocumentsBySourceType(String sourceType);

    List<RagDocument> findDocumentsByScreen(String screenId);
    List<RagDocument> findDocumentsByTableColumn(String table, String column);
}
```

### 4.2. RagIndexService

Trách nhiệm:
- Build index từ SQLite metadata + source code.
- Tìm context theo câu hỏi + context màn hình.

```java
interface RagIndexService {

    // Rebuild toàn bộ index (gọi khi user chọn project mới hoặc bấm "Rebuild Knowledge")
    void rebuildIndex(ProjectScanConfig config);

    // Tìm context cho 1 câu hỏi, có thể kèm màn hình hiện tại
    RagSearchResult search(String question, Optional<String> currentScreenId);
}
```

`rebuildIndex` gợi ý các bước:
1. Xoá documents/embeddings cũ cho project hiện tại (nếu cần).
2. Sinh **fact documents** từ SQLite:
   - Cho mỗi screen:
     - Fact: “Screen {id} {name} thuộc module {module}, dùng bảng X, cột Y.”
   - Cho mỗi column:
     - Fact: “Column {table}.{column} được cập nhật bởi {class.method}, ảnh hưởng tới screens/batch/report/rules…”
   - Cho mỗi workflow, rule, event, KB.
3. Index source code:
   - Chunk theo class/method (sử dụng `rag.chunking` hiện có).
   - Mỗi chunk có metadata: `path`, `symbol`, `screen_id` (nếu có link), `table_name`, `column_name` (nếu parse được).
4. Gọi `LLMClient` (embedding API) hoặc Embedding Service nội bộ để sinh embedding cho mỗi document.
5. Lưu embedding vào `rag_embeddings`.

`search` gợi ý:
- Tạo embedding cho câu hỏi.
- Query top-k embeddings (cách thực hiện tuỳ engine bạn dùng; với SQLite có thể lưu vector dạng BLOB và dùng approximate search).
- Áp dụng filter:
  - Nếu có `currentScreenId` → ưu tiên documents cùng screen + các `column_impacts` liên quan.
  - Nếu nhận diện được tên bảng/cột từ câu hỏi → filter thêm theo `table_name`, `column_name`.
- Trả về `RagSearchResult`:
  - `List<RagContextChunk> chunks` (mỗi chunk chứa `content` + metadata).

---

## 5. LlmContextBuilder & LlmOrchestrator

### 5.1. LlmContextBuilder

Trách nhiệm: từ `question`, `currentScreenId`, `RagSearchResult`, `MetadataService` → build context text cho LLM.

```java
interface LlmContextBuilder {
    LlmContext buildContext(String question, Optional<String> currentScreenId);
}
```

`buildContext` gợi ý:
1. Gọi `RagIndexService.search(question, currentScreenId)` để lấy top-k chunks.
2. Nếu câu hỏi chứa tên bảng/cột:
   - Gọi `MetadataService.getColumnDetails(table, column)` + `getImpactsForColumn(...)`.
3. Nếu câu hỏi liên quan đến approve/flow:
   - Gọi `MetadataService.getApprovalFlowForScreen(currentScreenId)` (nếu có).
4. Kết hợp:
   - Một đoạn **system context** ngắn mô tả hệ thống.
   - Một đoạn **structured summary** (tự format bằng code từ DTO).
   - Các **chunks text** từ RAG (giới hạn token, ví dụ 4–10 chunk).

### 5.2. LlmOrchestrator

Trách nhiệm: là gateway duy nhất gửi prompt tới `LLMClient`.

```java
interface LlmOrchestrator {
    LlmAnswer answerUserQuestion(String question, Optional<String> currentScreenId);
}
```

Luồng:
1. Gọi `LlmContextBuilder.buildContext(...)`.
2. Tạo prompt:
   - `system`: Mô tả vai trò (“Bạn là kiến trúc sư hệ thống của PCM Desktop…”).
   - `context`: context structured + chunks.
   - `user`: câu hỏi gốc.
3. Gọi `LLMClient` với mode chat/complete.
4. (Tùy chọn) Cho phép LLM dùng tools:
   - `getImpactsForColumn`, `getScreenDetails`,… thông qua `MetadataService`.
5. Trả về `LlmAnswer`:
   - `String text` – câu trả lời.
   - `List<Reference>` – danh sách screen/table/column/method mà câu trả lời dựa vào.

---

## 6. Luồng Runtime Chính

### 6.1. Luồng 1 – Rebuild Knowledge

1. User chọn project source folder + DB metadata trong Settings.
2. App chạy job:
   - Scan DB/schema, insert vào `db_tables`, `db_columns`.
   - (Tùy cách nhập) Admin update screen/workflow/rules/events trong “Metadata Admin UI”.
3. Gọi `RagIndexService.rebuildIndex(config)`:
   - Sinh fact documents từ SQLite + source code.
   - Lưu documents + embeddings.

### 6.2. Luồng 2 – User hỏi về màn hình đang mở

1. Chat UI gửi:
   - `question`
   - `currentScreenId` (ví dụ `AR-APPROVAL-001`).
2. `LlmOrchestrator.answerUserQuestion(question, currentScreenId)`:
   - `LlmContextBuilder.buildContext(...)`:
     - `RagIndexService.search(question, currentScreenId)`.
     - `MetadataService.getScreenDetails(currentScreenId)`.
     - Nếu có từ khóa cột (vd. `EXPIRY_DATE`) → `getColumnDetails` + `getImpactsForColumn`.
   - Gọi `LLMClient` với context đó.
3. Chat UI hiển thị:
   - Câu trả lời.
   - Danh sách reference (screen, bảng/cột, method, workflow) để user nhảy tới.

### 6.3. Luồng 3 – User hỏi về cột/bảng/impact tổng quát

1. Chat UI gửi chỉ `question` (không có `currentScreenId`).
2. `LlmContextBuilder`:
   - Dùng `RagIndexService.search(question, Optional.empty())`.
   - Parse câu hỏi để tìm bảng/cột (regex/heuristic).
   - Gọi `MetadataService.getColumnDetails` + `getImpactsForColumn`.
3. LLM trả lời với focus vào impacts:
   - Screens nào bị ảnh hưởng.
   - Batch jobs nào.
   - Reports nào.
   - Rules nào.

---

## 7. Hướng phát triển tiếp

- Xây dựng **Metadata Admin UI**:
  - Tabs: Screens, DB Schema, Workflows, Rules, Events, KB.
  - Mỗi tab bind vào `MetadataService` để CRUD.
- Thêm **tools** cho LLM:
  - Cho phép LLM gọi `getImpactsForColumn`, `getScreenDetails`, `getRulesForScreen`,… khi cần chi tiết.
- Cải thiện **prompt templates**:
  - Template riêng cho:
    - Câu hỏi về flow (approve, process).
    - Câu hỏi về impact (change field/column).
    - Câu hỏi về architecture (mối quan hệ module/screen).

Với kiến trúc này, chỉ cần SQLite được cập nhật đầy đủ, LLM + RAG + LLMClient hiện có sẽ có đủ thông tin để trả lời các câu hỏi nghiệp vụ phức tạp một cách chính xác, có dẫn chiếu tới bảng/cột/method/screen cụ thể. 

