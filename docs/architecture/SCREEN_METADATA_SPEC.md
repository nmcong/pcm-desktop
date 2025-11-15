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

