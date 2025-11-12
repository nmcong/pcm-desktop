# PCM WebApp - Architecture Overview

Tổng quan kiến trúc hệ thống AI nâng cấp với focus vào search capabilities và accuracy improvements.

---

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              AIPanel (Main Component)                     │  │
│  │  - Chat input/output                                      │  │
│  │  - Message history                                        │  │
│  │  - Settings & configuration                               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    INTELLIGENT LAYER                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ Intent Detector  │  │ Context Injector │  │   Response   │ │
│  │                  │  │                  │  │   Grounding  │ │
│  │ - Classify query │  │ - Pre-inject     │  │   Checker    │ │
│  │ - Extract        │  │   context        │  │ - Verify     │ │
│  │   entities       │  │ - Enrich with    │  │   citations  │ │
│  │ - Suggest tools  │  │   relevant data  │  │ - Detect     │ │
│  │                  │  │                  │  │   hallucina- │ │
│  │                  │  │                  │  │   tions      │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      SEARCH LAYER                                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ Fuzzy Search     │  │ Semantic Search  │  │  Exact       │ │
│  │ Service          │  │ Service          │  │  Search      │ │
│  │                  │  │                  │  │              │ │
│  │ - Fuse.js        │  │ - Synonyms Dict  │  │ - SQL LIKE   │ │
│  │ - Typo tolerant  │  │ - Expand terms   │  │ - Direct     │ │
│  │ - Index cache    │  │ - Multi-language │  │   match      │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     TOOLS LAYER                                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │
│  │   Search    │ │  Analysis   │ │ Development │ │Validation│ │
│  │   Tools     │ │   Tools     │ │   Tools     │ │  Tools   │ │
│  │             │ │             │ │             │ │          │ │
│  │ - search_   │ │ - analyze_  │ │ - find_     │ │ - vali-  │ │
│  │   projects  │ │   relation- │ │   source_   │ │   date_  │ │
│  │ - fuzzy_    │ │   ships     │ │   by_       │ │   screen │ │
│  │   search    │ │ - trace_    │ │   feature   │ │ - detect │ │
│  │ - semantic_ │ │   user_     │ │ - get_      │ │   _data_ │ │
│  │   search    │ │   journey   │ │   change_   │ │   gaps   │ │
│  │             │ │ - get_      │ │   impact    │ │          │ │
│  │             │ │   stats     │ │             │ │          │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     DATA LAYER                                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │   IndexedDB      │  │  Synonyms Dict   │  │    Cache     │ │
│  │                  │  │                  │  │              │ │
│  │ - Projects       │  │ - Categories     │  │ - Tool       │ │
│  │ - Screens        │  │ - Abbreviations  │  │   Results    │ │
│  │ - Subsystems     │  │ - Common typos   │  │ - Search     │ │
│  │ - Workflows      │  │                  │  │   Index      │ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   AI PROVIDERS LAYER                             │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐       │
│  │ OpenAI │ │ Claude │ │ Gemini │ │ViByte  │ │ Custom │       │
│  │        │ │        │ │        │ │/Ollama │ │        │       │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 User Query Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1: User Input                                               │
│ "Tìm dự án liên quan hoàn tién"  (typo: "tién" instead of "tiền")│
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 2: Intent Detection                                         │
│                                                                  │
│ Intent: "search"  (confidence: 0.95)                            │
│ Entities: { keywords: ["hoàn tién", "dự án"] }                 │
│ Suggested Tools: ["search_projects", "fuzzy_search"]           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 3: Context Injection (Optional)                            │
│                                                                  │
│ Pre-inject subsystems overview for better context              │
│ Add recent conversation entities                                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 4: Tool Selection & Execution                              │
│                                                                  │
│ 4a. Try exact search first: search_projects("hoàn tién")       │
│     → Result: 0 projects (typo)                                 │
│                                                                  │
│ 4b. Fallback to fuzzy search: fuzzy_search("hoàn tién")        │
│     → Normalize: "hoan tien"                                    │
│     → Match: "hoàn tiền" (confidence: 0.85)                     │
│     → Result: ["Refund System", "Payment Recovery"]             │
│                                                                  │
│ 4c. Also try semantic: semantic_search("hoàn tién")            │
│     → Expand: ["hoàn tiền", "refund", "return payment"]         │
│     → Result: Additional ["Chargeback Management"]              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 5: AI Response Generation                                  │
│                                                                  │
│ System Prompt:                                                   │
│ "You detected search intent. Use tool results below.            │
│  ALWAYS cite sources. Do NOT make up information."              │
│                                                                  │
│ Tool Results: [3 projects found...]                             │
│                                                                  │
│ AI generates response with citations                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 6: Response Grounding Check                                │
│                                                                  │
│ ✅ Has tool results: YES                                        │
│ ✅ Has citations: "Nguồn: fuzzy_search"                         │
│ ✅ Entities verified: "Refund System" exists in DB              │
│ ✅ No hallucinations detected                                   │
│                                                                  │
│ Confidence: 0.95 → GROUNDED ✅                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Step 7: Display Response                                        │
│                                                                  │
│ "Tôi tìm thấy 3 dự án liên quan 'hoàn tiền':                   │
│                                                                  │
│ 1. **Refund System** (Project ID: 12)                          │
│    - Description: Hệ thống xử lý hoàn tiền...                  │
│    - 15 screens, 32 events                                      │
│                                                                  │
│ 2. **Payment Recovery** (Project ID: 18)                       │
│    ...                                                           │
│                                                                  │
│ **Nguồn dữ liệu:** fuzzy_search, semantic_search"              │
│                                                                  │
│ [No warnings - response is grounded] ✅                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧠 Intent Detection Flow

```
User Query
    ↓
┌─────────────────────────────────────────┐
│ Pattern Matching                        │
│                                         │
│ - Regex patterns per intent type       │
│ - Weighted scoring                      │
│ - Entity extraction                     │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Intent Classification                   │
│                                         │
│ Intents:                                │
│ • search    (search/find keywords)      │
│ • details   (what/explain keywords)     │
│ • analysis  (workflow/impact keywords)  │
│ • statistics (how many/count keywords)  │
│ • development (file/code keywords)      │
│ • validation (check/missing keywords)   │
│ • general   (fallback)                  │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Tool Suggestion                         │
│                                         │
│ Map intent → relevant tools:            │
│                                         │
│ search → [search_projects,              │
│           fuzzy_search,                 │
│           semantic_search]              │
│                                         │
│ development → [find_source_by_feature,  │
│                get_change_impact]       │
│                                         │
│ ... etc                                 │
└─────────────────────────────────────────┘
    ↓
Output: { primary, confidence, suggestedTools }
```

---

## 🔍 Search Strategy Decision Tree

```
User Query
    ↓
    ├─ Has exact entity reference (ID/name)?
    │       YES → Exact Search
    │             ├─ Found? → Return result ✅
    │             └─ Not found? → Try fuzzy search
    │
    └─ NO → Determine search type
            ↓
            ├─ Simple keyword?
            │   └─ Fuzzy Search (typo-tolerant)
            │       ├─ Found (confidence > 0.6)? → Return ✅
            │       └─ Not found? → Try semantic
            │
            ├─ Contains domain terms?
            │   └─ Semantic Search (synonym expansion)
            │       ├─ Expand with synonyms
            │       ├─ Fuzzy search on expanded terms
            │       └─ Return best matches ✅
            │
            └─ Multiple criteria?
                └─ Multi-Criteria Search
                    ├─ Filter by: technology, subsystem, tags
                    ├─ Apply fuzzy matching on each field
                    └─ Return intersection ✅

Performance Optimization:
• Cache search indexes in memory
• Use Web Workers for heavy computations
• Limit results to top 10
• Debounce user input
```

---

## 🛡️ Anti-Hallucination System

```
AI Response
    ↓
┌─────────────────────────────────────────┐
│ Check 1: Tool Results Exist?            │
│                                         │
│ • toolResults.length > 0                │
│ • If NO → Confidence = 0.3 ⚠️          │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Check 2: Has Citations?                 │
│                                         │
│ • Pattern: "Nguồn:", "(ID: XX)"         │
│ • If NO → Confidence *= 0.7 ⚠️         │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Check 3: Verify Entity Mentions         │
│                                         │
│ • Extract: projects, screens mentioned │
│ • Query DB to verify existence          │
│ • If not exist → Hallucination 🚨       │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Check 4: Verify Numbers                 │
│                                         │
│ • Extract numbers from response         │
│ • Compare with tool result counts       │
│ • If mismatch → Warning ⚠️              │
└─────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────┐
│ Final Grounding Assessment              │
│                                         │
│ isGrounded = hasToolData &&             │
│              hasCitations &&            │
│              noHallucinations           │
│                                         │
│ confidence = 1.0 - (issues * 0.2)       │
└─────────────────────────────────────────┘
    ↓
    ├─ Grounded (confidence > 0.8)?
    │       YES → Display response ✅
    │
    └─ NO → Append warning:
            ⚠️ "Phản hồi chưa được xác minh đầy đủ"
            • List hallucinations
            • Show confidence score
            • Suggest verification
```

---

## 🗂️ Data Structures

### 1. Synonyms Dictionary

```json
{
  "categories": {
    "finance": {
      "refund": ["hoàn tiền", "refund", "chargeback", ...]
    }
  },
  "abbreviations": {
    "auth": "authentication"
  },
  "common_typos": {
    "hoan tien": "hoàn tiền"
  }
}
```

### 2. Intent Detection Result

```typescript
{
  primary: "search",
  confidence: 0.95,
  scores: {
    search: 2.0,
    details: 0.5,
    analysis: 0.0
  },
  suggestedTools: ["search_projects", "fuzzy_search"]
}
```

### 3. Search Result

```typescript
{
  id: 12,
  name: "Refund System",
  type: "project",
  description: "...",
  confidence: 0.85,
  matched_term: "hoàn tiền"
}
```

### 4. Grounding Analysis

```typescript
{
  isGrounded: true,
  hasCitations: true,
  hasToolData: true,
  hallucinations: [],
  warnings: [],
  confidence: 0.95
}
```

---

## 📊 Performance Characteristics

| Component            | Operation             | Time      | Caching              |
| -------------------- | --------------------- | --------- | -------------------- |
| **Fuzzy Search**     | Index build           | 100-200ms | Once on init         |
|                      | Search query          | 10-50ms   | In-memory index      |
| **Semantic Search**  | Load dictionary       | 50ms      | Once on init         |
|                      | Expand synonyms       | 5-10ms    | None                 |
|                      | Search with expansion | 20-100ms  | Inherits fuzzy cache |
| **Intent Detection** | Pattern matching      | <5ms      | None                 |
|                      | Entity extraction     | <5ms      | None                 |
| **Grounding Check**  | Citation check        | <5ms      | None                 |
|                      | Entity verification   | 50-200ms  | DB queries           |
|                      | Total validation      | 100-300ms | Partial              |

**Optimization strategies:**

- ✅ Build search indexes once on app init
- ✅ Cache tool results (TTL: 5-10 minutes)
- ✅ Use Web Workers for heavy operations
- ✅ Debounce user input (300ms)
- ✅ Limit results to top 10-20
- ✅ Lazy load synonyms dictionary

---

## 🔌 Integration Points

### 1. DatabaseQueryTool

```javascript
// Add new tools
defineFunctions() {
  return [
    ...existingTools,
    {
      name: "fuzzy_search",
      handler: this.fuzzySearch.bind(this)
    },
    {
      name: "semantic_search",
      handler: this.semanticSearch.bind(this)
    }
  ];
}
```

### 2. AIPanel

```javascript
// Integrate intent detection
handleUserMessage(message) {
  const intent = intentDetector.detectIntent(message);

  // Use intent to guide tool selection
  const systemPrompt = this.buildIntentAwarePrompt(intent);

  // ... continue with AI call
}

// Integrate grounding check
async processResponse(response, toolResults) {
  const grounding = await groundingChecker.check(response, toolResults);

  if (!grounding.isGrounded) {
    response += '\n\n' + grounding.formatWarning();
  }

  return response;
}
```

### 3. App Initialization

```javascript
async init() {
  // Initialize search services
  await fuzzySearchService.initialize();
  await semanticSearchService.initialize();

  // Warm up intent detector
  intentDetector.detectIntent("warm up");

  // Load grounding checker
  await groundingChecker.initialize();
}
```

---

## 🎯 Scalability Considerations

### Current Scale

- Projects: ~50-100
- Screens: ~500-1000
- Subsystems: ~10-20

### Future Scale (10x growth)

- Projects: ~1000
- Screens: ~10,000
- Subsystems: ~100

**Scaling strategies:**

1. **Search Indexes**
   - Partition by subsystem
   - Lazy load indexes on demand
   - Use IndexedDB for persistent cache

2. **Synonyms Dictionary**
   - Keep in memory (small: ~50KB)
   - Update via CDN or API
   - Version control for updates

3. **Grounding Verification**
   - Batch entity verification
   - Cache verified entities (TTL: 1 hour)
   - Use Bloom filter for quick existence check

4. **Tool Results**
   - Implement pagination
   - Limit context injection to top-N results
   - Stream large results

---

## 🔐 Security Considerations

### 1. XSS Prevention

```javascript
// Sanitize AI responses before rendering
import DOMPurify from "dompurify";

const cleanHTML = DOMPurify.sanitize(aiResponse, {
  ALLOWED_TAGS: ["p", "code", "pre", "a", "ul", "ol", "li"],
  ALLOWED_ATTR: ["href", "class"],
});
```

### 2. Input Validation

```javascript
// Validate user input
if (message.length > 5000) {
  throw new Error("Message too long");
}

// Sanitize entity references
const safeProjectId = parseInt(projectId, 10);
if (isNaN(safeProjectId)) {
  throw new Error("Invalid project ID");
}
```

### 3. Tool Execution Limits

```javascript
// Prevent infinite loops
const MAX_TOOL_ITERATIONS = 5;
let iteration = 0;

while (hasToolCalls && iteration < MAX_TOOL_ITERATIONS) {
  // Execute tools
  iteration++;
}
```

---

## 📈 Monitoring & Observability

### Key Metrics

```javascript
{
  // Search metrics
  "search_accuracy": 0.85,
  "fuzzy_search_hit_rate": 0.70,
  "semantic_search_hit_rate": 0.80,

  // Intent metrics
  "intent_detection_accuracy": 0.88,
  "intent_confidence_avg": 0.82,

  // Grounding metrics
  "hallucination_rate": 0.05,
  "grounded_response_rate": 0.95,
  "citation_rate": 0.98,

  // Performance metrics
  "avg_search_time_ms": 45,
  "avg_intent_detection_ms": 3,
  "avg_grounding_check_ms": 120,

  // User metrics
  "user_satisfaction": 4.2,  // out of 5
  "query_success_rate": 0.91,
  "retry_rate": 0.08
}
```

### Logging Strategy

```javascript
// Log important events
console.log("🔍 Search:", { query, type, results: count });
console.log("🎯 Intent:", { intent, confidence });
console.log("🛡️ Grounding:", { isGrounded, confidence });
console.log("⚠️ Hallucination:", { entity, type });
```

### Analytics Dashboard (Future)

- Search heatmap (which queries succeed/fail)
- Intent distribution chart
- Grounding confidence trends
- Performance latency graphs
- User satisfaction over time

---

## 🚀 Deployment Strategy

### Phase 1: Beta Testing

- Enable for 10% of users
- Monitor metrics closely
- Collect feedback
- Fix critical issues

### Phase 2: Gradual Rollout

- 25% → 50% → 75% → 100%
- Feature flags for quick rollback
- A/B testing for validation

### Phase 3: Full Production

- All users
- Remove feature flags
- Optimize based on data
- Plan next improvements

---

**Architecture v1.0.0 | Last updated: 2025-11-07**
