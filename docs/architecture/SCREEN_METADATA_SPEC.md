# Screen & System Metadata Specification for LLM Integration

This document defines a standard format for describing screens, workflows, and data so that the LLM can reliably understand the relationships between UI screens, source code (BE/FE), and database objects. The goal is to make questions like:

- "Quy trình approve diễn ra như thế nào, lưu vào cột nào?"
- "Thay đổi cột ngày hết hạn ảnh hưởng tới nghiệp vụ nào?"

trở nên có thể trả lời chính xác dựa trên metadata + code + schema.

> Recommended storage format: one YAML file per screen (or subsystem) under `data/screens/`, plus optional knowledge base files under `data/kb/`.

---

## 1. Top-Level Screen Metadata

Each screen should have a unique identity and business context.

```yaml
screen_id: AR-APPROVAL-001          # Unique, stable identifier
name: Màn hình phê duyệt đơn hàng   # Human-readable name
module: ORDER                       # Logical module / subsystem
route: /order/approval              # URL route or navigation path (if applicable)
business_purpose: |                 # 2–5 lines describing why this screen exists
  Dùng để phê duyệt / từ chối đơn hàng.
  Người dùng có thể xem chi tiết đơn hàng, kiểm tra trạng thái,
  và thực hiện hành động phê duyệt hoặc từ chối.
tags:                               # Optional tags for retrieval
  - approval
  - order
  - backoffice
```

**Guidelines**
- `screen_id` nên mang tính “business-friendly” (ví dụ: `AR-APPROVAL`, `CUST-DETAIL`).
- `business_purpose` nên nói rõ đầu vào, đầu ra, và actor chính.

---

## 2. Frontend Mapping (UI → Handlers)

Mô tả cách các thành phần UI chính liên kết với code (JavaFX, web, v.v.).

```yaml
frontend:
  type: javafx                       # e.g., javafx, web-react, web-vue, angular
  entry_file: src/main/java/com/noteflix/pcm/order/ui/OrderApprovalView.java
  route: /order/approval             # Optional: UI route if web

  main_components:
    - id: btnApprove
      label: Phê duyệt
      type: button
      description: Nút phê duyệt đơn hàng hiện tại.
      action_handler:
        class: com.noteflix.pcm.order.ui.OrderApprovalController
        method: onApproveClicked

    - id: btnReject
      label: Từ chối
      type: button
      description: Nút từ chối đơn hàng.
      action_handler:
        class: com.noteflix.pcm.order.ui.OrderApprovalController
        method: onRejectClicked

    - id: txtExpiryDate
      label: Ngày hết hạn
      type: datepicker
      description: Ngày hết hạn của đơn hàng.
      bound_model:
        class: com.noteflix.pcm.order.model.OrderViewModel
        field: expiryDate
```

**Guidelines**
- Chỉ cần liệt kê các component quan trọng (buttons, fields) có liên quan đến logic nghiệp vụ.
- `action_handler` phải trỏ đến class/method thực trong code để LLM có thể mapping sang BE.

---

## 3. Backend Entry Points & Workflows

Định nghĩa entry points và luồng nghiệp vụ chính liên quan đến màn hình.

```yaml
backend:
  entry_points:
    - name: approveOrder
      description: Xử lý phê duyệt đơn hàng từ màn hình phê duyệt.
      type: service                 # e.g., service, controller, handler, job
      class: com.noteflix.pcm.order.service.OrderApprovalService
      method: approveOrder
      called_from:
        - com.noteflix.pcm.order.ui.OrderApprovalController::onApproveClicked

    - name: rejectOrder
      description: Xử lý từ chối đơn hàng.
      type: service
      class: com.noteflix.pcm.order.service.OrderApprovalService
      method: rejectOrder
      called_from:
        - com.noteflix.pcm.order.ui.OrderApprovalController::onRejectClicked

  workflows:
    - id: ORDER-APPROVAL-FLOW
      name: Quy trình phê duyệt đơn hàng
      description: |
        Luồng xử lý khi người dùng phê duyệt đơn hàng từ màn hình phê duyệt.
      triggered_by:
        - screen_id: AR-APPROVAL-001
          action: btnApprove.click
      steps:
        - id: validate_order_status
          description: Kiểm tra trạng thái đơn hàng có cho phép phê duyệt hay không.
        - id: check_approval_permission
          description: Kiểm tra quyền phê duyệt của user hiện tại.
        - id: update_order_status_to_approved
          description: Cập nhật trạng thái đơn hàng sang APPROVED.
          implementation:
            class: com.noteflix.pcm.order.repo.OrderRepository
            method: updateApprovalStatus
        - id: write_audit_log
          description: Ghi log audit cho hành động phê duyệt.
          implementation:
            class: com.noteflix.pcm.audit.AuditService
            method: logOrderApproval
        - id: publish_order_approved_event
          description: Publish domain event OrderApproved.
          implementation:
            class: com.noteflix.pcm.order.service.OrderApprovalService
            method: publishOrderApprovedEvent
```

**Guidelines**
- `workflows` nên mô tả rõ *sequence* chính, không cần quá chi tiết về từng dòng code.
- Rất quan trọng: mapping bước workflow → implementation (class/method) → database (xem phần tiếp theo).

---

## 4. Database Mapping (Tables, Columns, Impacts)

Đây là phần giúp LLM trả lời chính xác câu hỏi về cột/bảng và impact nghiệp vụ.

```yaml
database:
  main_entities:
    - table: ORDER
      schema: PUBLIC                  # Optional, if multi-schema
      description: Bảng lưu thông tin đơn hàng.
      key_columns:
        - ORDER_ID

      columns:
        - name: APPROVAL_STATUS
          type: VARCHAR(20)
          description: Trạng thái phê duyệt (PENDING/APPROVED/REJECTED).
          updated_by:
            - class: com.noteflix.pcm.order.repo.OrderRepository
              method: updateApprovalStatus
              reason: Cập nhật trạng thái khi phê duyệt/từ chối từ màn hình phê duyệt.
          read_by:
            - class: com.noteflix.pcm.order.service.OrderQueryService
              method: findOrdersForApproval
              reason: Lọc danh sách đơn cần phê duyệt.
          impacts:
            screens:
              - AR-APPROVAL-001
              - AR-ORDER-LIST-001
            batches:
              - JOB-AUTO-CANCEL-EXPIRED-ORDERS
            reports:
              - RPT-ORDER-APPROVAL-STATUS

        - name: EXPIRY_DATE
          type: DATE
          description: Ngày hết hạn đơn hàng.
          updated_by:
            - class: com.noteflix.pcm.order.service.OrderExpiryService
              method: updateExpiryDate
              reason: Người dùng thay đổi ngày hết hạn từ màn hình chi tiết đơn hàng.
          read_by:
            - class: com.noteflix.pcm.order.service.OrderExpiryService
              method: checkExpiredOrders
            - class: com.noteflix.pcm.report.OrderExpiryReportService
              method: generateExpiryReport
          impacts:
            screens:
              - AR-ORDER-DETAIL-001
              - AR-ORDER-LIST-001
            batches:
              - JOB-AUTO-CANCEL-EXPIRED-ORDERS
            reports:
              - RPT-ORDER-EXPIRY
            business_rules:
              - RULE-ORDER-EXPIRY-001
              - RULE-ORDER-CHANGE-EXPIRY-IMPACT
```

**Guidelines**
- Mỗi cột quan trọng (status, amount, expiry date, …) nên có:
  - `updated_by` rõ ràng (class/method + lý do).
  - `impacts` liệt kê màn hình, batch, báo cáo, rule liên quan.
- Điều này cho phép LLM trả lời:
  - “Cột này ai cập nhật?” → dựa vào `updated_by`.
  - “Thay đổi cột này ảnh hưởng tới đâu?” → dựa vào `impacts`.

---

## 5. Events & Audit Trails

Domain events và audit log giúp mô tả side effects khi một hành động diễn ra.

```yaml
events:
  domain_events:
    - name: OrderApproved
      description: Phát sinh khi đơn hàng được phê duyệt.
      published_by:
        class: com.noteflix.pcm.order.service.OrderApprovalService
        method: approveOrder
      consumed_by:
        - class: com.noteflix.pcm.notification.service.NotificationService
          method: sendApprovalNotification
        - class: com.noteflix.pcm.analytics.OrderAnalyticsService
          method: recordApprovalMetrics

  audit:
    - action: APPROVE_ORDER
      description: Ghi nhận hành động phê duyệt đơn hàng.
      log_table: ORDER_AUDIT_LOG
      fields:
        - ORDER_ID
        - ACTION
        - USER_ID
        - TIMESTAMP
      written_by:
        class: com.noteflix.pcm.audit.AuditService
        method: logOrderApproval
```

**Guidelines**
- Luôn mô tả event ở mức business (OrderApproved, PaymentCaptured, v.v.).
- Kết nối event với screen/workflow giúp LLM kể lại “toàn bộ câu chuyện” sau một hành động.

---

## 6. Business Rules & Constraints

Nơi mô tả các luật nghiệp vụ liên quan đến screen, cột, luồng.

```yaml
business_rules:
  - id: RULE-ORDER-EXPIRY-001
    name: Không cho phép approve đơn quá hạn
    description: |
      Nếu EXPIRY_DATE < ngày hiện tại, đơn hàng có trạng thái EXPIRED
      và không được phép APPROVE.
    related_tables:
      - ORDER
    related_columns:
      - ORDER.EXPIRY_DATE
      - ORDER.APPROVAL_STATUS
    enforced_in:
      - class: com.noteflix.pcm.order.service.OrderApprovalService
        method: approveOrder

  - id: RULE-ORDER-CHANGE-EXPIRY-IMPACT
    name: Ảnh hưởng khi thay đổi ngày hết hạn
    description: |
      Khi cập nhật EXPIRY_DATE:
      - Cần re-evaluate trạng thái đơn.
      - Có thể ảnh hưởng tới job auto-cancel.
      - Ảnh hưởng tới báo cáo đơn sắp hết hạn.
    related_tables:
      - ORDER
    related_columns:
      - ORDER.EXPIRY_DATE
    enforced_in:
      - class: com.noteflix.pcm.order.service.OrderExpiryService
        method: updateExpiryDate
```

**Guidelines**
- Mỗi rule nên:
  - Có `id` rõ ràng, dễ search.
  - Liên kết tới bảng/cột và nơi enforce trong code.
- Đặc biệt quan trọng với các câu hỏi “thay đổi field X thì sao?”.

---

## 7. Cross-Screen & Cross-System Links

Cho phép LLM hiểu một màn hình nằm trong bối cảnh lớn hơn.

```yaml
relations:
  related_screens:
    - screen_id: AR-ORDER-LIST-001
      relation_type: navigates_to
      description: Danh sách đơn hàng có thể mở màn hình phê duyệt.

    - screen_id: AR-ORDER-DETAIL-001
      relation_type: shows_details_for
      description: Màn hình chi tiết đơn hàng liên kết với phê duyệt.

  related_batches:
    - id: JOB-AUTO-CANCEL-EXPIRED-ORDERS
      description: Batch tự động cancel các đơn quá hạn.
      related_tables:
        - ORDER
      related_columns:
        - ORDER.EXPIRY_DATE
        - ORDER.APPROVAL_STATUS

  related_reports:
    - id: RPT-ORDER-APPROVAL-STATUS
      description: Báo cáo thống kê trạng thái phê duyệt đơn hàng.
      related_tables:
        - ORDER
      related_columns:
        - ORDER.APPROVAL_STATUS
```

**Guidelines**
- Dùng `relations` để nối nhiều screen, batch, report lại thành một “graph nghiệp vụ”.
- LLM có thể dùng graph này để trả lời câu hỏi impact ở cấp hệ thống.

---

## 8. Knowledge Base Articles (Optional)

Ngoài metadata màn hình, có thể thêm bài viết kiến thức tổng quát (KB) trong `data/kb/` dạng Markdown:

```markdown
---
id: KB-ORDER-EXPIRY-BUSINESS-LOGIC
tags: [order, expiry, approval]
related_rules:
  - RULE-ORDER-EXPIRY-001
  - RULE-ORDER-CHANGE-EXPIRY-IMPACT
related_screens:
  - AR-ORDER-DETAIL-001
  - AR-APPROVAL-001
---

# Logic nghiệp vụ cho ngày hết hạn đơn hàng

- Mục đích của `EXPIRY_DATE` là giới hạn thời gian hiệu lực của đơn.
- Nếu đơn quá hạn:
  - Không cho phép phê duyệt.
  - Batch auto-cancel sẽ chạy để hủy đơn.
- Các báo cáo "Đơn sắp hết hạn" phụ thuộc vào giá trị này.
```

Các file KB này sẽ được index giống như screen metadata và dùng làm context bổ sung cho LLM.

---

## 9. Best Practices Khi Viết Metadata

- Viết bằng ngôn ngữ gần với cách mà business và developer nói chuyện (có thể dùng tiếng Việt, Anh hoặc mix, nhưng nên thống nhất).
- Ưu tiên đầy đủ thông tin hơn là quá ngắn gọn, đặc biệt với:
  - Luồng phê duyệt, trạng thái, ngày tháng, số tiền.
  - Các cột có impact lớn (status, expiry, flags…).
- Luôn nối 3 tầng: **Screen → Code → Database**, và thêm **Rules/Events** nếu có.
- Cập nhật metadata cùng lúc với khi thay đổi logic (treat như code).

Khi mọi màn hình quan trọng đều có file metadata theo spec này, LLM + RAG có thể:
- Tìm được chính xác method/service/batch xử lý một hành động cụ thể.
- Biết cột nào được ghi/đọc, và những màn hình/báo cáo/job nào bị ảnh hưởng khi cột thay đổi.
- Trả lời câu hỏi nghiệp vụ một cách mạch lạc, có dẫn chiếu đến bảng/cột/method/screen cụ thể.

---

## 10. Storage Schema (SQLite) for Screen & System Metadata

Đây là schema gợi ý để lưu trữ metadata đã parse từ YAML/KB vào SQLite (hoặc bất kỳ DB quan hệ nào). Mục tiêu:
- Dễ query theo screen, table, column, rule, batch, report.
- Dễ dùng cho tools của LLM (function calling).

### 10.1. Screens & Components

```sql
CREATE TABLE screens (
    screen_id          TEXT PRIMARY KEY,
    name               TEXT NOT NULL,
    module             TEXT,
    route              TEXT,
    business_purpose   TEXT,
    tags               TEXT            -- JSON array hoặc chuỗi phân tách bằng dấu phẩy
);

CREATE TABLE screen_components (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    screen_id          TEXT NOT NULL REFERENCES screens(screen_id) ON DELETE CASCADE,
    component_id       TEXT NOT NULL,  -- vd: btnApprove, txtExpiryDate
    label              TEXT,
    type               TEXT,           -- button, textfield, datepicker...
    description        TEXT,
    handler_class      TEXT,
    handler_method     TEXT,
    model_class        TEXT,
    model_field        TEXT
);
```

### 10.2. Backend Entry Points & Workflows

```sql
CREATE TABLE backend_entry_points (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    name               TEXT NOT NULL,  -- approveOrder, rejectOrder...
    description        TEXT,
    type               TEXT,           -- service, controller, job...
    class              TEXT NOT NULL,
    method             TEXT NOT NULL,
    screen_id          TEXT            -- optional: màn hình chính gọi entry point này
        REFERENCES screens(screen_id) ON DELETE SET NULL
);

CREATE TABLE workflows (
    workflow_id        TEXT PRIMARY KEY,  -- ORDER-APPROVAL-FLOW
    name               TEXT NOT NULL,
    description        TEXT,
    module             TEXT,
    primary_screen_id  TEXT REFERENCES screens(screen_id) ON DELETE SET NULL
);

CREATE TABLE workflow_steps (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    workflow_id        TEXT NOT NULL REFERENCES workflows(workflow_id) ON DELETE CASCADE,
    step_order         INTEGER NOT NULL,  -- 1,2,3...
    step_id            TEXT,             -- validate_order_status...
    description        TEXT,
    impl_class         TEXT,
    impl_method        TEXT
);
```

### 10.3. Database Tables, Columns & Impacts

```sql
CREATE TABLE db_tables (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    name               TEXT NOT NULL,   -- ORDER
    schema_name        TEXT,            -- PUBLIC
    description        TEXT
);

CREATE TABLE db_columns (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    table_id           INTEGER NOT NULL REFERENCES db_tables(id) ON DELETE CASCADE,
    name               TEXT NOT NULL,   -- APPROVAL_STATUS, EXPIRY_DATE
    data_type          TEXT,
    description        TEXT
);

-- Liên kết class/method với column (đọc/ghi)
CREATE TABLE column_usage (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    column_id          INTEGER NOT NULL REFERENCES db_columns(id) ON DELETE CASCADE,
    usage_type         TEXT NOT NULL,   -- READ, WRITE
    class              TEXT NOT NULL,
    method             TEXT NOT NULL,
    reason             TEXT
);

-- Mô tả impact khi column thay đổi
CREATE TABLE column_impacts (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    column_id          INTEGER NOT NULL REFERENCES db_columns(id) ON DELETE CASCADE,
    impact_type        TEXT NOT NULL,   -- SCREEN, BATCH, REPORT, RULE
    target_id          TEXT NOT NULL,   -- screen_id, batch_id, report_id, rule_id...
    description        TEXT
);
```

### 10.4. Business Rules, Events & Audit

```sql
CREATE TABLE business_rules (
    rule_id            TEXT PRIMARY KEY, -- RULE-ORDER-EXPIRY-001
    name               TEXT NOT NULL,
    description        TEXT,
    notes              TEXT
);

CREATE TABLE business_rule_links (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id            TEXT NOT NULL REFERENCES business_rules(rule_id) ON DELETE CASCADE,
    link_type          TEXT NOT NULL,   -- TABLE, COLUMN, SCREEN, WORKFLOW
    table_name         TEXT,            -- optional nếu type=TABLE/COLUMN
    column_name        TEXT,
    screen_id          TEXT,
    workflow_id        TEXT
);

CREATE TABLE rule_enforcements (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    rule_id            TEXT NOT NULL REFERENCES business_rules(rule_id) ON DELETE CASCADE,
    class              TEXT NOT NULL,
    method             TEXT NOT NULL,
    description        TEXT
);

CREATE TABLE events (
    event_id           TEXT PRIMARY KEY, -- OrderApproved, PaymentCaptured...
    name               TEXT NOT NULL,
    description        TEXT,
    event_type         TEXT NOT NULL     -- DOMAIN, AUDIT
);

CREATE TABLE event_publishers (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id           TEXT NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
    class              TEXT NOT NULL,
    method             TEXT NOT NULL
);

CREATE TABLE event_consumers (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id           TEXT NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
    class              TEXT NOT NULL,
    method             TEXT NOT NULL
);

CREATE TABLE audit_logs (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    action             TEXT NOT NULL,    -- APPROVE_ORDER
    description        TEXT,
    log_table          TEXT NOT NULL,    -- ORDER_AUDIT_LOG
    fields             TEXT              -- JSON hoặc chuỗi: ORDER_ID, ACTION, USER_ID...
);

CREATE TABLE audit_writers (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    audit_id           INTEGER NOT NULL REFERENCES audit_logs(id) ON DELETE CASCADE,
    class              TEXT NOT NULL,
    method             TEXT NOT NULL
);
```

### 10.5. Relations & Knowledge Base Articles

```sql
CREATE TABLE relations (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    from_type          TEXT NOT NULL,    -- SCREEN, WORKFLOW, BATCH, REPORT
    from_id            TEXT NOT NULL,
    to_type            TEXT NOT NULL,    -- SCREEN, WORKFLOW, BATCH, REPORT
    to_id              TEXT NOT NULL,
    relation_type      TEXT NOT NULL,    -- navigates_to, shows_details_for, depends_on...
    description        TEXT
);

CREATE TABLE kb_articles (
    id                 TEXT PRIMARY KEY, -- KB-ORDER-EXPIRY-BUSINESS-LOGIC
    title              TEXT NOT NULL,
    body               TEXT,             -- có thể lưu nguyên văn hoặc chỉ path tới file md
    body_path          TEXT,
    tags               TEXT              -- JSON array hoặc chuỗi phân tách bằng dấu phẩy
);
```

---

## 11. RAG Index Schema (Documents & Embeddings)

Metadata ở trên là “truth source” quan hệ. Để LLM truy vấn nhanh và ít tốn token, nên có thêm schema cho index (document + embedding). Dưới đây là một thiết kế đơn giản nếu bạn dùng SQLite làm storage cho index (hoặc một lớp abstraction tương đương nếu dùng Qdrant/pgvector, v.v.).

### 11.1. Documents

```sql
CREATE TABLE rag_documents (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    source_type        TEXT NOT NULL,    -- SCREEN_YAML, KB, CODE, SCHEMA, GENERATED_FACT
    source_path        TEXT,             -- file path nếu có: src/main/java/..., data/screens/...
    anchor             TEXT,             -- symbol/mục nhỏ: OrderApprovalService.approveOrder, RULE-ORDER-EXPIRY-001
    screen_id          TEXT,             -- liên kết nhanh tới screen nếu có
    table_name         TEXT,
    column_name        TEXT,
    rule_id            TEXT,
    event_id           TEXT,
    workflow_id        TEXT,
    summary            TEXT,             -- tóm tắt ngắn
    content            TEXT NOT NULL     -- đoạn text đầy đủ mà LLM sẽ thấy
);
```

### 11.2. Embeddings

```sql
CREATE TABLE rag_embeddings (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    document_id        INTEGER NOT NULL REFERENCES rag_documents(id) ON DELETE CASCADE,
    provider           TEXT NOT NULL,    -- openai, anthropic, local...
    model              TEXT NOT NULL,    -- tên model embedding
    dim                INTEGER NOT NULL,
    vector             BLOB NOT NULL     -- lưu vector dạng BLOB (hoặc JSON nếu cần)
);

CREATE INDEX idx_rag_embeddings_doc ON rag_embeddings(document_id);
CREATE INDEX idx_rag_documents_screen ON rag_documents(screen_id);
CREATE INDEX idx_rag_documents_table_col ON rag_documents(table_name, column_name);
CREATE INDEX idx_rag_documents_rule ON rag_documents(rule_id);
```

### 11.3. Cách LLM sử dụng schema này

- Khi **index**:
  - Parse YAML + KB + code + schema.
  - Sinh các đoạn “fact” dạng text (ngắn, rõ, ngôn ngữ tự nhiên).
  - Lưu mỗi fact vào `rag_documents` (kèm metadata: `screen_id`, `table_name`, `column_name`, `rule_id`...).
  - Tạo embedding tương ứng trong `rag_embeddings`.

- Khi **trả lời câu hỏi**:
  - Xác định context sơ bộ (screen hiện tại, từ khóa bảng/cột trong câu hỏi).
  - Query `rag_embeddings` để lấy top-k documents (kết hợp filter metadata).
  - Lấy `content` từ `rag_documents` → gửi vào LLM như context.
  - Nếu cần chi tiết hơn, LLM có thể gọi tools:
    - `get_screens_affecting_column(table, column)` → query `column_impacts`, `screens`, `relations`.
    - `get_workflow_for_action(screen_id, action_id)` → query `workflows`, `workflow_steps`, `screen_components`.

Với schema này:
- Bạn có cấu trúc rõ ràng để lưu **mối quan hệ** (screen ↔ code ↔ database ↔ rules ↔ events).
- Bạn có index RAG hiệu quả để **giảm token** và tăng độ chính xác khi LLM trả lời các câu hỏi nghiệp vụ phức tạp như:
  - “Approve xử lý thế nào, viết vào bảng/cột nào?”
  - “Thay đổi EXPIRY_DATE ảnh hưởng tới màn hình, batch, báo cáo nào?”.

