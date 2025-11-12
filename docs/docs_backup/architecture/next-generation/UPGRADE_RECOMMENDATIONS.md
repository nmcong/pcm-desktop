# PCM WebApp - Nâng Cấp Khuyến Nghị

**Ngày tạo:** 2025-11-07  
**Phạm vi:** Hệ thống AI Function Calling & Query Tools  
**Mục tiêu:** Tăng khả năng tìm kiếm và trả lời chính xác từ nhiều dạng câu hỏi người dùng

---

## 📋 Tổng Quan

Tài liệu này đề xuất các nâng cấp cho hệ thống `pcm-webapp` nhằm:

1. **Mở rộng khả năng tìm kiếm** - Thêm các công cụ mới để xử lý đa dạng câu hỏi
2. **Cải thiện độ chính xác** - Giảm hallucination, tăng grounding với dữ liệu thực
3. **Tối ưu hiệu năng** - Quản lý token, caching, semantic search
4. **Trải nghiệm người dùng** - Intent detection, context injection, multi-turn conversation

---

## 🎯 Phần 1: Công Cụ Bổ Sung (Additional Tools)

### 1.1. Công Cụ Tìm Kiếm Nâng Cao

#### **Tool: `fuzzy_search`**

Tìm kiếm xấp xỉ cho các trường hợp người dùng nhập sai chính tả, viết tắt, hoặc từ đồng nghĩa.

```javascript
{
  name: "fuzzy_search",
  description: "Tìm kiếm xấp xỉ project/screen/subsystem khi không có kết quả chính xác. Hỗ trợ typo, viết tắt, từ đồng nghĩa.",
  parameters: {
    type: "object",
    properties: {
      query: {
        type: "string",
        description: "Từ khóa tìm kiếm (có thể không chính xác)"
      },
      entity_type: {
        type: "string",
        enum: ["project", "screen", "subsystem", "all"],
        description: "Loại entity cần tìm"
      },
      threshold: {
        type: "number",
        description: "Ngưỡng tương đồng (0.0-1.0), mặc định 0.6",
        default: 0.6
      }
    },
    required: ["query"]
  }
}
```

**Triển khai:**

- Tích hợp thư viện `Fuse.js` (~5KB) cho fuzzy search
- Sử dụng Levenshtein distance cho tính toán độ tương đồng
- Lưu cache index trong `sessionStorage` để tối ưu hiệu năng

**Ví dụ sử dụng:**

```
User: "Tìm dự án hoàn tền"
→ fuzzy_search({query: "hoàn tền", entity_type: "project"})
→ Kết quả: ["Hoàn tiền", "Refund System", "Payment Recovery"]
```

---

#### **Tool: `search_by_multiple_criteria`**

Tìm kiếm kết hợp nhiều tiêu chí (technology + subsystem + keyword).

```javascript
{
  name: "search_by_multiple_criteria",
  description: "Tìm kiếm nâng cao với nhiều tiêu chí kết hợp: technology stack, subsystem, date range, tags.",
  parameters: {
    type: "object",
    properties: {
      technologies: {
        type: "array",
        items: { type: "string" },
        description: "Danh sách technology (Java, React, JSP, ...)"
      },
      subsystem_ids: {
        type: "array",
        items: { type: "number" },
        description: "Danh sách subsystem IDs"
      },
      keywords: {
        type: "array",
        items: { type: "string" },
        description: "Từ khóa trong tên/description"
      },
      has_workflows: {
        type: "boolean",
        description: "Chỉ lấy project có workflows"
      },
      favorite_only: {
        type: "boolean",
        description: "Chỉ lấy project được đánh dấu favorite"
      },
      date_range: {
        type: "object",
        properties: {
          from: { type: "string", format: "date" },
          to: { type: "string", format: "date" }
        }
      }
    }
  }
}
```

**Ví dụ sử dụng:**

```
User: "Dự án nào dùng React và liên quan đến payment?"
→ search_by_multiple_criteria({
    technologies: ["React"],
    keywords: ["payment", "thanh toán"]
  })
```

---

#### **Tool: `semantic_search`**

Tìm kiếm ngữ nghĩa (semantic search) không cần vector DB - sử dụng synonyms dictionary.

```javascript
{
  name: "semantic_search",
  description: "Tìm kiếm theo ngữ nghĩa, tự động mở rộng từ đồng nghĩa và các biến thể.",
  parameters: {
    type: "object",
    properties: {
      query: {
        type: "string",
        description: "Câu truy vấn ngữ nghĩa"
      },
      expand_synonyms: {
        type: "boolean",
        description: "Tự động mở rộng từ đồng nghĩa",
        default: true
      },
      language: {
        type: "string",
        enum: ["vi", "en", "both"],
        description: "Ngôn ngữ tìm kiếm",
        default: "both"
      }
    },
    required: ["query"]
  }
}
```

**Triển khai:**

- File `synonyms.json` chứa mapping từ khóa → từ đồng nghĩa
- Normalize text (bỏ dấu, lowercase) trước khi tìm
- Hỗ trợ cả tiếng Việt và tiếng Anh

**Ví dụ `synonyms.json`:**

```json
{
  "refund": ["hoàn tiền", "trả tiền", "refund", "return payment"],
  "authentication": ["xác thực", "đăng nhập", "auth", "login"],
  "payment": ["thanh toán", "payment", "pay", "chi trả"],
  "approval": ["phê duyệt", "duyệt", "approval", "approve"],
  "risk": ["rủi ro", "risk", "fraud", "gian lận"]
}
```

---

### 1.2. Công Cụ Phân Tích & Thống Kê

#### **Tool: `get_statistics`**

Cung cấp thống kê tổng quan về hệ thống.

```javascript
{
  name: "get_statistics",
  description: "Lấy thống kê tổng quan: số lượng projects, screens, events, workflows, technologies.",
  parameters: {
    type: "object",
    properties: {
      group_by: {
        type: "string",
        enum: ["subsystem", "technology", "status", "overall"],
        description: "Nhóm thống kê theo tiêu chí",
        default: "overall"
      },
      include_details: {
        type: "boolean",
        description: "Bao gồm chi tiết từng nhóm",
        default: false
      }
    }
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "statistics": {
    "total_subsystems": 5,
    "total_projects": 23,
    "total_screens": 187,
    "total_events": 452,
    "top_technologies": ["Java", "React", "JSP"],
    "favorite_projects": 8,
    "by_subsystem": {
      "Revenue Recovery & Refunds": { "projects": 7, "screens": 65 },
      "Compliance & Audit": { "projects": 4, "screens": 38 }
    }
  }
}
```

---

#### **Tool: `analyze_relationships`**

Phân tích mối quan hệ giữa các entities (project → screens → events).

```javascript
{
  name: "analyze_relationships",
  description: "Phân tích mối quan hệ và dependencies giữa projects, screens, và workflows.",
  parameters: {
    type: "object",
    properties: {
      entity_type: {
        type: "string",
        enum: ["project", "screen"],
        description: "Loại entity gốc"
      },
      entity_id: {
        type: "number",
        description: "ID của entity"
      },
      depth: {
        type: "number",
        description: "Độ sâu phân tích (1-3)",
        default: 2
      }
    },
    required: ["entity_type", "entity_id"]
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "entity": { "type": "screen", "id": 45, "name": "Risk Review Screen" },
  "relationships": {
    "belongs_to_project": { "id": 12, "name": "Refund Intake" },
    "triggers_screens": [
      {
        "id": 47,
        "name": "Approval Screen",
        "via_event": "Submit for Approval"
      }
    ],
    "triggered_by_screens": [
      { "id": 43, "name": "Initial Request", "via_event": "Review Risk" }
    ],
    "uses_database_tables": ["refund_requests", "risk_scores"],
    "uses_source_files": ["RiskReviewPage.tsx", "RiskService.java"]
  }
}
```

---

#### **Tool: `find_similar_entities`**

Tìm entities tương tự dựa trên metadata (technology, structure, naming pattern).

```javascript
{
  name: "find_similar_entities",
  description: "Tìm projects/screens tương tự dựa trên technology stack, naming pattern, và structure.",
  parameters: {
    type: "object",
    properties: {
      reference_entity_type: {
        type: "string",
        enum: ["project", "screen"]
      },
      reference_entity_id: {
        type: "number",
        description: "ID của entity tham chiếu"
      },
      similarity_criteria: {
        type: "array",
        items: {
          type: "string",
          enum: ["technology", "structure", "naming", "workflow"]
        },
        description: "Tiêu chí đánh giá độ tương đồng"
      },
      limit: {
        type: "number",
        description: "Số lượng kết quả tối đa",
        default: 5
      }
    },
    required: ["reference_entity_type", "reference_entity_id"]
  }
}
```

---

### 1.3. Công Cụ Hỗ Trợ Development

#### **Tool: `find_source_by_feature`**

Tìm source code và database tables liên quan đến một tính năng.

```javascript
{
  name: "find_source_by_feature",
  description: "Tìm source files và database tables liên quan đến một feature/requirement. Hỗ trợ gợi ý file cần sửa.",
  parameters: {
    type: "object",
    properties: {
      feature_keywords: {
        type: "array",
        items: { type: "string" },
        description: "Từ khóa mô tả feature (vd: 'risk review', 'approval flow')"
      },
      include_related: {
        type: "boolean",
        description: "Bao gồm các file/tables liên quan gián tiếp",
        default: true
      },
      confidence_threshold: {
        type: "number",
        description: "Ngưỡng độ tin cậy (0.0-1.0)",
        default: 0.7
      }
    },
    required: ["feature_keywords"]
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "feature": "Risk Review Approval",
  "matched_screens": [
    {
      "screen_id": 45,
      "screen_name": "Risk Review Screen",
      "confidence": 0.95,
      "source_files": [
        {
          "path": "apps/refund-intake/src/pages/RiskReviewPage.tsx",
          "type": "React Component"
        },
        {
          "path": "services/risk-service/src/RiskService.java",
          "type": "Backend Service"
        }
      ],
      "database_tables": [
        {
          "table": "refund_requests",
          "columns": ["risk_score", "risk_status"]
        },
        { "table": "risk_audit_logs", "columns": ["*"] }
      ]
    }
  ],
  "suggestions": {
    "files_to_modify": [
      "apps/refund-intake/src/pages/RiskReviewPage.tsx",
      "services/risk-service/src/api/RiskController.java"
    ],
    "database_changes": ["Có thể cần migration cho bảng 'refund_requests'"]
  }
}
```

---

#### **Tool: `get_change_impact`**

Phân tích impact khi thay đổi một entity.

```javascript
{
  name: "get_change_impact",
  description: "Phân tích impact khi thay đổi một screen/project: các screens liên quan, workflows bị ảnh hưởng.",
  parameters: {
    type: "object",
    properties: {
      entity_type: {
        type: "string",
        enum: ["project", "screen", "database_table"]
      },
      entity_identifier: {
        type: "string",
        description: "ID hoặc tên của entity"
      },
      change_type: {
        type: "string",
        enum: ["modify", "delete", "rename"],
        description: "Loại thay đổi"
      }
    },
    required: ["entity_type", "entity_identifier"]
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "impact_analysis": {
    "affected_screens": 3,
    "affected_workflows": 2,
    "affected_projects": 1,
    "details": {
      "screens": [
        {
          "id": 47,
          "name": "Approval Screen",
          "reason": "Receives navigation from this screen"
        }
      ],
      "workflows": [{ "id": 5, "name": "Standard Refund Flow", "step": 3 }]
    },
    "risk_level": "medium",
    "recommendations": [
      "Cập nhật navigation path trong Approval Screen",
      "Test lại workflow 'Standard Refund Flow'"
    ]
  }
}
```

---

#### **Tool: `get_recent_changes`**

Lấy lịch sử thay đổi gần đây (metadata changes, không phải git history).

```javascript
{
  name: "get_recent_changes",
  description: "Lấy danh sách các thay đổi gần đây trong PCM system (thêm/sửa/xóa projects, screens).",
  parameters: {
    type: "object",
    properties: {
      entity_type: {
        type: "string",
        enum: ["all", "project", "screen", "subsystem"]
      },
      days: {
        type: "number",
        description: "Số ngày trở về trước",
        default: 7
      },
      limit: {
        type: "number",
        description: "Số lượng kết quả",
        default: 20
      }
    }
  }
}
```

---

### 1.4. Công Cụ Context & Workflow

#### **Tool: `get_workflow_details`**

Lấy chi tiết workflow bao gồm BPMN diagram và conditions.

```javascript
{
  name: "get_workflow_details",
  description: "Lấy chi tiết workflow với BPMN diagram, branch conditions, và screen transitions.",
  parameters: {
    type: "object",
    properties: {
      workflow_id: {
        type: "number",
        description: "ID của workflow"
      },
      include_bpmn: {
        type: "boolean",
        description: "Bao gồm BPMN XML/JSON",
        default: false
      }
    },
    required: ["workflow_id"]
  }
}
```

---

#### **Tool: `trace_user_journey`**

Trace hành trình người dùng qua các screens.

```javascript
{
  name: "trace_user_journey",
  description: "Trace đường đi của user từ screen A đến screen B, hiển thị tất cả paths có thể.",
  parameters: {
    type: "object",
    properties: {
      from_screen_id: {
        type: "number",
        description: "Screen bắt đầu"
      },
      to_screen_id: {
        type: "number",
        description: "Screen đích"
      },
      max_depth: {
        type: "number",
        description: "Độ sâu tối đa (số screens trung gian)",
        default: 5
      }
    },
    required: ["from_screen_id", "to_screen_id"]
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "paths_found": 2,
  "paths": [
    {
      "length": 3,
      "screens": [
        { "id": 10, "name": "Login" },
        { "id": 12, "name": "Dashboard" },
        { "id": 25, "name": "Refund Request" }
      ],
      "transitions": [
        { "event": "Login Success", "condition": null },
        { "event": "Create Refund", "condition": null }
      ]
    },
    {
      "length": 4,
      "screens": [
        { "id": 10, "name": "Login" },
        { "id": 12, "name": "Dashboard" },
        { "id": 15, "name": "Menu" },
        { "id": 25, "name": "Refund Request" }
      ],
      "transitions": [
        { "event": "Login Success", "condition": null },
        { "event": "Open Menu", "condition": null },
        { "event": "Select Refund", "condition": null }
      ]
    }
  ]
}
```

---

#### **Tool: `summarize_conversation`**

Tóm tắt cuộc hội thoại để quản lý token limit.

```javascript
{
  name: "summarize_conversation",
  description: "Tóm tắt nội dung cuộc hội thoại thành bullet points với citations, dùng để quản lý token limit.",
  parameters: {
    type: "object",
    properties: {
      conversation_id: {
        type: "string",
        description: "ID của conversation cần tóm tắt"
      },
      preserve_data: {
        type: "boolean",
        description: "Giữ lại dữ liệu quan trọng (tool results)",
        default: true
      }
    },
    required: ["conversation_id"]
  }
}
```

---

### 1.5. Công Cụ Validation & Quality

#### **Tool: `validate_screen_completeness`**

Kiểm tra độ đầy đủ của screen metadata.

```javascript
{
  name: "validate_screen_completeness",
  description: "Kiểm tra screen có đầy đủ metadata: description, events, source files, permissions.",
  parameters: {
    type: "object",
    properties: {
      screen_id: {
        type: "number",
        description: "ID của screen cần validate"
      },
      strict_mode: {
        type: "boolean",
        description: "Mode nghiêm ngặt (yêu cầu tất cả fields)",
        default: false
      }
    },
    required: ["screen_id"]
  }
}
```

**Output ví dụ:**

```json
{
  "success": true,
  "screen": { "id": 45, "name": "Risk Review" },
  "completeness_score": 0.75,
  "validation": {
    "has_description": true,
    "has_events": true,
    "has_source_files": true,
    "has_permissions": false,
    "has_database_tables": true,
    "has_screenshots": false
  },
  "recommendations": [
    "Thêm permissions để xác định role nào truy cập được",
    "Upload screenshots để dễ hiểu UI"
  ]
}
```

---

#### **Tool: `detect_data_gaps`**

Phát hiện gaps trong dữ liệu (screens không có events, projects không có workflows).

```javascript
{
  name: "detect_data_gaps",
  description: "Phát hiện các gaps trong dữ liệu: screens thiếu events, projects thiếu workflows, source files không tồn tại.",
  parameters: {
    type: "object",
    properties: {
      scope: {
        type: "string",
        enum: ["all", "project", "subsystem"],
        description: "Phạm vi kiểm tra"
      },
      entity_id: {
        type: "number",
        description: "ID của project/subsystem (nếu scope không phải 'all')"
      }
    }
  }
}
```

---

## 🎯 Phần 2: Cải Thiện Intent Detection

### 2.1. Query Intent Classification

Tạo hệ thống phân loại intent tự động để chọn tool phù hợp.

**Các loại intent:**

| Intent Category | Example Queries                                               | Primary Tools                                                      |
| --------------- | ------------------------------------------------------------- | ------------------------------------------------------------------ |
| **Search**      | "Tìm dự án liên quan payment", "Screen nào xử lý risk?"       | `search_projects`, `search_screens`, `fuzzy_search`                |
| **Details**     | "Chi tiết project Refund", "Screen 45 làm gì?"                | `get_project_details`, `get_screen_events`                         |
| **Analysis**    | "Workflow hoàn tiền như thế nào?", "Impact khi sửa screen X?" | `trace_user_journey`, `get_change_impact`, `analyze_relationships` |
| **Statistics**  | "Bao nhiêu project dùng React?", "Thống kê subsystem"         | `get_statistics`, `search_by_technology`                           |
| **Development** | "File nào xử lý approval?", "Cần sửa gì cho feature X?"       | `find_source_by_feature`, `get_change_impact`                      |
| **Validation**  | "Screen nào chưa hoàn chỉnh?", "Dữ liệu có gaps không?"       | `validate_screen_completeness`, `detect_data_gaps`                 |

**Triển khai:**

```javascript
// IntentDetector.js
export class IntentDetector {
  constructor() {
    this.patterns = {
      search: [/tìm|search|find|có .* nào/i, /dự án|project|screen|màn hình/i],
      details: [
        /chi tiết|detail|thông tin|information/i,
        /làm gì|what does|how does/i,
      ],
      analysis: [
        /workflow|luồng|flow|journey|đường đi/i,
        /impact|ảnh hưởng|liên quan|related/i,
      ],
      statistics: [
        /bao nhiêu|how many|count|thống kê|statistics/i,
        /tổng|total|danh sách|list all/i,
      ],
      development: [
        /file|source|code|sửa|modify|fix/i,
        /cần|need to|phải|should/i,
      ],
      validation: [
        /kiểm tra|check|validate|verify/i,
        /thiếu|missing|gap|incomplete/i,
      ],
    };
  }

  detectIntent(query) {
    const scores = {};
    const queryLower = query.toLowerCase();

    for (const [intent, patterns] of Object.entries(this.patterns)) {
      let score = 0;
      for (const pattern of patterns) {
        if (pattern.test(queryLower)) {
          score++;
        }
      }
      scores[intent] = score;
    }

    // Get intent with highest score
    const maxScore = Math.max(...Object.values(scores));
    if (maxScore === 0) return "general"; // No specific intent

    const detectedIntent = Object.keys(scores).find(
      (intent) => scores[intent] === maxScore,
    );

    return {
      primary: detectedIntent,
      confidence: maxScore / this.patterns[detectedIntent].length,
      scores: scores,
    };
  }

  suggestTools(intent) {
    const toolMapping = {
      search: [
        "search_projects",
        "search_screens",
        "fuzzy_search",
        "semantic_search",
      ],
      details: [
        "get_project_details",
        "get_screen_events",
        "get_workflow_details",
      ],
      analysis: [
        "analyze_relationships",
        "trace_user_journey",
        "get_change_impact",
      ],
      statistics: ["get_statistics", "list_subsystems", "search_by_technology"],
      development: [
        "find_source_by_feature",
        "get_change_impact",
        "find_similar_entities",
      ],
      validation: ["validate_screen_completeness", "detect_data_gaps"],
      general: ["list_subsystems", "get_statistics"], // Fallback tools
    };

    return toolMapping[intent.primary] || toolMapping.general;
  }
}
```

---

### 2.2. Context Pre-Injection Strategy

Chiến lược inject context tự động dựa trên intent.

```javascript
// ContextInjector.js
export class ContextInjector {
  constructor(databaseQueryTool) {
    this.queryTool = databaseQueryTool;
    this.intentDetector = new IntentDetector();
  }

  async injectContext(userMessage, conversationHistory = []) {
    const intent = this.intentDetector.detectIntent(userMessage);
    let injectedContext = "";

    // Extract entities from message
    const entities = this.extractEntities(userMessage);

    // Inject based on intent
    switch (intent.primary) {
      case "search":
        if (entities.subsystem) {
          const subsystems = await this.queryTool.listSubsystems();
          injectedContext += `\n\n**Subsystems Context:**\n${JSON.stringify(subsystems, null, 2)}`;
        }
        break;

      case "details":
        if (entities.projectId || entities.screenId) {
          // Inject specific entity details
          if (entities.projectId) {
            const project = await this.queryTool.getProjectDetails({
              project_id: entities.projectId,
            });
            injectedContext += `\n\n**Project Context:**\n${JSON.stringify(project, null, 2)}`;
          }
        }
        break;

      case "analysis":
        // Always inject workflow overview for analysis queries
        injectedContext += await this.getWorkflowOverview();
        break;

      case "statistics":
        // Inject high-level statistics
        const stats = await this.queryTool.getStatistics();
        injectedContext += `\n\n**Statistics Context:**\n${JSON.stringify(stats, null, 2)}`;
        break;

      case "general":
        // Inject minimal context (subsystem list only)
        const subsystemsList = await this.queryTool.listSubsystems();
        injectedContext += `\n\n**Available Subsystems:**\n${JSON.stringify(
          subsystemsList.subsystems.map((s) => s.name),
          null,
          2,
        )}`;
        break;
    }

    return {
      enhancedMessage: userMessage + injectedContext,
      intent: intent,
      suggestedTools: this.intentDetector.suggestTools(intent),
    };
  }

  extractEntities(message) {
    // Extract project IDs, screen IDs, keywords
    const projectIdMatch = message.match(/project[:\s]+(\d+)/i);
    const screenIdMatch = message.match(/screen[:\s]+(\d+)/i);

    return {
      projectId: projectIdMatch ? parseInt(projectIdMatch[1]) : null,
      screenId: screenIdMatch ? parseInt(screenIdMatch[1]) : null,
      subsystem: message.match(/subsystem|hệ thống con/i) !== null,
    };
  }

  async getWorkflowOverview() {
    // Provide workflow overview for analysis
    return `\n\n**Workflow Analysis Context:**\nUse 'trace_user_journey' to find paths between screens.`;
  }
}
```

---

## 🎯 Phần 3: Semantic Search Improvements

### 3.1. Synonyms Dictionary System

Tạo file `public/data/synonyms.json`:

```json
{
  "version": "1.0.0",
  "last_updated": "2025-11-07",
  "categories": {
    "finance": {
      "refund": [
        "hoàn tiền",
        "trả tiền",
        "refund",
        "return payment",
        "chargeback"
      ],
      "payment": ["thanh toán", "payment", "pay", "chi trả", "transaction"],
      "invoice": ["hóa đơn", "invoice", "bill", "receipt"],
      "revenue": ["doanh thu", "revenue", "income"],
      "fee": ["phí", "fee", "charge", "cost"]
    },
    "workflow": {
      "approval": ["phê duyệt", "duyệt", "approval", "approve", "authorize"],
      "review": ["xem xét", "review", "check", "verify", "kiểm tra"],
      "submit": ["nộp", "submit", "send", "gửi"],
      "reject": ["từ chối", "reject", "deny", "decline"]
    },
    "security": {
      "authentication": [
        "xác thực",
        "authentication",
        "auth",
        "login",
        "đăng nhập"
      ],
      "authorization": [
        "phân quyền",
        "authorization",
        "permission",
        "quyền hạn"
      ],
      "risk": ["rủi ro", "risk", "fraud", "gian lận", "security"],
      "audit": ["kiểm toán", "audit", "compliance", "tuân thủ"]
    },
    "technology": {
      "frontend": ["frontend", "client", "UI", "giao diện", "web"],
      "backend": ["backend", "server", "API", "service", "dịch vụ"],
      "database": ["database", "DB", "cơ sở dữ liệu", "table", "bảng"],
      "react": ["react", "reactjs", "jsx", "tsx"],
      "java": ["java", "spring", "springboot", "backend"]
    },
    "general": {
      "user": ["người dùng", "user", "customer", "khách hàng", "client"],
      "admin": ["quản trị", "admin", "administrator", "manager"],
      "system": ["hệ thống", "system", "platform", "nền tảng"],
      "data": ["dữ liệu", "data", "information", "thông tin"]
    }
  },
  "abbreviations": {
    "auth": "authentication",
    "DB": "database",
    "UI": "user interface",
    "API": "application programming interface",
    "BPMN": "business process model notation",
    "PCM": "project & compliance management"
  },
  "common_typos": {
    "hoan tien": "hoàn tiền",
    "thanh toan": "thanh toán",
    "phe duyet": "phê duyệt",
    "xac thuc": "xác thực"
  }
}
```

---

### 3.2. Fuzzy Search Implementation

Tích hợp Fuse.js cho fuzzy search:

```javascript
// FuzzySearchService.js
import Fuse from "fuse.js";

// Cần cài đặt: npm install fuse.js

export class FuzzySearchService {
  constructor(databaseManager) {
    this.databaseManager = databaseManager;
    this.indexCache = null;
    this.synonymsDict = null;
  }

  async initialize() {
    // Load synonyms dictionary
    const response = await fetch("/public/data/synonyms.json");
    this.synonymsDict = await response.json();

    // Build search index
    await this.buildIndex();
  }

  async buildIndex() {
    const projects = await this.databaseManager.getProjects();
    const screens = await this.databaseManager.getScreens();
    const subsystems = await this.databaseManager.getSubsystems();

    this.indexCache = {
      projects: new Fuse(projects, {
        keys: ["name", "description", "shortName"],
        threshold: 0.4,
        includeScore: true,
        useExtendedSearch: true,
      }),
      screens: new Fuse(screens, {
        keys: ["name", "description", "notes"],
        threshold: 0.4,
        includeScore: true,
        useExtendedSearch: true,
      }),
      subsystems: new Fuse(subsystems, {
        keys: ["name", "description"],
        threshold: 0.3,
        includeScore: true,
        useExtendedSearch: true,
      }),
    };

    console.log("✅ Fuzzy search index built");
  }

  expandQueryWithSynonyms(query) {
    const queryLower = query.toLowerCase().trim();
    let expandedTerms = [queryLower];

    // Check each category
    for (const category of Object.values(this.synonymsDict.categories)) {
      for (const [key, synonyms] of Object.entries(category)) {
        // If query matches any synonym, include all synonyms
        if (synonyms.some((syn) => queryLower.includes(syn.toLowerCase()))) {
          expandedTerms.push(...synonyms.map((s) => s.toLowerCase()));
        }
      }
    }

    // Check abbreviations
    for (const [abbr, full] of Object.entries(
      this.synonymsDict.abbreviations,
    )) {
      if (queryLower.includes(abbr.toLowerCase())) {
        expandedTerms.push(full.toLowerCase());
      }
    }

    // Remove duplicates
    return [...new Set(expandedTerms)];
  }

  async fuzzySearch(query, entityType = "all", options = {}) {
    if (!this.indexCache) {
      await this.initialize();
    }

    const { threshold = 0.6, limit = 10, expandSynonyms = true } = options;

    // Expand query with synonyms
    const searchTerms = expandSynonyms
      ? this.expandQueryWithSynonyms(query)
      : [query];

    const results = [];

    // Search across expanded terms
    for (const term of searchTerms) {
      if (entityType === "all" || entityType === "project") {
        const projectResults = this.indexCache.projects.search(term);
        results.push(
          ...projectResults.map((r) => ({
            ...r.item,
            type: "project",
            score: r.score,
            matchedTerm: term,
          })),
        );
      }

      if (entityType === "all" || entityType === "screen") {
        const screenResults = this.indexCache.screens.search(term);
        results.push(
          ...screenResults.map((r) => ({
            ...r.item,
            type: "screen",
            score: r.score,
            matchedTerm: term,
          })),
        );
      }

      if (entityType === "all" || entityType === "subsystem") {
        const subsystemResults = this.indexCache.subsystems.search(term);
        results.push(
          ...subsystemResults.map((r) => ({
            ...r.item,
            type: "subsystem",
            score: r.score,
            matchedTerm: term,
          })),
        );
      }
    }

    // Deduplicate by ID and type
    const uniqueResults = [];
    const seen = new Set();

    for (const result of results) {
      const key = `${result.type}-${result.id}`;
      if (!seen.has(key)) {
        seen.add(key);
        uniqueResults.push(result);
      }
    }

    // Sort by score and filter by threshold
    return uniqueResults
      .filter((r) => 1 - r.score >= threshold)
      .sort((a, b) => a.score - b.score)
      .slice(0, limit);
  }

  normalizeText(text) {
    // Remove Vietnamese accents
    return text
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase()
      .trim();
  }
}

export default new FuzzySearchService(databaseManager);
```

---

### 3.3. Tích hợp vào DatabaseQueryTool

Thêm fuzzy search tools vào `DatabaseQueryTool.js`:

```javascript
// Thêm vào defineFunctions()
{
  name: "fuzzy_search",
  description: "Tìm kiếm xấp xỉ khi không có kết quả chính xác. Hỗ trợ typo, từ đồng nghĩa.",
  parameters: {
    type: "object",
    properties: {
      query: {
        type: "string",
        description: "Từ khóa tìm kiếm"
      },
      entity_type: {
        type: "string",
        enum: ["project", "screen", "subsystem", "all"],
        description: "Loại entity"
      },
      threshold: {
        type: "number",
        description: "Ngưỡng tương đồng (0.0-1.0)",
        default: 0.6
      }
    },
    required: ["query"]
  },
  handler: this.fuzzySearch.bind(this)
},

// Handler implementation
async fuzzySearch({ query, entity_type = 'all', threshold = 0.6 }) {
  try {
    const results = await fuzzySearchService.fuzzySearch(query, entity_type, {
      threshold,
      limit: 10,
      expandSynonyms: true
    });

    return {
      success: true,
      query: query,
      entity_type: entity_type,
      count: results.length,
      results: results.map(r => ({
        id: r.id,
        name: r.name,
        type: r.type,
        description: r.description,
        confidence: (1 - r.score).toFixed(2),
        matched_term: r.matchedTerm
      }))
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}
```

---

## 🎯 Phần 4: Multi-Turn Conversation & Context Management

### 4.1. Conversation State Manager

Quản lý trạng thái hội thoại phức tạp.

```javascript
// ConversationStateManager.js
export class ConversationStateManager {
  constructor() {
    this.state = {
      currentTopic: null,
      referencedEntities: {}, // { type: 'project', id: 12, name: 'Refund System' }
      pendingActions: [],
      tokenBudget: 6000,
      tokenUsed: 0,
    };
  }

  updateTopic(intent, entities) {
    this.state.currentTopic = {
      intent: intent,
      entities: entities,
      timestamp: Date.now(),
    };
  }

  addReferencedEntity(type, id, name) {
    const key = `${type}_${id}`;
    this.state.referencedEntities[key] = {
      type,
      id,
      name,
      timestamp: Date.now(),
    };
  }

  getRecentEntities(maxAge = 300000) {
    // 5 minutes
    const now = Date.now();
    return Object.values(this.state.referencedEntities).filter(
      (e) => now - e.timestamp < maxAge,
    );
  }

  estimateTokens(text) {
    // Rough estimate: 1 token ≈ 4 characters
    return Math.ceil(text.length / 4);
  }

  canAddMessage(message) {
    const messageTokens = this.estimateTokens(message);
    return this.state.tokenUsed + messageTokens < this.state.tokenBudget;
  }

  shouldSummarize() {
    return this.state.tokenUsed > this.state.tokenBudget * 0.8;
  }

  reset() {
    this.state = {
      currentTopic: null,
      referencedEntities: {},
      pendingActions: [],
      tokenBudget: 6000,
      tokenUsed: 0,
    };
  }
}
```

---

### 4.2. Reference Resolution

Xử lý references trong multi-turn conversation.

```javascript
// ReferenceResolver.js
export class ReferenceResolver {
  constructor(conversationStateManager) {
    this.stateManager = conversationStateManager;
  }

  resolveReferences(userMessage) {
    const resolved = { ...userMessage };
    const recentEntities = this.stateManager.getRecentEntities();

    // Detect pronouns and references
    const pronouns = [
      "nó",
      "đó",
      "it",
      "this",
      "that",
      "the project",
      "the screen",
    ];

    for (const pronoun of pronouns) {
      if (userMessage.toLowerCase().includes(pronoun)) {
        // Get most recent entity of relevant type
        const lastEntity = recentEntities[recentEntities.length - 1];
        if (lastEntity) {
          resolved.explicitReference = lastEntity;
          resolved.message = userMessage.replace(
            new RegExp(pronoun, "gi"),
            `${lastEntity.name} (${lastEntity.type} ID: ${lastEntity.id})`,
          );
        }
      }
    }

    return resolved;
  }
}
```

**Ví dụ sử dụng:**

```
User: "Tìm dự án refund"
AI: → Gọi search_projects, tìm thấy "Refund System" (ID: 12)
     → Lưu vào referencedEntities

User: "Nó có bao nhiêu screens?"
AI: → Resolve "nó" = "Refund System (project ID: 12)"
     → Gọi get_project_details({ project_id: 12 })
```

---

## 🎯 Phần 5: Anti-Hallucination Strategies

### 5.1. Response Grounding System

Đảm bảo AI trả lời dựa trên dữ liệu thực.

```javascript
// ResponseGroundingChecker.js
export class ResponseGroundingChecker {
  constructor(databaseManager) {
    this.databaseManager = databaseManager;
  }

  async checkGrounding(aiResponse, toolResults) {
    const analysis = {
      isGrounded: true,
      hasCitations: false,
      mentionsData: false,
      hallucinations: [],
      confidence: 1.0,
    };

    // Check if response uses tool results
    if (!toolResults || toolResults.length === 0) {
      analysis.isGrounded = false;
      analysis.confidence = 0.3;
      analysis.hallucinations.push({
        type: "no_tool_use",
        message: "Response không dựa trên dữ liệu từ tools",
      });
      return analysis;
    }

    // Check for citations
    const citationPatterns = [
      /Nguồn:/i,
      /Dựa vào/i,
      /Theo dữ liệu/i,
      /From data/i,
      /\(Project ID: \d+\)/i,
      /\(Screen ID: \d+\)/i,
    ];

    analysis.hasCitations = citationPatterns.some((pattern) =>
      pattern.test(aiResponse),
    );

    // Extract entity mentions
    const projectMentions =
      aiResponse.match(/(?:project|dự án)\s+["']?([^"'\n,]+)["']?/gi) || [];
    const screenMentions =
      aiResponse.match(/(?:screen|màn hình)\s+["']?([^"'\n,]+)["']?/gi) || [];

    // Verify mentions exist in database
    for (const mention of projectMentions) {
      const projectName = mention
        .replace(/(?:project|dự án)\s+/gi, "")
        .trim()
        .replace(/['"]/g, "");
      const exists = await this.verifyProjectExists(projectName);
      if (!exists) {
        analysis.hallucinations.push({
          type: "non_existent_project",
          entity: projectName,
          message: `Project "${projectName}" không tồn tại trong database`,
        });
      }
    }

    for (const mention of screenMentions) {
      const screenName = mention
        .replace(/(?:screen|màn hình)\s+/gi, "")
        .trim()
        .replace(/['"]/g, "");
      const exists = await this.verifyScreenExists(screenName);
      if (!exists) {
        analysis.hallucinations.push({
          type: "non_existent_screen",
          entity: screenName,
          message: `Screen "${screenName}" không tồn tại trong database`,
        });
      }
    }

    // Calculate final grounding status
    analysis.isGrounded =
      analysis.hallucinations.length === 0 && analysis.hasCitations;
    analysis.confidence = analysis.isGrounded
      ? 1.0
      : Math.max(0.2, 1.0 - analysis.hallucinations.length * 0.2);

    return analysis;
  }

  async verifyProjectExists(projectName) {
    const projects = await this.databaseManager.searchProjects(projectName);
    return projects.some(
      (p) =>
        p.name.toLowerCase() === projectName.toLowerCase() ||
        p.shortName.toLowerCase() === projectName.toLowerCase(),
    );
  }

  async verifyScreenExists(screenName) {
    const screens = await this.databaseManager.searchScreens(screenName);
    return screens.some(
      (s) => s.name.toLowerCase() === screenName.toLowerCase(),
    );
  }

  formatWarning(analysis) {
    if (analysis.isGrounded) return null;

    let warning = "⚠️ **Cảnh báo:** ";
    if (!analysis.hasCitations) {
      warning += "Phản hồi không có trích dẫn nguồn. ";
    }
    if (analysis.hallucinations.length > 0) {
      warning += `Phát hiện ${analysis.hallucinations.length} thông tin chưa được xác minh:\n`;
      analysis.hallucinations.forEach((h) => {
        warning += `- ${h.message}\n`;
      });
    }
    warning += "\n*Vui lòng kiểm tra lại thông tin.*";

    return warning;
  }
}
```

---

### 5.2. Improved System Prompt

Cải thiện system prompt để giảm hallucination.

```javascript
// SystemPromptBuilder.js
export class SystemPromptBuilder {
  static buildAntiHallucinationPrompt(tools) {
    return `Bạn là NoteFlix AI Assistant - trợ lý thông minh cho hệ thống PCM (Project & Compliance Management).

**VAI TRÒ VÀ GIỚI HẠN:**
- Bạn CHỈ trả lời dựa trên dữ liệu từ các công cụ được cung cấp
- KHÔNG được tự bịa hoặc suy đoán thông tin không có trong dữ liệu
- Khi không tìm thấy dữ liệu, hãy nói rõ: "Tôi không có đủ dữ liệu để trả lời câu hỏi này"
- LUÔN trích dẫn nguồn dữ liệu: "Nguồn: [Tool name] - [Entity name] (ID: xxx)"

**CÁC CÔNG CỤ KHẢ DỤNG:**
${tools.map((t) => `- ${t.name}: ${t.description}`).join("\n")}

**QUY TẮC SỬ DỤNG CÔNG CỤ:**
1. ƯU TIÊN gọi công cụ trước khi trả lời
2. Nếu câu hỏi về projects/screens → GỌI search_projects hoặc search_screens
3. Nếu câu hỏi về số liệu → GỌI get_statistics
4. Nếu không tìm thấy → THỬ fuzzy_search hoặc semantic_search
5. Chỉ trả lời sau khi ĐÃ CÓ dữ liệu từ công cụ

**ĐỊNH DẠNG TRẢ LỜI:**
- Luôn bao gồm: "**Nguồn dữ liệu:** [Tool name]"
- Cite entity cụ thể: "Project 'Refund System' (ID: 12)"
- Nếu nhiều kết quả: Liệt kê rõ ràng, đánh số
- Phân biệt giữa "dữ liệu chắc chắn" và "ước đoán"

**CÁC CÂU TRẢ LỜI TIÊU CHUẨN:**
- Không tìm thấy: "Tôi không tìm thấy [entity] trong hệ thống PCM. Bạn có thể kiểm tra lại tên không?"
- Không đủ quyền: "Tôi không có quyền truy cập dữ liệu này."
- Lỗi công cụ: "Đã xảy ra lỗi khi truy vấn dữ liệu. Vui lòng thử lại."

**QUAN TRỌNG:**
- KHÔNG nói "có thể", "nên", "thường thì" nếu không có dữ liệu
- KHÔNG đưa ra con số, tên project/screen không có trong tool results
- KHÔNG trả lời câu hỏi ngoài phạm vi PCM system`;
  }

  static buildContextAwarePrompt(intent, recentEntities) {
    let prompt = this.buildAntiHallucinationPrompt([]);

    // Add context based on intent
    if (intent.primary === "development") {
      prompt += `\n\n**NGUYÊN CHỨC DEVELOPMENT:**
- Khi được hỏi về file/code → GỌI find_source_by_feature
- Khi nói về impact → GỌI get_change_impact
- LUÔN nhắc người dùng "cần kiểm tra code thực tế"`;
    }

    // Add recent entities context
    if (recentEntities.length > 0) {
      prompt += `\n\n**NGUYÊN CẢNH HIỆN TẠI:**
Các entities đã được nhắc đến:
${recentEntities.map((e) => `- ${e.type}: "${e.name}" (ID: ${e.id})`).join("\n")}

Nếu người dùng dùng đại từ ("nó", "đó", "this"), hãy hiểu là đang nhắc đến entities trên.`;
    }

    return prompt;
  }
}
```

---

## 🎯 Phần 6: Caching & Performance

### 6.1. Tool Result Caching

Cache kết quả các tools ít thay đổi.

```javascript
// ToolResultCache.js
export class ToolResultCache {
  constructor() {
    this.cache = new Map();
    this.ttl = {
      list_subsystems: 600000, // 10 minutes
      get_statistics: 300000, // 5 minutes
      search_projects: 60000, // 1 minute
      get_project_details: 120000, // 2 minutes
      default: 30000, // 30 seconds
    };
  }

  getCacheKey(toolName, params) {
    return `${toolName}:${JSON.stringify(params)}`;
  }

  get(toolName, params) {
    const key = this.getCacheKey(toolName, params);
    const cached = this.cache.get(key);

    if (!cached) return null;

    const ttl = this.ttl[toolName] || this.ttl.default;
    const age = Date.now() - cached.timestamp;

    if (age > ttl) {
      this.cache.delete(key);
      return null;
    }

    console.log(`✅ Cache hit: ${toolName}`, params);
    return cached.result;
  }

  set(toolName, params, result) {
    const key = this.getCacheKey(toolName, params);
    this.cache.set(key, {
      result: result,
      timestamp: Date.now(),
    });

    // Limit cache size
    if (this.cache.size > 100) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey);
    }
  }

  clear() {
    this.cache.clear();
  }

  invalidate(toolName) {
    // Remove all cache entries for a specific tool
    for (const key of this.cache.keys()) {
      if (key.startsWith(`${toolName}:`)) {
        this.cache.delete(key);
      }
    }
  }
}
```

**Tích hợp vào DatabaseQueryTool:**

```javascript
// Trong DatabaseQueryTool.js
import { ToolResultCache } from "./ToolResultCache.js";

export class DatabaseQueryTool {
  constructor() {
    this.availableFunctions = this.defineFunctions();
    this.cache = new ToolResultCache();
  }

  async executeFunction(functionName, parameters) {
    // Check cache first
    const cached = this.cache.get(functionName, parameters);
    if (cached) return cached;

    const func = this.availableFunctions.find((f) => f.name === functionName);
    if (!func) {
      return {
        success: false,
        error: `Function '${functionName}' not found`,
      };
    }

    try {
      const result = await func.handler(parameters);

      // Cache successful results
      if (result.success) {
        this.cache.set(functionName, parameters, result);
      }

      return result;
    } catch (error) {
      return {
        success: false,
        error: error.message,
      };
    }
  }
}
```

---

### 6.2. Web Worker for Heavy Computations

Sử dụng Web Worker cho các tác vụ nặng (fuzzy search, statistics).

```javascript
// workers/search-worker.js
import Fuse from "fuse.js";

self.onmessage = async function (e) {
  const { type, data } = e.data;

  switch (type) {
    case "FUZZY_SEARCH":
      const { query, items, options } = data;
      const fuse = new Fuse(items, options);
      const results = fuse.search(query);
      self.postMessage({ type: "FUZZY_SEARCH_RESULT", results });
      break;

    case "COMPUTE_STATISTICS":
      const stats = computeStatistics(data.items);
      self.postMessage({ type: "STATISTICS_RESULT", stats });
      break;

    default:
      self.postMessage({ type: "ERROR", error: "Unknown worker task" });
  }
};

function computeStatistics(items) {
  // Heavy computation
  // ...
  return stats;
}
```

**Sử dụng worker:**

```javascript
// SearchWorkerManager.js
export class SearchWorkerManager {
  constructor() {
    this.worker = new Worker("/workers/search-worker.js", { type: "module" });
    this.pendingRequests = new Map();
    this.requestId = 0;

    this.worker.onmessage = (e) => {
      const { type, requestId, results, error } = e.data;
      const pending = this.pendingRequests.get(requestId);

      if (pending) {
        if (error) {
          pending.reject(new Error(error));
        } else {
          pending.resolve(results);
        }
        this.pendingRequests.delete(requestId);
      }
    };
  }

  fuzzySearch(query, items, options) {
    return new Promise((resolve, reject) => {
      const requestId = this.requestId++;
      this.pendingRequests.set(requestId, { resolve, reject });

      this.worker.postMessage({
        type: "FUZZY_SEARCH",
        requestId,
        data: { query, items, options },
      });
    });
  }

  terminate() {
    this.worker.terminate();
  }
}
```

---

## 🎯 Phần 7: Implementation Roadmap

### Phase 1: Core Tools (Week 1-2)

**Priority: HIGH**

- [ ] Implement `fuzzy_search` with Fuse.js
- [ ] Create `synonyms.json` dictionary
- [ ] Add `semantic_search` tool
- [ ] Implement `search_by_multiple_criteria`
- [ ] Add `IntentDetector` service
- [ ] Update `DatabaseQueryTool` with new tools

### Phase 2: Analysis & Development Tools (Week 3-4)

**Priority: MEDIUM**

- [ ] Implement `analyze_relationships`
- [ ] Add `find_source_by_feature`
- [ ] Create `get_change_impact`
- [ ] Implement `trace_user_journey`
- [ ] Add `get_statistics` with grouping
- [ ] Create `find_similar_entities`

### Phase 3: Anti-Hallucination (Week 5)

**Priority: HIGH**

- [ ] Implement `ResponseGroundingChecker`
- [ ] Update system prompts with anti-hallucination rules
- [ ] Add citation enforcement
- [ ] Create `ReferenceResolver` for multi-turn conversations
- [ ] Implement response validation

### Phase 4: Performance & UX (Week 6)

**Priority: MEDIUM**

- [ ] Implement `ToolResultCache`
- [ ] Create Web Worker for fuzzy search
- [ ] Add `ConversationStateManager`
- [ ] Implement token budget management
- [ ] Add `summarize_conversation` tool

### Phase 5: Quality & Validation (Week 7-8)

**Priority: LOW**

- [ ] Implement `validate_screen_completeness`
- [ ] Add `detect_data_gaps`
- [ ] Create `get_recent_changes`
- [ ] Add analytics logging
- [ ] Implement A/B testing framework

---

## 🎯 Phần 8: Testing & Validation

### 8.1. Test Cases for New Tools

```javascript
// tests/tools.test.js
describe("New AI Tools", () => {
  describe("fuzzy_search", () => {
    it('should find "hoan tien" when searching "hoàn tiền"', async () => {
      const result = await databaseQueryTool.fuzzySearch({
        query: "hoan tien",
        entity_type: "project",
      });
      expect(result.success).toBe(true);
      expect(result.count).toBeGreaterThan(0);
    });

    it("should handle typos", async () => {
      const result = await databaseQueryTool.fuzzySearch({
        query: "refnd", // typo of "refund"
        entity_type: "project",
        threshold: 0.7,
      });
      expect(result.results.some((r) => r.name.includes("Refund"))).toBe(true);
    });
  });

  describe("semantic_search", () => {
    it("should expand synonyms", async () => {
      const result = await databaseQueryTool.semanticSearch({
        query: "authentication",
        expand_synonyms: true,
      });
      // Should also find projects with "xác thực", "login", "auth"
      expect(result.expanded_terms).toContain("xác thực");
    });
  });

  describe("find_source_by_feature", () => {
    it("should find source files for risk review", async () => {
      const result = await databaseQueryTool.findSourceByFeature({
        feature_keywords: ["risk", "review", "approval"],
      });
      expect(result.matched_screens.length).toBeGreaterThan(0);
      expect(result.matched_screens[0].source_files.length).toBeGreaterThan(0);
    });
  });
});
```

### 8.2. Hallucination Detection Tests

```javascript
describe("Anti-Hallucination", () => {
  it("should detect non-existent projects", async () => {
    const aiResponse = 'The "Fake Project X" handles payments...';
    const toolResults = []; // No tool results

    const analysis = await groundingChecker.checkGrounding(
      aiResponse,
      toolResults,
    );
    expect(analysis.isGrounded).toBe(false);
    expect(analysis.hallucinations.length).toBeGreaterThan(0);
  });

  it("should accept grounded responses", async () => {
    const aiResponse =
      'Based on data, the "Refund System" (Project ID: 12) has 15 screens. Nguồn: get_project_details';
    const toolResults = [
      { success: true, project: { id: 12, name: "Refund System" } },
    ];

    const analysis = await groundingChecker.checkGrounding(
      aiResponse,
      toolResults,
    );
    expect(analysis.isGrounded).toBe(true);
    expect(analysis.hasCitations).toBe(true);
  });
});
```

---

## 🎯 Phần 9: Monitoring & Analytics

### 9.1. Tool Usage Analytics

```javascript
// ToolAnalytics.js
export class ToolAnalytics {
  constructor() {
    this.metrics = {
      toolCalls: {},
      successRate: {},
      averageLatency: {},
      cacheHitRate: 0,
      hallucinationRate: 0,
    };
  }

  logToolCall(toolName, params, result, latency) {
    if (!this.metrics.toolCalls[toolName]) {
      this.metrics.toolCalls[toolName] = 0;
      this.metrics.successRate[toolName] = { success: 0, total: 0 };
      this.metrics.averageLatency[toolName] = [];
    }

    this.metrics.toolCalls[toolName]++;
    this.metrics.successRate[toolName].total++;
    if (result.success) {
      this.metrics.successRate[toolName].success++;
    }
    this.metrics.averageLatency[toolName].push(latency);

    // Keep only last 100 latency measurements
    if (this.metrics.averageLatency[toolName].length > 100) {
      this.metrics.averageLatency[toolName].shift();
    }
  }

  logHallucination(conversationId, analysis) {
    if (!analysis.isGrounded) {
      // Log to analytics service or localStorage
      const log = {
        conversationId,
        timestamp: Date.now(),
        hallucinations: analysis.hallucinations,
        confidence: analysis.confidence,
      };

      const logs = JSON.parse(
        localStorage.getItem("hallucination_logs") || "[]",
      );
      logs.push(log);
      localStorage.setItem(
        "hallucination_logs",
        JSON.stringify(logs.slice(-100)),
      ); // Keep last 100
    }
  }

  getReport() {
    const report = {
      total_tool_calls: Object.values(this.metrics.toolCalls).reduce(
        (a, b) => a + b,
        0,
      ),
      top_tools: Object.entries(this.metrics.toolCalls)
        .sort(([, a], [, b]) => b - a)
        .slice(0, 5),
      success_rates: {},
      average_latencies: {},
    };

    for (const [tool, data] of Object.entries(this.metrics.successRate)) {
      report.success_rates[tool] =
        ((data.success / data.total) * 100).toFixed(2) + "%";
    }

    for (const [tool, latencies] of Object.entries(
      this.metrics.averageLatency,
    )) {
      const avg = latencies.reduce((a, b) => a + b, 0) / latencies.length;
      report.average_latencies[tool] = avg.toFixed(0) + "ms";
    }

    return report;
  }
}
```

---

## 🎯 Phần 10: Configuration & Settings

### 10.1. Feature Flags

Thêm các feature flags để bật/tắt tính năng mới:

```javascript
// FeatureFlags.js
export class FeatureFlags {
  static FLAGS = {
    FUZZY_SEARCH: "fuzzy_search_enabled",
    SEMANTIC_SEARCH: "semantic_search_enabled",
    INTENT_DETECTION: "intent_detection_enabled",
    ANTI_HALLUCINATION: "anti_hallucination_enabled",
    TOOL_CACHING: "tool_caching_enabled",
    WEB_WORKERS: "web_workers_enabled",
    RESPONSE_GROUNDING: "response_grounding_enabled",
    MULTI_TURN_CONTEXT: "multi_turn_context_enabled",
  };

  static isEnabled(flag) {
    const stored = localStorage.getItem(flag);
    return stored === "true" || stored === null; // Default enabled
  }

  static setEnabled(flag, enabled) {
    localStorage.setItem(flag, enabled.toString());
  }

  static getAll() {
    const flags = {};
    for (const [key, flag] of Object.entries(this.FLAGS)) {
      flags[key] = this.isEnabled(flag);
    }
    return flags;
  }
}
```

### 10.2. AI Settings Modal Extension

Thêm settings cho các tính năng mới vào `AISettingsModal`:

```javascript
// Trong AISettingsModal.js - thêm vào createSettingsContent()

// Advanced Features Section
const advancedSection = document.createElement("div");
advancedSection.className = "settings-section";

const advancedTitle = document.createElement("h3");
advancedTitle.textContent = "🔬 Advanced Features";
advancedSection.appendChild(advancedTitle);

// Fuzzy Search
const fuzzySearchToggle = this.createToggle(
  "Fuzzy Search",
  "Bật tìm kiếm xấp xỉ (typo-tolerant)",
  FeatureFlags.isEnabled(FeatureFlags.FLAGS.FUZZY_SEARCH),
  (enabled) =>
    FeatureFlags.setEnabled(FeatureFlags.FLAGS.FUZZY_SEARCH, enabled),
);
advancedSection.appendChild(fuzzySearchToggle);

// Semantic Search
const semanticSearchToggle = this.createToggle(
  "Semantic Search",
  "Tự động mở rộng từ đồng nghĩa",
  FeatureFlags.isEnabled(FeatureFlags.FLAGS.SEMANTIC_SEARCH),
  (enabled) =>
    FeatureFlags.setEnabled(FeatureFlags.FLAGS.SEMANTIC_SEARCH, enabled),
);
advancedSection.appendChild(semanticSearchToggle);

// Anti-Hallucination
const antiHallucinationToggle = this.createToggle(
  "Anti-Hallucination Check",
  "Kiểm tra và cảnh báo khi AI đưa ra thông tin không có trong dữ liệu",
  FeatureFlags.isEnabled(FeatureFlags.FLAGS.ANTI_HALLUCINATION),
  (enabled) =>
    FeatureFlags.setEnabled(FeatureFlags.FLAGS.ANTI_HALLUCINATION, enabled),
);
advancedSection.appendChild(antiHallucinationToggle);

// Tool Caching
const cachingToggle = this.createToggle(
  "Tool Result Caching",
  "Cache kết quả các tool để tăng tốc độ",
  FeatureFlags.isEnabled(FeatureFlags.FLAGS.TOOL_CACHING),
  (enabled) =>
    FeatureFlags.setEnabled(FeatureFlags.FLAGS.TOOL_CACHING, enabled),
);
advancedSection.appendChild(cachingToggle);

contentEl.appendChild(advancedSection);
```

---

## 📊 Tổng Kết

### Lợi Ích Mong Đợi

| Cải Thiện                  | Trước                   | Sau                       | Tăng      |
| -------------------------- | ----------------------- | ------------------------- | --------- |
| **Độ chính xác tìm kiếm**  | ~60% (exact match only) | ~85% (fuzzy + semantic)   | +25%      |
| **Phạm vi câu hỏi hỗ trợ** | 7 loại câu hỏi cơ bản   | 20+ loại câu hỏi          | +13 types |
| **Tỷ lệ hallucination**    | ~30%                    | ~5% (với grounding check) | -25%      |
| **Response time**          | 2-5s                    | 1-3s (với caching)        | -40%      |
| **User satisfaction**      | Baseline                | Target +30%               | +30%      |

### Công Cụ Mới

- ✅ **10+ công cụ mới** cho tìm kiếm, phân tích, development
- ✅ **Fuzzy & Semantic Search** với synonyms dictionary
- ✅ **Intent Detection** tự động
- ✅ **Anti-Hallucination** với grounding checker
- ✅ **Multi-turn Conversation** với context management
- ✅ **Performance Optimization** với caching & web workers

### Next Steps

1. **Tuần 1-2:** Implement core search tools (fuzzy, semantic)
2. **Tuần 3-4:** Add analysis & development tools
3. **Tuần 5:** Integrate anti-hallucination system
4. **Tuần 6:** Performance optimization
5. **Tuần 7-8:** Quality assurance & testing

---

**Liên hệ:** Để được hỗ trợ triển khai, vui lòng tham khảo:

- `docs/afc/AI_FUNCTION_CALLING_SYSTEM.md` - Hệ thống function calling hiện tại
- `docs/afc_new/UNIFIED_FUNCTION_CALLING.md` - Architecture tổng quan
- `public/js/services/ai/README.md` - Hướng dẫn thêm AI provider

---

_Document version: 1.0.0_  
_Last updated: 2025-11-07_
