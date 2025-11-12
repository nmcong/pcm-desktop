# 🚀 Quick Guide: Làm AI LLM Hiểu Yêu Cầu Đa Dạng

## 📝 TL;DR

Để AI hiểu và xử lý yêu cầu đa dạng, cần **5 yếu tố**:

```
User Input → Intent Detection → Enhanced Prompts → LLM Processing → Advanced Functions → Smart Response
```

---

## 🎯 5 Cải Tiến Chính

### 1. **Advanced Functions** (Công cụ mạnh mẽ)

**File**: `modules/ai/services/functions/AdvancedQueryFunctions.js`

4 functions mới:

- ✅ `semanticSearch` - Tìm kiếm thông minh với scoring
- ✅ `analyzeRelationships` - Phân tích mối quan hệ
- ✅ `getSystemInsights` - Overview và metrics
- ✅ `executeNaturalQuery` - Xử lý queries phức tạp

**Ví dụ**:

```javascript
// User: "Find anything related to authentication"
semanticSearch({
  query: "authentication",
  entityTypes: ["all"],
  limit: 10,
  minScore: 0.3,
});
// → Returns ranked results across projects, screens, databases, knowledge base
```

---

### 2. **Intent Detection** (Hiểu ý định)

**File**: `modules/ai/services/IntentDetectionService.js`

12 loại intent:

- SEARCH, ANALYSIS, CREATION, UPDATE, DELETION, STATISTICS, ...

**Ví dụ**:

```javascript
detectIntent("Find all auth projects with GitHub");
// → {
//   category: "search",
//   entities: ["project"],
//   filters: { with: "github" },
//   suggestedTools: ["semanticSearch", "searchProjects"]
// }
```

---

### 3. **Enhanced Prompts** (Context thông minh)

**File**: `modules/ai/services/EnhancedPromptService.js`

System prompt bao gồm:

- ✅ AI role & capabilities
- ✅ Current system statistics (live data)
- ✅ Tool usage guidelines
- ✅ Few-shot examples
- ✅ User context (current project/screen)

**Ví dụ**:

```javascript
const systemPrompt = await buildSystemPrompt({
  includeStatistics: true,
  includeExamples: true,
  userContext: { currentProject: { id: 1, name: "Auth Service" } },
});
// → "You are an AI assistant for PCM system. Current state: 25 projects, 50 screens..."
```

---

### 4. **Query Complexity Analysis** (Phân tích độ phức tạp)

```javascript
analyzeQueryComplexity(
  "How are auth projects related to user management screens?",
);
// → {
//   complexity: "complex",
//   suggestedApproach: "multi_tool",
//   estimatedSteps: 3,
//   recommendedTools: ["analyzeRelationships", "semanticSearch"]
// }
```

---

### 5. **Conversation Context** (Nhớ hội thoại)

```javascript
buildConversationContext(messages);
// → Summarize recent conversation for context
// AI remembers what was discussed and references it
```

---

## 🔧 Cách Sử Dụng

### Option A: Tích hợp vào AIPanel (Khuyến nghị)

```javascript
// In modules/ai/components/AIPanel.js

import { detectIntent } from '../services/IntentDetectionService.js';
import { buildSystemPrompt, enrichUserMessage } from '../services/EnhancedPromptService.js';

async handleSendMessage(e) {
  const message = this.messageInput.value.trim();

  // 1. Detect intent
  const intent = detectIntent(message);

  // 2. Build enhanced prompt
  const systemPrompt = await buildSystemPrompt({
    includeStatistics: true,
    userContext: { currentProject: this.currentProject }
  });

  // 3. Enrich user message
  const enrichedMessage = await enrichUserMessage(message, { currentProject: this.currentProject });

  // 4. Call LLM with enhanced context
  const response = await provider.chat([
    { role: 'system', content: systemPrompt },
    { role: 'user', content: enrichedMessage }
  ], {
    tools: this.getAvailableTools() // Now includes advanced functions!
  });

  // 5. Handle response
  this.handleResponse(response);
}
```

---

### Option B: Sử dụng Độc Lập

```javascript
// Test intent detection
import {
  detectIntent,
  formatIntent,
} from "./modules/ai/services/IntentDetectionService.js";
// → {
//   summary: "SEARCH → search [project]",
//   confidence: "85%",
//   toolsToUse: ["semanticSearch", "searchProjects"]
// }

// Test semantic search
import { advancedQueryFunctions } from "./modules/ai/services/functions/AdvancedQueryFunctions.js";

const intent = detectIntent("Find all authentication projects");
console.log(formatIntent(intent));

const result = await advancedQueryFunctions.semanticSearch.handler({
  query: "authentication",
  entityTypes: ["all"],
  limit: 10,
});
console.log(result.data);
// → { totalResults: 15, projects: [...], screens: [...], ... }
```

---

## 💡 Use Cases Thực Tế

### 1. Tìm Kiếm Mơ Hồ

**Before**:

```
User: "Find auth stuff"
AI: "I need more details. What type of items?"
```

**After**:

```
User: "Find auth stuff"
AI: [Calls semanticSearch({ query: "auth", entityTypes: ["all"] })]
    "I found 15 items related to 'auth':
     • 3 projects: Auth Service, User Auth, OAuth
     • 5 screens: Login, Register, Password Reset
     • 7 database tables: users, auth_tokens, sessions..."
```

---

### 2. Phân Tích Phức Tạp

**Before**:

```
User: "How is Login screen related to database?"
AI: "The Login screen is part of Auth Service project."
```

**After**:

```
User: "How is Login screen related to database?"
AI: [Calls analyzeRelationships + getDBObjectsByProject]
    "Login Screen (Project: Auth Service) uses these database tables:
     1. users_table (primary key: id)
        → Stores user credentials
        → Indexed on: email, username
     2. auth_tokens (FK: user_id → users_table.id)
        → JWT tokens for authentication
     3. sessions (FK: token_id → auth_tokens.id)
        → Active user sessions

     Relationship: Login → Query users_table → Generate auth_token → Create session"
```

---

### 3. Context-Aware

**Before**:

```
User: "Show me tables in this project"
AI: "Which project? Please specify project ID."
```

**After**:

```
[User is viewing Project "Auth Service" (ID: 1)]
User: "Show me tables in this project"
AI: [Enriched: "Show me tables in this project [Context: Project 'Auth Service' (ID: 1)]"]
    [Calls getDBObjectsByProject({ projectId: 1, type: "TABLE" })]
    "Here are 5 tables in Auth Service project:
     1. users_table - User credentials and profiles
     2. auth_tokens - JWT authentication tokens
     3. sessions - Active user sessions
     ..."
```

---

## 📊 Kết Quả

### Trước Khi Cải Tiến:

- ❌ Chỉ trả lời được câu hỏi đơn giản
- ❌ Không hiểu context
- ❌ Không thể phân tích relationships
- ❌ Tìm kiếm kém hiệu quả
- ❌ Không nhớ conversation history

### Sau Khi Cải Tiến:

- ✅ Hiểu câu hỏi phức tạp, mơ hồ
- ✅ Nhớ context (current project, screen, history)
- ✅ Phân tích relationships, dependencies
- ✅ Tìm kiếm semantic với ranking
- ✅ Generate insights từ data
- ✅ Multi-turn conversations
- ✅ Tự động suggest next steps

---

## 🎯 Checklist Tích Hợp

- [ ] Import 3 files mới vào AIPanel.js
- [ ] Thêm intent detection vào handleSendMessage
- [ ] Sử dụng buildSystemPrompt thay vì static prompt
- [ ] Test với câu hỏi mơ hồ (e.g., "find auth stuff")
- [ ] Test với câu hỏi phức tạp (e.g., "analyze relationships")
- [ ] Test context awareness (e.g., "show tables in this project")
- [ ] Monitor metrics (intent accuracy, function call success rate)

---

## 📚 Đọc Thêm

- **[Comprehensive Guide](./COMPREHENSIVE_AI_ENHANCEMENT_GUIDE.md)** - Hướng dẫn chi tiết đầy đủ
- **[Unified Function Calling](./function-calling-v2/UNIFIED_FUNCTION_CALLING.md)** - Function calling system
- **[AI Function Calling System](./function-calling-v2/AI_FUNCTION_CALLING_SYSTEM.md)** - Tool execution

---

## 🚀 Bắt Đầu Ngay

```bash
# 1. Files đã được tạo sẵn, chỉ cần integrate vào AIPanel

# 2. Test intent detection
node -e "
const { detectIntent } = require('./modules/ai/services/IntentDetectionService.js');
console.log(detectIntent('Find all auth projects'));
"

# 3. Test semantic search
node -e "
const { semanticSearch } = require('./modules/ai/services/functions/AdvancedQueryFunctions.js');
semanticSearch.handler({ query: 'auth', entityTypes: ['all'] }).then(console.log);
"

# 4. Mở AIPanel và thử chat!
```

---

**Phiên bản**: 1.0.0  
**Tác giả**: PCM Development Team  
**Cập nhật**: November 10, 2025
