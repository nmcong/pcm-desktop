# 🚀 Hướng Dẫn Toàn Diện: Làm AI LLM Hiểu và Xử Lý Yêu Cầu Đa Dạng

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Kiến Trúc 5 Lớp](#kiến-trúc-5-lớp)
3. [Cài Đặt và Tích Hợp](#cài-đặt-và-tích-hợp)
4. [Sử Dụng Thực Tế](#sử-dụng-thực-tế)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Tổng Quan

Để AI LLM có thể hiểu và thực hiện yêu cầu đa dạng nhất có thể, chúng ta đã xây dựng một hệ thống **5 lớp** hoạt động
cùng nhau:

```
┌─────────────────────────────────────────────────────────────┐
│                    USER INPUT (Natural Language)             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  LAYER 1: Intent Detection                                   │
│  - Phân tích ý định người dùng                               │
│  - Xác định entity types và actions                          │
│  - Đề xuất tools phù hợp                                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  LAYER 2: Enhanced Prompt Engineering                        │
│  - Build system prompt với context                           │
│  - Inject domain knowledge                                   │
│  - Provide examples (few-shot learning)                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  LAYER 3: LLM Processing (OpenAI/Claude/Gemini/Ollama)      │
│  - Hiểu context và intent                                    │
│  - Quyết định tool calls                                     │
│  - Generate structured responses                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  LAYER 4: Advanced Function Execution                        │
│  - 50+ specialized functions                                 │
│  - Semantic search, relationship analysis                    │
│  - Natural language query processing                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  LAYER 5: Result Synthesis & Response                        │
│  - Tổng hợp kết quả từ multiple tools                        │
│  - Generate insights và recommendations                      │
│  - Format cho UI display                                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Kiến Trúc 5 Lớp

### Layer 1: Intent Detection Service

**Mục đích**: Hiểu chính xác người dùng muốn gì

**File**: `modules/ai/services/IntentDetectionService.js`

**Chức năng**:

- ✅ Phân loại intent thành 12 categories (search, analysis, creation, etc.)
- ✅ Xác định entity types (projects, screens, databases, etc.)
- ✅ Extract filters và parameters từ câu hỏi
- ✅ Đề xuất tools phù hợp
- ✅ Tính confidence score

**Ví dụ**:

```javascript
import { detectIntent } from './IntentDetectionService.js';

const intent = detectIntent("Find all authentication projects with GitHub integration");

// Result:
{
  category: "search",
  entities: ["project"],
  action: "search",
  confidence: 0.85,
  filters: {
    with: "github integration"
  },
  suggestedTools: ["searchProjects", "semanticSearch"]
}
```

**12 Intent Categories**:

1. **SEARCH** - Tìm kiếm items
2. **ANALYSIS** - Phân tích relationships, dependencies
3. **CREATION** - Tạo mới items
4. **UPDATE** - Cập nhật items
5. **DELETION** - Xóa items
6. **NAVIGATION** - Di chuyển giữa các items
7. **INFORMATION** - Lấy thông tin chi tiết
8. **STATISTICS** - Metrics và counts
9. **TROUBLESHOOTING** - Giải quyết vấn đề
10. **COMPARISON** - So sánh items
11. **RECOMMENDATION** - Xin gợi ý
12. **EXPLANATION** - Giải thích concepts

---

### Layer 2: Enhanced Prompt Engineering

**Mục đích**: Cung cấp context và hướng dẫn cho AI

**File**: `modules/ai/services/EnhancedPromptService.js`

**Chức năng**:

- ✅ Build comprehensive system prompts
- ✅ Inject real-time system statistics
- ✅ Provide few-shot examples
- ✅ Add conversation context
- ✅ Enrich user messages with context

**System Prompt Components**:

```
1. Role & Capabilities
   └─ AI's identity and what it can do

2. Tool Usage Guidelines
   └─ How to select and use tools effectively

3. Current System State
   └─ Live statistics (projects, screens, etc.)

4. Example Interactions
   └─ Good query → tool call → response patterns

5. Response Structure
   └─ How to format answers (summary, details, insights)

6. User Context
   └─ Current project/screen, recent activity
```

**Sử dụng**:

```javascript
import {
  buildSystemPrompt,
  enrichUserMessage,
} from "./EnhancedPromptService.js";

// Build system prompt with all context
const systemPrompt = await buildSystemPrompt({
  includeStatistics: true,
  includeExamples: true,
  includeGuidelines: true,
  userContext: {
    currentProject: { id: 1, name: "Auth Service" },
    recentActivity: ["viewed screens", "searched databases"],
  },
});

// Enrich user message
const enrichedMessage = await enrichUserMessage(
  "What tables are used in this project?",
  { currentProject: { id: 1, name: "Auth Service" } },
);
// Result: "What tables are used in this project?\n[Context: Current Project: 'Auth Service' (ID: 1)]"
```

**Query Complexity Analysis**:

```javascript
import { analyzeQueryComplexity } from './EnhancedPromptService.js';

const analysis = analyzeQueryComplexity(
  "How are authentication projects related to user management screens and database tables?"
);

// Result:
{
  complexity: "complex",
  suggestedApproach: "multi_tool",
  recommendedTools: ["analyzeRelationships", "semanticSearch"],
  estimatedSteps: 3
}
```

---

### Layer 3: LLM Processing

**Mục đích**: AI xử lý và quyết định actions

**Đã có sẵn trong**: `modules/ai/components/AIPanel.js`, `modules/ai/services/BaseProvider.js`

**Flow**:

1. Nhận enhanced prompt + user message
2. Process với LLM (OpenAI/Claude/Gemini/Ollama)
3. Generate tool calls hoặc direct response
4. Return structured output

**Unified Function Calling** hỗ trợ:

- ✅ Native function calling (OpenAI, Claude)
- ✅ Text-based function calling (Ollama, custom APIs)
- ✅ Multi-turn conversations
- ✅ Tool result feedback loop

---

### Layer 4: Advanced Function Execution

**Mục đích**: Thực thi functions và lấy data

**50+ Functions** được tổ chức thành categories:

#### **A. Basic CRUD Functions** (đã có)

- Projects: `getAllProjects`, `getProjectById`, `createProject`, `updateProject`, `deleteProject`, `searchProjects`
- Screens: `getAllScreens`, `getScreenById`, `createScreen`, `updateScreen`, `deleteScreen`, `searchScreens`
- Subsystems: `getAllSubsystems`, `getSubsystemById`, `createSubsystem`, `updateSubsystem`, `deleteSubsystem`
- DB Objects: `getAllDBObjects`, `getDBObjectById`, `searchDBObjects`, `createDBObject`, `updateDBObject`
- Knowledge Base: `searchKnowledgeBase`, `createKnowledgeEntry`, `updateKnowledgeEntry`
- GitHub: `getGitHubRepositories`, `getFileContent`, `getRepositoryBranches`
- Batch Jobs: `getAllBatchJobs`, `getBatchJobById`, `executeBatchJob`

#### **B. Advanced Query Functions** (MỚI) ⭐

**File**: `modules/ai/services/functions/AdvancedQueryFunctions.js`

##### **1. semanticSearch**

Tìm kiếm thông minh với scoring và ranking.

```javascript
// Use case: "Find anything related to authentication"
{
  name: "semanticSearch",
  arguments: {
    query: "authentication",
    entityTypes: ["all"], // or ["projects", "screens"]
    limit: 10,
    minScore: 0.3
  }
}

// Returns:
{
  success: true,
  data: {
    totalResults: 15,
    projects: [
      { id: 1, name: "Auth Service", score: 0.92, ... },
      { id: 3, name: "User Authentication", score: 0.85, ... }
    ],
    screens: [
      { id: 5, name: "Login Screen", score: 0.88, ... }
    ],
    dbObjects: [
      { id: 10, name: "users_auth_table", score: 0.80, ... }
    ],
    knowledge: [...]
  }
}
```

**Scoring Algorithm**:

- Exact match: 10 points
- Starts with: 7 points
- Contains: 5 points
- Fuzzy match: 2 points
- Normalized to 0-1 scale

##### **2. analyzeRelationships**

Phân tích mối quan hệ và dependencies.

```javascript
// Use case: "How is Project X related to Screen Y?"
{
  name: "analyzeRelationships",
  arguments: {
    entityType: "project",
    entityId: 1,
    depth: 2
  }
}

// Returns:
{
  success: true,
  data: {
    entity: { id: 1, name: "Auth Service", ... },
    directDependencies: [
      { id: 5, name: "Login Screen", ... },
      { id: 6, name: "Register Screen", ... }
    ],
    indirectDependencies: [...],
    dependents: [...],
    relatedEntities: [
      { id: 10, name: "users_table", type: "TABLE", ... }
    ]
  }
}
```

##### **3. getSystemInsights**

Tổng hợp metrics và insights.

```javascript
// Use case: "Give me an overview of the system"
{
  name: "getSystemInsights",
  arguments: {
    includeMetrics: ["projects", "screens", "dbObjects", "knowledge"]
  }
}

// Returns:
{
  success: true,
  data: {
    projects: {
      total: 25,
      bySubsystem: [
        { subsystem: "Backend", count: 10 },
        { subsystem: "Frontend", count: 15 }
      ],
      withGitHub: 20,
      withoutGitHub: 5
    },
    screens: {
      total: 50,
      withEvents: 45,
      avgEventsPerScreen: "3.2"
    },
    dbObjects: {
      total: 100,
      byType: {
        tables: 30,
        views: 20,
        functions: 25,
        procedures: 15,
        triggers: 5,
        packages: 5
      }
    },
    knowledge: {
      total: 75,
      categories: 10
    }
  }
}
```

##### **4. executeNaturalQuery**

Meta-function xử lý complex queries.

```javascript
// Use case: "What projects have screens with authentication?"
{
  name: "executeNaturalQuery",
  arguments: {
    query: "What projects have screens with authentication?",
    context: {}
  }
}

// Internally:
// 1. Parse intent → "find projects with screens matching 'authentication'"
// 2. Execute: searchScreens({ query: "authentication" })
// 3. Get unique projectIds from screens
// 4. Execute: getProjectById for each projectId
// 5. Synthesize results

// Returns:
{
  success: true,
  data: {
    query: "What projects have screens with authentication?",
    intent: {
      action: "find",
      entities: ["projects", "screens"],
      filters: { with: "authentication" }
    },
    data: {
      projects: [
        { id: 1, name: "Auth Service", matchingScreens: 3, ... },
        { id: 5, name: "User Management", matchingScreens: 1, ... }
      ]
    }
  }
}
```

---

### Layer 5: Result Synthesis & Response

**Mục đích**: Tổng hợp kết quả và tạo response có insight

**Flow**:

1. Nhận results từ tool execution
2. Analyze và tìm patterns
3. Generate insights
4. Format response với:
    - Summary (tóm tắt)
    - Details (chi tiết)
    - Insights (phát hiện)
    - Next Steps (gợi ý tiếp theo)

**Ví dụ Multi-Tool Response**:

```
User: "Find authentication projects and show me their database tables"

Step 1: semanticSearch({ query: "authentication", entityTypes: ["projects"] })
Step 2: getDBObjectsByProject({ projectId: <found_ids> })

AI Response:
═══════════════════════════════════════════════════════════
📊 SUMMARY

I found 3 authentication-related projects with a total of 15 database tables.

───────────────────────────────────────────────────────────
📝 DETAILS

**Projects:**
1. Auth Service (ID: 1) - Main authentication service
   └─ Database Tables: users_table, auth_tokens, sessions (3 tables)

2. User Management (ID: 5) - User account management
   └─ Database Tables: users, roles, permissions (3 tables)

3. OAuth Integration (ID: 8) - Third-party auth
   └─ Database Tables: oauth_providers, oauth_tokens (2 tables)

───────────────────────────────────────────────────────────
💡 INSIGHTS

• Most tables are shared between projects (users_table appears in 2 projects)
• Auth Service has the most comprehensive data model
• 60% of tables have foreign key relationships to users_table

───────────────────────────────────────────────────────────
🔄 NEXT STEPS

• Analyze table relationships: analyzeRelationships()
• View schema details: getDBObjectById()
• Check for missing indexes or constraints
═══════════════════════════════════════════════════════════
```

---

## 🔧 Cài Đặt và Tích Hợp

### Bước 1: Đã Hoàn Thành ✅

Các files đã được tạo:

```
modules/ai/services/
├── functions/
│   ├── AdvancedQueryFunctions.js  ✅ NEW
│   └── index.js                    ✅ UPDATED
├── EnhancedPromptService.js        ✅ NEW
└── IntentDetectionService.js       ✅ NEW
```

### Bước 2: Tích Hợp vào AIPanel

Cập nhật `modules/ai/components/AIPanel.js`:

```javascript
// Add imports
import { detectIntent, formatIntent } from '../services/IntentDetectionService.js';
import { buildSystemPrompt, enrichUserMessage } from '../services/EnhancedPromptService.js';

// In handleSendMessage method:
async handleSendMessage(e) {
  e.preventDefault();
  const message = this.messageInput.value.trim();
  if (!message) return;

  // Step 1: Detect intent
  const intent = detectIntent(message);
  console.log("[AIPanel] Detected intent:", formatIntent(intent));

  // Step 2: Enrich message with context
  const userContext = {
    currentProject: this.currentProject,
    currentScreen: this.currentScreen,
    recentActivity: this.getRecentActivity()
  };
  const enrichedMessage = await enrichUserMessage(message, userContext);

  // Step 3: Add to conversation
  this.conversationHistory.push({
    role: 'user',
    content: enrichedMessage
  });

  // Step 4: Get AI response with enhanced prompts
  await this.getAIResponseWithEnhancement(intent, enrichedMessage, userContext);
}

async getAIResponseWithEnhancement(intent, userMessage, userContext) {
  try {
    // Build enhanced system prompt
    const systemPrompt = await buildSystemPrompt({
      includeStatistics: true,
      includeExamples: true,
      includeGuidelines: true,
      userContext
    });

    // Prepare messages with system prompt
    const messages = [
      { role: 'system', content: systemPrompt },
      ...this.conversationHistory
    ];

    // Get provider and call with tools
    const provider = providerRegistry.getActive();
    const response = await provider.chat(messages, {
      tools: this.getAvailableTools(),
      temperature: 0.7,
      max_tokens: 2048
    });

    // Handle tool calls or direct response
    if (response.tool_calls && response.tool_calls.length > 0) {
      await this.handleToolCalls(response.tool_calls);
    } else {
      this.chatView.addMessage('assistant', response.content);
    }

  } catch (error) {
    console.error("[AIPanel] Error:", error);
    this.chatView.addMessage('assistant', `Error: ${error.message}`);
  }
}
```

### Bước 3: Configure Settings

Trong `modules/ai/components/AISettingsModal.js`, thêm settings:

```javascript
{
  id: 'enableIntentDetection',
  label: 'Enable Intent Detection',
  type: 'checkbox',
  default: true,
  description: 'Automatically detect user intent for better tool selection'
},
{
  id: 'enableEnhancedPrompts',
  label: 'Enhanced Prompts',
  type: 'checkbox',
  default: true,
  description: 'Use context-aware system prompts with examples'
},
{
  id: 'includeSystemStats',
  label: 'Include System Statistics',
  type: 'checkbox',
  default: true,
  description: 'Add live system stats to AI context'
}
```

---

## 💡 Sử Dụng Thực Tế

### Kịch Bản 1: Tìm Kiếm Thông Minh

**User**: "Find all projects related to user authentication with GitHub integration"

**AI Process**:

1. **Intent Detection**:

   ```javascript
   {
     category: "search",
     entities: ["project"],
     filters: { with: "github integration" },
     suggestedTools: ["semanticSearch", "searchProjects"]
   }
   ```

2. **Tool Execution**:

   ```javascript
   semanticSearch({
     query: "user authentication",
     entityTypes: ["projects"],
     limit: 10,
   });
   ```

3. **Post-Processing**: Filter results where `repositoryUrl` exists

4. **Response**:

   ```
   I found 3 projects related to user authentication with GitHub integration:

   1. Auth Service (score: 0.92) - https://github.com/org/auth-service
   2. User Management (score: 0.85) - https://github.com/org/user-mgmt
   3. OAuth Integration (score: 0.78) - https://github.com/org/oauth

   All projects have active GitHub repositories and are being actively developed.
   ```

---

### Kịch Bản 2: Phân Tích Phức Tạp

**User**: "Analyze the relationship between Login Screen and database tables"

**AI Process**:

1. **Intent Detection**:

   ```javascript
   {
     category: "analysis",
     entities: ["screen", "dbObject"],
     action: "analyze",
     suggestedTools: ["analyzeRelationships"]
   }
   ```

2. **Multi-Step Execution**:

   ```javascript
   // Step 1: Find Login Screen
   searchScreens({ query: "Login Screen" });
   // Result: { id: 5, name: "Login Screen", projectId: 1 }

   // Step 2: Get project's database objects
   getDBObjectsByProject({ projectId: 1 });
   // Result: [users_table, auth_tokens, sessions, ...]

   // Step 3: Analyze dependencies
   analyzeDBObjectDependencies({ objectId: 10 }); // users_table
   ```

3. **Synthesize Response**:

   ```
   🔍 RELATIONSHIP ANALYSIS

   Login Screen (ID: 5) is part of Auth Service project, which uses 8 database objects:

   PRIMARY TABLES:
   • users_table - Stores user credentials
   • auth_tokens - JWT tokens for authentication
   • sessions - Active user sessions

   DEPENDENCIES:
   users_table → auth_tokens (user_id FK)
   auth_tokens → sessions (token_id FK)

   INSIGHTS:
   • Login Screen likely queries users_table for authentication
   • Token generation involves auth_tokens table
   • Session management uses sessions table

   RECOMMENDATION:
   • Ensure indexes on users_table.email and auth_tokens.user_id
   • Consider caching session data in Redis
   ```

---

### Kịch Bản 3: Câu Hỏi Mở

**User**: "Give me insights about the entire system"

**AI Process**:

1. **Intent Detection**:

   ```javascript
   {
     category: "statistics",
     entities: ["all"],
     suggestedTools: ["getSystemInsights"]
   }
   ```

2. **Tool Execution**:

   ```javascript
   getSystemInsights({
     includeMetrics: [
       "projects",
       "screens",
       "dbObjects",
       "knowledge",
       "relationships",
     ],
   });
   ```

3. **Generate Insights**:

   ```
   📊 SYSTEM OVERVIEW

   SCALE:
   • 25 projects across 5 subsystems
   • 50 screens with 160 total events
   • 100 database objects (30 tables, 25 functions, 20 views)
   • 75 knowledge base articles

   QUALITY METRICS:
   • 80% projects have GitHub integration ✅
   • Average 3.2 events per screen (good navigation)
   • 90% screens have documentation

   TOP SUBSYSTEMS:
   1. Backend API (10 projects)
   2. Frontend Web (8 projects)
   3. Mobile Apps (7 projects)

   RECOMMENDATIONS:
   • 5 projects missing GitHub links - consider adding
   • 10 screens without events - check if intentional
   • 15 database objects lack descriptions - document them

   HEALTH SCORE: 8.5/10 ⭐
   ```

---

## 📚 Best Practices

### 1. Prompt Engineering

#### ✅ DO:

```javascript
// Clear, specific system prompts
const systemPrompt = `
You are an AI assistant for PCM system.
When user asks "find X", use semanticSearch for broad queries.
When user asks "show me details", use getById functions.
Always explain WHY results are relevant.
`;
```

#### ❌ DON'T:

```javascript
// Vague prompts
const systemPrompt = "You are a helpful assistant.";
```

---

### 2. Function Descriptions

#### ✅ DO:

```javascript
{
  name: "semanticSearch",
  description: `
    Perform intelligent semantic search across all entity types.

    USE THIS WHEN:
    - User asks broad questions like "find anything related to X"
    - Need to search multiple entity types
    - Query is exploratory

    RETURNS:
    - Ranked results with relevance scores
    - Results grouped by entity type

    EXAMPLE:
    User: "Find auth-related items"
    Call: semanticSearch({ query: "authentication", entityTypes: ["all"] })
  `
}
```

#### ❌ DON'T:

```javascript
{
  name: "semanticSearch",
  description: "Search for stuff"
}
```

---

### 3. Context Injection

#### ✅ DO:

```javascript
// Inject relevant, structured context
const context = {
  currentProject: { id: 1, name: "Auth Service", status: "active" },
  recentActivity: ["viewed screens", "edited database table"],
  systemState: {
    totalProjects: 25,
    totalScreens: 50,
  },
};
```

#### ❌ DON'T:

```javascript
// Dump everything
const context = JSON.stringify(entireDatabase); // Too much!
```

---

### 4. Intent Detection

#### ✅ DO:

```javascript
// Use intent to guide tool selection
const intent = detectIntent(userMessage);

if (intent.confidence < 0.5) {
  // Ask for clarification
  return "Could you be more specific? Are you looking for projects, screens, or database objects?";
}

// Use suggested tools
const tools = intent.suggestedTools;
```

#### ❌ DON'T:

```javascript
// Ignore intent and guess
const tools = ["getAllProjects"]; // Random guess
```

---

### 5. Multi-Turn Conversations

#### ✅ DO:

```javascript
// Maintain context across turns
const conversationHistory = [
  { role: "user", content: "Find auth projects" },
  { role: "assistant", content: "Found 3 projects..." },
  { role: "user", content: "Show me the first one" }, // "first one" references previous
];

// Build context from history
const context = buildConversationContext(conversationHistory);
```

#### ❌ DON'T:

```javascript
// Treat each message independently
const response = await chat([{ role: "user", content: message }]); // No context!
```

---

## 🐛 Troubleshooting

### Issue 1: AI không gọi tools

**Triệu chứng**: AI trả lời trực tiếp thay vì gọi functions

**Nguyên nhân**:

- System prompt không rõ ràng
- Tool descriptions quá phức tạp
- LLM model không support function calling tốt

**Giải pháp**:

```javascript
// 1. Rõ ràng hơn trong system prompt
const systemPrompt = `
CRITICAL: You MUST use tools to get information.
DO NOT make up answers.
ALWAYS call appropriate functions first.

To call a function, output ONLY a JSON block:
\`\`\`json
{
  "tool_calls": [
    {"name": "functionName", "arguments": {...}}
  ]
}
\`\`\`
`;

// 2. Simplify tool descriptions
{
  name: "searchProjects",
  description: "Search for projects by name or description. Returns list of projects.",
  // Don't: "This function performs a comprehensive search across..."
}

// 3. Use better model
// Switch from llama2:7b → qwen2:14b or gpt-4
```

---

### Issue 2: Intent Detection sai

**Triệu chứng**: Gọi sai tools, không hiểu query

**Debug**:

```javascript
import { detectIntent, formatIntent } from "./IntentDetectionService.js";

const intent = detectIntent(userMessage);
console.log("Intent:", formatIntent(intent));

// Check:
// - Is category correct?
// - Are entities correct?
// - Is confidence reasonable?
```

**Giải pháp**:

```javascript
// Add more patterns to intent detection
function detectIntentCategory(message) {
  // Add your domain-specific patterns
  if (message.match(/\b(auth|login|user|password)\b/)) {
    // Likely searching for auth-related items
  }
}
```

---

### Issue 3: Kết quả không relevant

**Triệu chứng**: semanticSearch trả về kết quả không liên quan

**Debug**:

```javascript
// Check scoring
const results = await semanticSearch({
  query: "authentication",
  entityTypes: ["all"],
  minScore: 0.1, // Lower to see all results
});

console.log("Results:", results.data);
// Check scores - are they reasonable?
```

**Giải pháp**:

```javascript
// 1. Adjust scoring algorithm
function calculateRelevanceScore(item, searchTerms, fields) {
  let score = 0;

  // Weight certain fields higher
  const fieldWeights = {
    name: 10,
    description: 5,
    tags: 8,
  };

  for (const field of fields) {
    const weight = fieldWeights[field] || 1;
    // Apply weight to score calculation
  }
}

// 2. Use better search strategy
// Instead of simple string matching, use:
// - Stemming (authentication → auth)
// - Synonyms (user → account)
// - Fuzzy matching with Levenshtein distance
```

---

### Issue 4: Quá chậm

**Triệu chứng**: AI mất nhiều thời gian response

**Nguyên nhân**:

- System prompt quá dài
- Quá nhiều tools trong một request
- Database queries chưa optimize

**Giải pháp**:

```javascript
// 1. Cache system statistics
let cachedStats = null;
let cacheTime = null;

async function getSystemStatistics() {
  const now = Date.now();
  if (cachedStats && now - cacheTime < 5 * 60 * 1000) {
    // 5 min cache
    return cachedStats;
  }

  cachedStats = await fetchStats();
  cacheTime = now;
  return cachedStats;
}

// 2. Limit tools sent to LLM
function getRelevantTools(intent) {
  // Only send tools likely to be used
  return intent.suggestedTools.slice(0, 5); // Max 5 tools
}

// 3. Optimize database queries
// Add indexes on frequently searched fields
// Use batch queries instead of loops
```

---

## 📊 Metrics & Monitoring

Theo dõi hiệu suất của hệ thống:

```javascript
// Track function execution
const metrics = {
  totalCalls: 0,
  successfulCalls: 0,
  averageExecutionTime: 0,
  intentAccuracy: 0,
  userSatisfaction: 0,
};

// Log every function call
functionCallingService.on(
  "execute",
  (functionName, params, result, duration) => {
    metrics.totalCalls++;
    if (result.success) metrics.successfulCalls++;

    // Update average
    metrics.averageExecutionTime =
      (metrics.averageExecutionTime * (metrics.totalCalls - 1) + duration) /
      metrics.totalCalls;
  },
);

// Track intent accuracy
intentDetectionService.on(
  "detect",
  (userMessage, detectedIntent, userFeedback) => {
    if (userFeedback === "correct") {
      metrics.intentAccuracy =
        (metrics.intentAccuracy * metrics.totalCalls + 1) /
        (metrics.totalCalls + 1);
    }
  },
);
```

---

## 🎓 Tóm Tắt

### ✅ Những gì đã có:

1. ✅ **50+ function definitions** - CRUD operations cho tất cả entities
2. ✅ **Unified function calling** - Support OpenAI, Claude, Gemini, Ollama
3. ✅ **Multi-turn conversations** - Context-aware chats
4. ✅ **Tool execution framework** - Robust error handling

### 🆕 Những gì mới thêm:

1. 🆕 **Advanced Query Functions** - semanticSearch, analyzeRelationships, getSystemInsights, executeNaturalQuery
2. 🆕 **Intent Detection Service** - 12 intent categories, entity extraction, tool suggestions
3. 🆕 **Enhanced Prompt Service** - Context-aware prompts, few-shot examples, system statistics
4. 🆕 **Comprehensive Documentation** - Complete guide với examples

### 🚀 Kết quả:

AI LLM giờ có thể:

- ✅ Hiểu yêu cầu phức tạp trong natural language
- ✅ Tự động chọn tools phù hợp
- ✅ Thực hiện multi-step queries
- ✅ Tìm kiếm semantic với ranking
- ✅ Phân tích relationships và dependencies
- ✅ Generate insights từ data
- ✅ Học từ context và conversation history
- ✅ Xử lý edge cases và errors gracefully

---

**Phiên bản**: 1.0.0  
**Ngày cập nhật**: November 10, 2025  
**Tác giả**: PCM Development Team

**Để biết thêm chi tiết, xem**:

- [AI Function Calling System](./function-calling-v2/AI_FUNCTION_CALLING_SYSTEM.md)
- [Unified Function Calling](./function-calling-v2/UNIFIED_FUNCTION_CALLING.md)
- [Function Calling Quick Start](./function-calling-v2/FUNCTION_CALLING_QUICK_START.md)
