# PCM WebApp - Quick Reference: New AI Tools

**Tham khảo đầy đủ:** `UPGRADE_RECOMMENDATIONS.md`

---

## 🔍 Tìm Kiếm Nâng Cao (Search Tools)

### 1. `fuzzy_search`

**Khi nào dùng:** User nhập sai chính tả, viết tắt, hoặc không chính xác  
**Ví dụ:** `"hoan tien"` → tìm được `"hoàn tiền"`

```javascript
{
  query: "refnd system",  // typo
  entity_type: "project",
  threshold: 0.6
}
→ Kết quả: "Refund System" (confidence: 0.85)
```

---

### 2. `semantic_search`

**Khi nào dùng:** Tìm kiếm theo nghĩa, không theo từ chính xác  
**Ví dụ:** `"authentication"` → tìm cả `"xác thực"`, `"login"`, `"auth"`

```javascript
{
  query: "thanh toán",
  expand_synonyms: true,
  language: "both"
}
→ Kết quả: Projects với "payment", "pay", "chi trả"
```

---

### 3. `search_by_multiple_criteria`

**Khi nào dùng:** Tìm kiếm kết hợp nhiều điều kiện  
**Ví dụ:** Dự án React + liên quan payment + có workflow

```javascript
{
  technologies: ["React", "TypeScript"],
  keywords: ["payment", "thanh toán"],
  has_workflows: true,
  favorite_only: false
}
```

---

## 📊 Phân Tích & Thống Kê (Analysis Tools)

### 4. `get_statistics`

**Khi nào dùng:** Câu hỏi về số liệu, tổng quan  
**Ví dụ:** "Bao nhiêu project dùng Java?"

```javascript
{
  group_by: "technology",  // "subsystem" | "status" | "overall"
  include_details: true
}
→ { top_technologies: ["Java", "React"], by_technology: {...} }
```

---

### 5. `analyze_relationships`

**Khi nào dùng:** Phân tích mối quan hệ giữa entities  
**Ví dụ:** "Screen này trigger màn hình nào?"

```javascript
{
  entity_type: "screen",
  entity_id: 45,
  depth: 2
}
→ { triggers_screens: [...], triggered_by_screens: [...] }
```

---

### 6. `find_similar_entities`

**Khi nào dùng:** Tìm projects/screens tương tự  
**Ví dụ:** "Dự án nào giống Refund System?"

```javascript
{
  reference_entity_type: "project",
  reference_entity_id: 12,
  similarity_criteria: ["technology", "workflow"],
  limit: 5
}
```

---

## 💻 Development Support Tools

### 7. `find_source_by_feature`

**Khi nào dùng:** Cần tìm file code/DB cho một feature  
**Ví dụ:** "File nào xử lý risk review?"

```javascript
{
  feature_keywords: ["risk", "review", "approval"],
  include_related: true,
  confidence_threshold: 0.7
}
→ {
  source_files: ["RiskReviewPage.tsx", "RiskService.java"],
  database_tables: ["refund_requests", "risk_audit_logs"]
}
```

---

### 8. `get_change_impact`

**Khi nào dùng:** Đánh giá impact khi sửa/xóa entity  
**Ví dụ:** "Nếu xóa screen này thì sao?"

```javascript
{
  entity_type: "screen",
  entity_identifier: "45",
  change_type: "delete"
}
→ {
  affected_screens: 3,
  affected_workflows: 2,
  risk_level: "high",
  recommendations: [...]
}
```

---

### 9. `trace_user_journey`

**Khi nào dùng:** Trace đường đi giữa 2 screens  
**Ví dụ:** "Từ Login đến Refund Request đi như thế nào?"

```javascript
{
  from_screen_id: 10,
  to_screen_id: 25,
  max_depth: 5
}
→ { paths: [
  { screens: [Login → Dashboard → Refund Request] },
  { screens: [Login → Dashboard → Menu → Refund Request] }
]}
```

---

### 10. `get_recent_changes`

**Khi nào dùng:** Xem lịch sử thay đổi gần đây  
**Ví dụ:** "Có gì thay đổi tuần này?"

```javascript
{
  entity_type: "all",
  days: 7,
  limit: 20
}
```

---

## 🎯 Workflow & Context Tools

### 11. `get_workflow_details`

**Khi nào dùng:** Chi tiết về workflow cụ thể  
**Ví dụ:** "Chi tiết workflow Standard Refund Flow?"

```javascript
{
  workflow_id: 5,
  include_bpmn: true
}
→ { steps: [...], bpmn_xml: "...", conditions: [...] }
```

---

### 12. `summarize_conversation`

**Khi nào dùng:** Hội thoại quá dài, cần tóm tắt  
**Ví dụ:** Tự động gọi khi vượt 80% token budget

```javascript
{
  conversation_id: "conv_123",
  preserve_data: true
}
→ Rút gọn messages nhưng giữ lại tool results quan trọng
```

---

## ✅ Validation & Quality Tools

### 13. `validate_screen_completeness`

**Khi nào dùng:** Kiểm tra screen có đầy đủ metadata không  
**Ví dụ:** "Screen này thiếu gì?"

```javascript
{
  screen_id: 45,
  strict_mode: false
}
→ {
  completeness_score: 0.75,
  validation: { has_events: true, has_permissions: false },
  recommendations: ["Thêm permissions", "Upload screenshots"]
}
```

---

### 14. `detect_data_gaps`

**Khi nào dùng:** Phát hiện gaps trong dữ liệu  
**Ví dụ:** "Dữ liệu có vấn đề gì không?"

```javascript
{
  scope: "all",  // "project" | "subsystem"
  entity_id: null
}
→ {
  screens_without_events: [...],
  projects_without_workflows: [...],
  missing_source_files: [...]
}
```

---

## 🎨 Intent Detection - Tự Động Chọn Tool

### Intent Categories & Tools

| User Question Pattern           | Detected Intent | Auto-Selected Tools               |
| ------------------------------- | --------------- | --------------------------------- |
| "Tìm dự án liên quan X"         | `search`        | `search_projects`, `fuzzy_search` |
| "Chi tiết project Y"            | `details`       | `get_project_details`             |
| "Workflow như thế nào?"         | `analysis`      | `trace_user_journey`              |
| "Bao nhiêu project dùng React?" | `statistics`    | `get_statistics`                  |
| "File nào xử lý feature X?"     | `development`   | `find_source_by_feature`          |
| "Screen này thiếu gì?"          | `validation`    | `validate_screen_completeness`    |

### Ví Dụ Intent Detection

```javascript
User: "Tìm dự án liên quan hoàn tiền"
→ Intent: search (confidence: 0.9)
→ Auto tools: [search_projects, fuzzy_search, semantic_search]

User: "File nào xử lý risk review?"
→ Intent: development (confidence: 0.95)
→ Auto tools: [find_source_by_feature, search_screens]

User: "Impact khi xóa screen 45?"
→ Intent: analysis (confidence: 1.0)
→ Auto tools: [get_change_impact, analyze_relationships]
```

---

## 🛡️ Anti-Hallucination Checklist

### AI Response PHẢI có:

- ✅ **Citation:** `"Nguồn: search_projects"`
- ✅ **Entity reference:** `"Project 'Refund System' (ID: 12)"`
- ✅ **Data-backed:** Tất cả thông tin từ tool results
- ✅ **Humble:** Nói "không có dữ liệu" nếu không tìm thấy

### AI Response KHÔNG được:

- ❌ Bịa tên project/screen không có trong DB
- ❌ Đưa ra con số không có từ tools
- ❌ Dùng "có thể", "thường thì" mà không có data
- ❌ Trả lời câu hỏi ngoài phạm vi PCM

### Warning Flags

```javascript
⚠️ "Phản hồi không có trích dẫn nguồn"
⚠️ "Phát hiện 2 thông tin chưa được xác minh"
⚠️ "Project 'XYZ' không tồn tại trong database"
```

---

## 🚀 Quick Start Implementation

### 1. Thêm Tool Mới vào DatabaseQueryTool

```javascript
// DatabaseQueryTool.js - defineFunctions()
{
  name: "fuzzy_search",
  description: "Tìm kiếm xấp xỉ...",
  parameters: {...},
  handler: this.fuzzySearch.bind(this)
}
```

### 2. Implement Handler

```javascript
async fuzzySearch({ query, entity_type, threshold }) {
  const results = await fuzzySearchService.search(query, entity_type);
  return {
    success: true,
    results: results.filter(r => r.score >= threshold)
  };
}
```

### 3. Test Tool

```javascript
const result = await databaseQueryTool.executeFunction("fuzzy_search", {
  query: "refnd",
  entity_type: "project",
  threshold: 0.6,
});
console.log(result); // Should find "Refund System"
```

---

## 📦 Dependencies Cần Thêm

```json
{
  "dependencies": {
    "fuse.js": "^7.0.0", // Fuzzy search
    "dompurify": "^3.0.0" // XSS protection (optional)
  }
}
```

**Cài đặt:**

```bash
pnpm add fuse.js dompurify
```

---

## 🎯 Priority Implementation

### Phase 1 (Week 1-2) - CRITICAL

- ✅ `fuzzy_search` - Giải quyết typo
- ✅ `semantic_search` - Mở rộng tìm kiếm
- ✅ Intent detection - Tự động chọn tools
- ✅ Synonyms dictionary - Hỗ trợ semantic

### Phase 2 (Week 3-4) - HIGH

- ✅ `find_source_by_feature` - Dev support
- ✅ `get_change_impact` - Impact analysis
- ✅ `analyze_relationships` - Entity relationships
- ✅ Response grounding checker - Anti-hallucination

### Phase 3 (Week 5-6) - MEDIUM

- ✅ `trace_user_journey` - User flow
- ✅ `get_statistics` - Overview stats
- ✅ Tool result caching - Performance
- ✅ Multi-turn context - Better conversations

---

## 📞 Support & References

| Document                                 | Purpose                         |
| ---------------------------------------- | ------------------------------- |
| `UPGRADE_RECOMMENDATIONS.md`             | Chi tiết đầy đủ tất cả nâng cấp |
| `ai-panel-review.md`                     | Review và issues hiện tại       |
| `docs/afc/AI_FUNCTION_CALLING_SYSTEM.md` | Hệ thống function calling       |
| `public/js/services/ai/README.md`        | Hướng dẫn AI providers          |

---

_Quick Reference v1.0.0 | Last updated: 2025-11-07_
