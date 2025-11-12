# PCM WebApp - Hướng Dẫn Triển Khai Tools Mới

**Tài liệu tham khảo:** `UPGRADE_RECOMMENDATIONS.md`, `TOOLS_QUICK_REFERENCE.md`

---

## 📋 Mục Lục

1. [Setup Dependencies](#1-setup-dependencies)
2. [Implement Fuzzy Search](#2-implement-fuzzy-search)
3. [Create Synonyms Dictionary](#3-create-synonyms-dictionary)
4. [Add Intent Detection](#4-add-intent-detection)
5. [Integrate Anti-Hallucination](#5-integrate-anti-hallucination)
6. [Testing & Validation](#6-testing--validation)

---

## 1. Setup Dependencies

### Step 1.1: Cài Đặt Packages

```bash
cd apps/pcm-webapp
pnpm add fuse.js
pnpm add dompurify  # Optional: for XSS protection
```

### Step 1.2: Tạo File Structures

```bash
# Create new directories
mkdir -p public/data
mkdir -p public/js/services/search
mkdir -p public/js/utils/ai

# Create new files
touch public/data/synonyms.json
touch public/js/services/search/FuzzySearchService.js
touch public/js/services/search/SemanticSearchService.js
touch public/js/utils/ai/IntentDetector.js
touch public/js/utils/ai/ContextInjector.js
touch public/js/utils/ai/ResponseGroundingChecker.js
```

---

## 2. Implement Fuzzy Search

### Step 2.1: Create FuzzySearchService.js

```javascript
/**
 * Fuzzy Search Service
 * Provides typo-tolerant search using Fuse.js
 */
import Fuse from "fuse.js";
import databaseManager from "../DatabaseManager.js";

export class FuzzySearchService {
  constructor() {
    this.indexCache = null;
    this.isInitialized = false;
  }

  /**
   * Initialize search indexes
   */
  async initialize() {
    if (this.isInitialized) return;

    console.log("🔍 Initializing fuzzy search indexes...");

    try {
      const [projects, screens, subsystems] = await Promise.all([
        databaseManager.getProjects(),
        databaseManager.getScreens(),
        databaseManager.getSubsystems(),
      ]);

      this.indexCache = {
        projects: new Fuse(projects, {
          keys: ["name", "description", "shortName"],
          threshold: 0.4,
          includeScore: true,
          minMatchCharLength: 2,
          useExtendedSearch: true,
        }),
        screens: new Fuse(screens, {
          keys: ["name", "description", "notes"],
          threshold: 0.4,
          includeScore: true,
          minMatchCharLength: 2,
          useExtendedSearch: true,
        }),
        subsystems: new Fuse(subsystems, {
          keys: ["name", "description"],
          threshold: 0.3,
          includeScore: true,
          minMatchCharLength: 2,
          useExtendedSearch: true,
        }),
      };

      this.isInitialized = true;
      console.log("✅ Fuzzy search indexes ready");
    } catch (error) {
      console.error("❌ Failed to initialize fuzzy search:", error);
      throw error;
    }
  }

  /**
   * Normalize text (remove Vietnamese accents)
   */
  normalizeText(text) {
    return text
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase()
      .trim();
  }

  /**
   * Perform fuzzy search
   */
  async search(query, entityType = "all", options = {}) {
    if (!this.isInitialized) {
      await this.initialize();
    }

    const { threshold = 0.6, limit = 10, normalizeQuery = true } = options;

    // Normalize query if needed
    const searchQuery = normalizeQuery ? this.normalizeText(query) : query;
    const results = [];

    // Search across entity types
    if (entityType === "all" || entityType === "project") {
      const projectResults = this.indexCache.projects.search(searchQuery);
      results.push(
        ...projectResults.map((r) => ({
          ...r.item,
          type: "project",
          score: r.score,
          confidence: 1 - r.score,
        })),
      );
    }

    if (entityType === "all" || entityType === "screen") {
      const screenResults = this.indexCache.screens.search(searchQuery);
      results.push(
        ...screenResults.map((r) => ({
          ...r.item,
          type: "screen",
          score: r.score,
          confidence: 1 - r.score,
        })),
      );
    }

    if (entityType === "all" || entityType === "subsystem") {
      const subsystemResults = this.indexCache.subsystems.search(searchQuery);
      results.push(
        ...subsystemResults.map((r) => ({
          ...r.item,
          type: "subsystem",
          score: r.score,
          confidence: 1 - r.score,
        })),
      );
    }

    // Filter by threshold and sort
    return results
      .filter((r) => r.confidence >= threshold)
      .sort((a, b) => b.confidence - a.confidence)
      .slice(0, limit);
  }

  /**
   * Rebuild indexes (call when data changes)
   */
  async rebuildIndexes() {
    this.isInitialized = false;
    await this.initialize();
  }
}

// Export singleton instance
export default new FuzzySearchService();
```

### Step 2.2: Add Fuzzy Search Tool to DatabaseQueryTool

```javascript
// DatabaseQueryTool.js

import fuzzySearchService from './search/FuzzySearchService.js';

// Add to defineFunctions()
{
  name: "fuzzy_search",
  description: "Tìm kiếm xấp xỉ khi không có kết quả chính xác. Hỗ trợ typo, viết tắt, và các biến thể tên.",
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
        description: "Loại entity cần tìm",
        default: "all"
      },
      threshold: {
        type: "number",
        description: "Ngưỡng tương đồng (0.0-1.0), mặc định 0.6",
        default: 0.6
      }
    },
    required: ["query"]
  },
  handler: this.fuzzySearch.bind(this)
},

// Add handler method
async fuzzySearch({ query, entity_type = 'all', threshold = 0.6 }) {
  try {
    const results = await fuzzySearchService.search(query, entity_type, {
      threshold,
      limit: 10,
      normalizeQuery: true
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
        description: r.description || '',
        confidence: r.confidence.toFixed(2)
      })),
      note: results.length === 0
        ? 'Không tìm thấy kết quả tương tự. Thử điều chỉnh từ khóa.'
        : null
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}
```

### Step 2.3: Initialize on App Load

```javascript
// App.js - trong init()

import fuzzySearchService from './services/search/FuzzySearchService.js';

async init() {
  // ... existing code ...

  // Initialize fuzzy search
  try {
    await fuzzySearchService.initialize();
  } catch (error) {
    console.warn('Fuzzy search initialization failed:', error);
  }

  // ... rest of init ...
}
```

---

## 3. Create Synonyms Dictionary

### Step 3.1: Create synonyms.json

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
        "chargeback",
        "hoan tien"
      ],
      "payment": [
        "thanh toán",
        "payment",
        "pay",
        "chi trả",
        "transaction",
        "thanh toan"
      ],
      "invoice": ["hóa đơn", "invoice", "bill", "receipt", "hoa don"],
      "revenue": ["doanh thu", "revenue", "income", "doanh_thu"],
      "fee": ["phí", "fee", "charge", "cost", "phi"]
    },
    "workflow": {
      "approval": [
        "phê duyệt",
        "duyệt",
        "approval",
        "approve",
        "authorize",
        "phe duyet"
      ],
      "review": [
        "xem xét",
        "review",
        "check",
        "verify",
        "kiểm tra",
        "kiem tra"
      ],
      "submit": ["nộp", "submit", "send", "gửi", "nop", "gui"],
      "reject": ["từ chối", "reject", "deny", "decline", "tu choi"]
    },
    "security": {
      "authentication": [
        "xác thực",
        "authentication",
        "auth",
        "login",
        "đăng nhập",
        "xac thuc",
        "dang nhap"
      ],
      "authorization": [
        "phân quyền",
        "authorization",
        "permission",
        "quyền hạn",
        "phan quyen"
      ],
      "risk": [
        "rủi ro",
        "risk",
        "fraud",
        "gian lận",
        "security",
        "rui ro",
        "gian lan"
      ],
      "audit": [
        "kiểm toán",
        "audit",
        "compliance",
        "tuân thủ",
        "kiem toan",
        "tuan thu"
      ]
    },
    "technology": {
      "frontend": ["frontend", "client", "UI", "giao diện", "web", "giao dien"],
      "backend": ["backend", "server", "API", "service", "dịch vụ", "dich vu"],
      "database": [
        "database",
        "DB",
        "cơ sở dữ liệu",
        "table",
        "bảng",
        "co so du lieu",
        "bang"
      ],
      "react": ["react", "reactjs", "jsx", "tsx", "react.js"],
      "java": ["java", "spring", "springboot", "spring boot", "backend"]
    },
    "general": {
      "user": [
        "người dùng",
        "user",
        "customer",
        "khách hàng",
        "client",
        "nguoi dung",
        "khach hang"
      ],
      "admin": ["quản trị", "admin", "administrator", "manager", "quan tri"],
      "system": [
        "hệ thống",
        "system",
        "platform",
        "nền tảng",
        "he thong",
        "nen tang"
      ],
      "data": [
        "dữ liệu",
        "data",
        "information",
        "thông tin",
        "du lieu",
        "thong tin"
      ],
      "screen": ["màn hình", "screen", "page", "trang", "man hinh"],
      "project": ["dự án", "project", "du an"]
    }
  },
  "abbreviations": {
    "auth": "authentication",
    "DB": "database",
    "UI": "user interface",
    "API": "application programming interface",
    "BPMN": "business process model notation",
    "PCM": "project & compliance management",
    "QA": "quality assurance",
    "prod": "production",
    "dev": "development"
  },
  "common_typos": {
    "hoan tien": "hoàn tiền",
    "thanh toan": "thanh toán",
    "phe duyet": "phê duyệt",
    "xac thuc": "xác thực",
    "kiem tra": "kiểm tra",
    "du an": "dự án",
    "man hinh": "màn hình",
    "he thong": "hệ thống"
  }
}
```

### Step 3.2: Create SemanticSearchService.js

```javascript
/**
 * Semantic Search Service
 * Expands queries with synonyms for better search coverage
 */
import fuzzySearchService from "./FuzzySearchService.js";

export class SemanticSearchService {
  constructor() {
    this.synonymsDict = null;
    this.isInitialized = false;
  }

  /**
   * Load synonyms dictionary
   */
  async initialize() {
    if (this.isInitialized) return;

    try {
      const response = await fetch("/public/data/synonyms.json");
      this.synonymsDict = await response.json();
      this.isInitialized = true;
      console.log("✅ Synonyms dictionary loaded");
    } catch (error) {
      console.error("❌ Failed to load synonyms:", error);
      // Fallback to empty dict
      this.synonymsDict = {
        categories: {},
        abbreviations: {},
        common_typos: {},
      };
      this.isInitialized = true;
    }
  }

  /**
   * Normalize text (remove accents, lowercase)
   */
  normalizeText(text) {
    return text
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .toLowerCase()
      .trim();
  }

  /**
   * Expand query with synonyms
   */
  expandQueryWithSynonyms(query) {
    if (!this.isInitialized) {
      return [query];
    }

    const queryLower = this.normalizeText(query);
    const expandedTerms = new Set([query, queryLower]);

    // Check common typos first
    for (const [typo, correct] of Object.entries(
      this.synonymsDict.common_typos || {},
    )) {
      if (queryLower.includes(this.normalizeText(typo))) {
        expandedTerms.add(correct);
        expandedTerms.add(this.normalizeText(correct));
      }
    }

    // Check abbreviations
    for (const [abbr, full] of Object.entries(
      this.synonymsDict.abbreviations || {},
    )) {
      if (queryLower.includes(abbr.toLowerCase())) {
        expandedTerms.add(full);
        expandedTerms.add(this.normalizeText(full));
      }
    }

    // Check categories
    for (const category of Object.values(this.synonymsDict.categories || {})) {
      for (const [key, synonyms] of Object.entries(category)) {
        // If query contains any synonym, add all synonyms
        const normalized = synonyms.map((s) => this.normalizeText(s));
        if (normalized.some((syn) => queryLower.includes(syn))) {
          synonyms.forEach((syn) => {
            expandedTerms.add(syn);
            expandedTerms.add(this.normalizeText(syn));
          });
        }
      }
    }

    return Array.from(expandedTerms);
  }

  /**
   * Semantic search with synonym expansion
   */
  async search(query, entityType = "all", options = {}) {
    if (!this.isInitialized) {
      await this.initialize();
    }

    const { expandSynonyms = true, threshold = 0.6, limit = 10 } = options;

    // Expand query with synonyms
    const searchTerms = expandSynonyms
      ? this.expandQueryWithSynonyms(query)
      : [query];

    console.log("🔍 Semantic search:", {
      original: query,
      expanded: searchTerms,
    });

    // Search with all expanded terms
    const allResults = new Map(); // Deduplicate by type-id

    for (const term of searchTerms) {
      const results = await fuzzySearchService.search(term, entityType, {
        threshold: threshold * 0.9, // Slightly lower threshold for expanded terms
        limit: limit * 2, // Get more results to compensate for deduplication
        normalizeQuery: false, // Already normalized
      });

      results.forEach((result) => {
        const key = `${result.type}-${result.id}`;
        if (
          !allResults.has(key) ||
          result.confidence > allResults.get(key).confidence
        ) {
          allResults.set(key, {
            ...result,
            matched_term: term,
          });
        }
      });
    }

    // Sort and limit
    return Array.from(allResults.values())
      .sort((a, b) => b.confidence - a.confidence)
      .slice(0, limit);
  }
}

// Export singleton instance
export default new SemanticSearchService();
```

### Step 3.3: Add Semantic Search Tool

```javascript
// DatabaseQueryTool.js

import semanticSearchService from './search/SemanticSearchService.js';

// Add to defineFunctions()
{
  name: "semantic_search",
  description: "Tìm kiếm theo ngữ nghĩa, tự động mở rộng từ đồng nghĩa và các biến thể (tiếng Việt + tiếng Anh).",
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
      entity_type: {
        type: "string",
        enum: ["project", "screen", "subsystem", "all"],
        default: "all"
      }
    },
    required: ["query"]
  },
  handler: this.semanticSearch.bind(this)
},

// Add handler
async semanticSearch({ query, expand_synonyms = true, entity_type = 'all' }) {
  try {
    const results = await semanticSearchService.search(query, entity_type, {
      expandSynonyms: expand_synonyms,
      threshold: 0.6,
      limit: 10
    });

    const expandedTerms = semanticSearchService.expandQueryWithSynonyms(query);

    return {
      success: true,
      query: query,
      expanded_terms: expandedTerms.slice(0, 5), // Show first 5 expanded terms
      entity_type: entity_type,
      count: results.length,
      results: results.map(r => ({
        id: r.id,
        name: r.name,
        type: r.type,
        description: r.description || '',
        confidence: r.confidence.toFixed(2),
        matched_term: r.matched_term
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

## 4. Add Intent Detection

### Step 4.1: Create IntentDetector.js

```javascript
/**
 * Intent Detector
 * Detects user intent from queries to suggest appropriate tools
 */
export class IntentDetector {
  constructor() {
    this.patterns = {
      search: {
        keywords: [
          /tìm|search|find|có .* nào/i,
          /dự án|project|screen|màn hình/i,
          /liên quan|related to/i,
        ],
        weight: 1.0,
      },
      details: {
        keywords: [
          /chi tiết|detail|thông tin|information/i,
          /làm gì|what does|how does|explain/i,
          /là gì|what is/i,
        ],
        weight: 1.0,
      },
      analysis: {
        keywords: [
          /workflow|luồng|flow|journey|đường đi/i,
          /impact|ảnh hưởng|liên quan|related|affected/i,
          /phân tích|analysis|analyze/i,
        ],
        weight: 1.0,
      },
      statistics: {
        keywords: [
          /bao nhiêu|how many|count|số lượng/i,
          /thống kê|statistics|stats/i,
          /tổng|total|danh sách|list all/i,
          /\d+ project|\d+ screen/i,
        ],
        weight: 1.0,
      },
      development: {
        keywords: [
          /file|source|code|sửa|modify|fix/i,
          /cần|need to|phải|should|require/i,
          /implement|triển khai|xây dựng/i,
          /\.tsx|\.java|\.jsx|\.sql/i,
        ],
        weight: 1.0,
      },
      validation: {
        keywords: [
          /kiểm tra|check|validate|verify/i,
          /thiếu|missing|gap|incomplete|không đầy đủ/i,
          /đúng|correct|sai|wrong|error/i,
        ],
        weight: 1.0,
      },
    };

    // Tool mapping for each intent
    this.toolMapping = {
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
      general: ["list_subsystems", "get_statistics"], // Fallback
    };
  }

  /**
   * Detect intent from user query
   */
  detectIntent(query) {
    const scores = {};
    const queryLower = query.toLowerCase();

    // Calculate scores for each intent
    for (const [intent, config] of Object.entries(this.patterns)) {
      let score = 0;
      for (const pattern of config.keywords) {
        if (pattern.test(queryLower)) {
          score += config.weight;
        }
      }
      scores[intent] = score;
    }

    // Find intent with highest score
    const maxScore = Math.max(...Object.values(scores));

    if (maxScore === 0) {
      return {
        primary: "general",
        confidence: 0.5,
        scores: scores,
        suggestedTools: this.toolMapping.general,
      };
    }

    const primaryIntent = Object.keys(scores).find(
      (intent) => scores[intent] === maxScore,
    );

    const confidence = Math.min(
      maxScore / this.patterns[primaryIntent].keywords.length,
      1.0,
    );

    return {
      primary: primaryIntent,
      confidence: confidence,
      scores: scores,
      suggestedTools: this.toolMapping[primaryIntent],
    };
  }

  /**
   * Suggest tools based on intent
   */
  suggestTools(intent) {
    if (typeof intent === "string") {
      return this.toolMapping[intent] || this.toolMapping.general;
    }
    return intent.suggestedTools || this.toolMapping.general;
  }

  /**
   * Extract entities from query (project IDs, screen IDs, keywords)
   */
  extractEntities(query) {
    const entities = {
      projectId: null,
      screenId: null,
      subsystemName: null,
      keywords: [],
    };

    // Extract project ID
    const projectIdMatch = query.match(/project[:\s#]+(\d+)/i);
    if (projectIdMatch) {
      entities.projectId = parseInt(projectIdMatch[1]);
    }

    // Extract screen ID
    const screenIdMatch = query.match(/screen[:\s#]+(\d+)/i);
    if (screenIdMatch) {
      entities.screenId = parseInt(screenIdMatch[1]);
    }

    // Extract subsystem mention
    if (/subsystem|hệ thống con/i.test(query)) {
      entities.subsystemName = "detected";
    }

    // Extract keywords (words longer than 3 chars, not common words)
    const commonWords = [
      "the",
      "and",
      "for",
      "with",
      "project",
      "screen",
      "system",
      "của",
      "và",
      "cho",
      "với",
    ];
    const words = query.toLowerCase().split(/\s+/);
    entities.keywords = words.filter(
      (w) => w.length > 3 && !commonWords.includes(w) && !/^\d+$/.test(w),
    );

    return entities;
  }
}

// Export singleton instance
export default new IntentDetector();
```

### Step 4.2: Integrate Intent Detection into AIPanel

```javascript
// AIPanel.js

import intentDetector from '../utils/ai/IntentDetector.js';

// Modify handleUserMessage method
async handleUserMessage(message) {
  // ... existing validation ...

  // Detect intent
  const intent = intentDetector.detectIntent(message);
  console.log('🎯 Detected intent:', intent);

  // Log intent for analytics
  this.logIntent(intent);

  // Use intent to guide tool selection
  if (this.enableFunctionCalling && intent.confidence > 0.7) {
    // High confidence intent - suggest specific tools
    const systemPrompt = this.buildIntentAwareSystemPrompt(intent);
    // ... continue with function calling ...
  }

  // ... rest of method ...
}

buildIntentAwareSystemPrompt(intent) {
  let prompt = `User's question intent: ${intent.primary} (confidence: ${intent.confidence.toFixed(2)})\n`;
  prompt += `Suggested tools: ${intent.suggestedTools.join(', ')}\n\n`;

  // Add intent-specific instructions
  switch (intent.primary) {
    case 'development':
      prompt += 'Focus on source files and database tables. Use find_source_by_feature tool.\n';
      break;
    case 'analysis':
      prompt += 'Focus on relationships and workflows. Use analyze_relationships or trace_user_journey.\n';
      break;
    case 'search':
      prompt += 'If no exact match, try fuzzy_search or semantic_search.\n';
      break;
  }

  return prompt;
}

logIntent(intent) {
  // Log for analytics
  const logs = JSON.parse(localStorage.getItem('intent_logs') || '[]');
  logs.push({
    timestamp: Date.now(),
    intent: intent.primary,
    confidence: intent.confidence
  });
  localStorage.setItem('intent_logs', JSON.stringify(logs.slice(-100)));
}
```

---

## 5. Integrate Anti-Hallucination

### Step 5.1: Create ResponseGroundingChecker.js

```javascript
/**
 * Response Grounding Checker
 * Validates AI responses to prevent hallucinations
 */
import databaseManager from "../services/DatabaseManager.js";

export class ResponseGroundingChecker {
  constructor() {
    this.citationPatterns = [
      /Nguồn:/i,
      /Dựa vào/i,
      /Theo dữ liệu/i,
      /From data/i,
      /Based on/i,
      /\(Project ID: \d+\)/i,
      /\(Screen ID: \d+\)/i,
    ];
  }

  /**
   * Check if response is grounded in data
   */
  async checkGrounding(aiResponse, toolResults) {
    const analysis = {
      isGrounded: true,
      hasCitations: false,
      hasToolData: false,
      hallucinations: [],
      confidence: 1.0,
      warnings: [],
    };

    // Check 1: Tool results exist
    if (!toolResults || toolResults.length === 0) {
      analysis.isGrounded = false;
      analysis.hasToolData = false;
      analysis.confidence = 0.3;
      analysis.warnings.push("Response không dựa trên dữ liệu từ tools");
      return analysis;
    }

    analysis.hasToolData = true;

    // Check 2: Citations present
    analysis.hasCitations = this.citationPatterns.some((pattern) =>
      pattern.test(aiResponse),
    );

    if (!analysis.hasCitations) {
      analysis.confidence *= 0.7;
      analysis.warnings.push("Response không có trích dẫn nguồn rõ ràng");
    }

    // Check 3: Verify entity mentions
    await this.verifyEntityMentions(aiResponse, analysis);

    // Check 4: Verify numbers match tool data
    this.verifyNumbers(aiResponse, toolResults, analysis);

    // Calculate final grounding status
    analysis.isGrounded =
      analysis.hallucinations.length === 0 &&
      analysis.hasCitations &&
      analysis.hasToolData;

    analysis.confidence = Math.max(
      0.2,
      analysis.confidence - analysis.hallucinations.length * 0.2,
    );

    return analysis;
  }

  /**
   * Verify entity mentions exist in database
   */
  async verifyEntityMentions(response, analysis) {
    // Extract project mentions
    const projectMentions =
      response.match(/(?:project|dự án)\s+["']([^"'\n,]+)["']/gi) || [];

    for (const mention of projectMentions) {
      const projectName = mention
        .replace(/(?:project|dự án)\s+/gi, "")
        .replace(/['"]/g, "")
        .trim();

      const exists = await this.verifyProjectExists(projectName);
      if (!exists) {
        analysis.hallucinations.push({
          type: "non_existent_project",
          entity: projectName,
          message: `Project "${projectName}" không tồn tại trong database`,
        });
      }
    }

    // Extract screen mentions
    const screenMentions =
      response.match(/(?:screen|màn hình)\s+["']([^"'\n,]+)["']/gi) || [];

    for (const mention of screenMentions) {
      const screenName = mention
        .replace(/(?:screen|màn hình)\s+/gi, "")
        .replace(/['"]/g, "")
        .trim();

      const exists = await this.verifyScreenExists(screenName);
      if (!exists) {
        analysis.hallucinations.push({
          type: "non_existent_screen",
          entity: screenName,
          message: `Screen "${screenName}" không tồn tại trong database`,
        });
      }
    }
  }

  /**
   * Verify numbers in response match tool data
   */
  verifyNumbers(response, toolResults, analysis) {
    // Extract numbers from response
    const numberMatches =
      response.match(/\d+\s+(?:projects?|screens?|subsystems?)/gi) || [];

    // Extract numbers from tool results
    const toolNumbers = {};
    toolResults.forEach((result) => {
      try {
        const data =
          typeof result.content === "string"
            ? JSON.parse(result.content)
            : result.content;

        if (data.count !== undefined) {
          toolNumbers[result.name] = data.count;
        }
        if (data.projects?.length) {
          toolNumbers.projects = data.projects.length;
        }
        if (data.screens?.length) {
          toolNumbers.screens = data.screens.length;
        }
      } catch (e) {
        // Ignore parsing errors
      }
    });

    // Compare (simple heuristic - can be improved)
    if (numberMatches.length > 0 && Object.keys(toolNumbers).length === 0) {
      analysis.warnings.push(
        "Response chứa số liệu nhưng không tìm thấy trong tool results",
      );
    }
  }

  /**
   * Verify project exists in database
   */
  async verifyProjectExists(projectName) {
    try {
      const projects = await databaseManager.searchProjects(projectName);
      return projects.some(
        (p) =>
          p.name.toLowerCase() === projectName.toLowerCase() ||
          p.shortName?.toLowerCase() === projectName.toLowerCase(),
      );
    } catch {
      return false;
    }
  }

  /**
   * Verify screen exists in database
   */
  async verifyScreenExists(screenName) {
    try {
      const screens = await databaseManager.searchScreens(screenName);
      return screens.some(
        (s) => s.name.toLowerCase() === screenName.toLowerCase(),
      );
    } catch {
      return false;
    }
  }

  /**
   * Format warning message for UI
   */
  formatWarning(analysis) {
    if (analysis.isGrounded && analysis.warnings.length === 0) {
      return null;
    }

    let warning = "⚠️ **Cảnh báo Chất Lượng Phản Hồi:**\n\n";

    if (analysis.warnings.length > 0) {
      warning += "**Lưu ý:**\n";
      analysis.warnings.forEach((w, i) => {
        warning += `${i + 1}. ${w}\n`;
      });
      warning += "\n";
    }

    if (analysis.hallucinations.length > 0) {
      warning += "**Thông tin chưa được xác minh:**\n";
      analysis.hallucinations.forEach((h, i) => {
        warning += `${i + 1}. ${h.message}\n`;
      });
      warning += "\n";
    }

    warning += `*Độ tin cậy: ${(analysis.confidence * 100).toFixed(0)}%*\n`;
    warning += "*Vui lòng kiểm tra lại thông tin quan trọng.*";

    return warning;
  }
}

// Export singleton instance
export default new ResponseGroundingChecker();
```

### Step 5.2: Integrate into AIPanel

```javascript
// AIPanel.js

import responseGroundingChecker from '../utils/ai/ResponseGroundingChecker.js';

// After getting AI response in handleFunctionCallingMode
async handleFunctionCallingMode(message, conversationMessages) {
  // ... existing code to call LLM and execute tools ...

  // After final response
  if (finalResponse && toolResults.length > 0) {
    // Check grounding
    const grounding = await responseGroundingChecker.checkGrounding(
      finalResponse.content,
      toolResults
    );

    console.log('🛡️ Grounding check:', grounding);

    // If not grounded, append warning
    if (!grounding.isGrounded || grounding.warnings.length > 0) {
      const warning = responseGroundingChecker.formatWarning(grounding);
      if (warning) {
        finalResponse.content += '\n\n---\n\n' + warning;
      }
    }

    // Log for analytics
    this.logGrounding(grounding);
  }

  // ... continue rendering response ...
}

logGrounding(grounding) {
  const logs = JSON.parse(localStorage.getItem('grounding_logs') || '[]');
  logs.push({
    timestamp: Date.now(),
    isGrounded: grounding.isGrounded,
    confidence: grounding.confidence,
    hallucinations: grounding.hallucinations.length
  });
  localStorage.setItem('grounding_logs', JSON.stringify(logs.slice(-100)));
}
```

---

## 6. Testing & Validation

### Step 6.1: Unit Tests

```javascript
// tests/search.test.js
import fuzzySearchService from "../public/js/services/search/FuzzySearchService.js";
import semanticSearchService from "../public/js/services/search/SemanticSearchService.js";

describe("Fuzzy Search", () => {
  beforeAll(async () => {
    await fuzzySearchService.initialize();
  });

  test('should find "hoàn tiền" from typo "hoan tien"', async () => {
    const results = await fuzzySearchService.search("hoan tien", "project", {
      threshold: 0.6,
    });

    expect(results.length).toBeGreaterThan(0);
    expect(
      results.some((r) => r.name.toLowerCase().includes("hoàn tiền")),
    ).toBe(true);
  });

  test("should handle abbreviations", async () => {
    const results = await fuzzySearchService.search("refnd", "project", {
      threshold: 0.7,
    });

    expect(results.some((r) => r.name.toLowerCase().includes("refund"))).toBe(
      true,
    );
  });
});

describe("Semantic Search", () => {
  beforeAll(async () => {
    await semanticSearchService.initialize();
  });

  test("should expand synonyms", async () => {
    const expandedTerms =
      semanticSearchService.expandQueryWithSynonyms("authentication");

    expect(expandedTerms).toContain("xác thực");
    expect(expandedTerms).toContain("login");
  });

  test("should find results with expanded terms", async () => {
    const results = await semanticSearchService.search("thanh toán", "project");

    // Should find both "thanh toán" and "payment"
    expect(results.length).toBeGreaterThan(0);
  });
});
```

### Step 6.2: Integration Tests

```javascript
// tests/intent.test.js
import intentDetector from "../public/js/utils/ai/IntentDetector.js";

describe("Intent Detection", () => {
  test("should detect search intent", () => {
    const intent = intentDetector.detectIntent("Tìm dự án liên quan hoàn tiền");

    expect(intent.primary).toBe("search");
    expect(intent.confidence).toBeGreaterThan(0.7);
    expect(intent.suggestedTools).toContain("search_projects");
  });

  test("should detect development intent", () => {
    const intent = intentDetector.detectIntent("File nào xử lý risk review?");

    expect(intent.primary).toBe("development");
    expect(intent.suggestedTools).toContain("find_source_by_feature");
  });

  test("should detect statistics intent", () => {
    const intent = intentDetector.detectIntent("Bao nhiêu project dùng React?");

    expect(intent.primary).toBe("statistics");
    expect(intent.suggestedTools).toContain("get_statistics");
  });
});
```

### Step 6.3: Manual Testing Checklist

**Test Cases:**

1. **Fuzzy Search**
   - [ ] Typo: `"hoan tien"` → finds `"hoàn tiền"`
   - [ ] Abbreviation: `"refnd"` → finds `"refund"`
   - [ ] No accent: `"xac thuc"` → finds `"xác thực"`
   - [ ] Partial match: `"pay"` → finds `"payment systems"`

2. **Semantic Search**
   - [ ] Vietnamese: `"thanh toán"` → finds `"payment"`
   - [ ] English: `"authentication"` → finds `"xác thực"`
   - [ ] Synonym: `"approval"` → finds `"phê duyệt"`
   - [ ] Multi-word: `"risk review"` → finds `"xem xét rủi ro"`

3. **Intent Detection**
   - [ ] Search: `"Tìm dự án payment"` → search intent
   - [ ] Details: `"Chi tiết project 12"` → details intent
   - [ ] Analysis: `"Workflow hoàn tiền thế nào?"` → analysis intent
   - [ ] Development: `"File nào xử lý approval?"` → development intent

4. **Anti-Hallucination**
   - [ ] With tool data: Response has citations → ✅ grounded
   - [ ] Without tool data: Response makes claims → ⚠️ warning
   - [ ] Fake entity: Mentions non-existent project → 🚨 hallucination
   - [ ] Correct numbers: Matches tool results → ✅ verified

---

## 7. Performance Monitoring

### Step 7.1: Add Performance Metrics

```javascript
// utils/PerformanceMonitor.js

export class PerformanceMonitor {
  static measureAsync(name, asyncFn) {
    const start = performance.now();
    return asyncFn().finally(() => {
      const duration = performance.now() - start;
      console.log(`⏱️ [Performance] ${name}: ${duration.toFixed(2)}ms`);

      // Log to analytics
      this.logMetric(name, duration);
    });
  }

  static logMetric(name, duration) {
    const metrics = JSON.parse(
      localStorage.getItem("performance_metrics") || "{}",
    );
    if (!metrics[name]) {
      metrics[name] = [];
    }
    metrics[name].push({
      timestamp: Date.now(),
      duration: duration,
    });

    // Keep only last 50 measurements per metric
    metrics[name] = metrics[name].slice(-50);

    localStorage.setItem("performance_metrics", JSON.stringify(metrics));
  }

  static getReport() {
    const metrics = JSON.parse(
      localStorage.getItem("performance_metrics") || "{}",
    );
    const report = {};

    for (const [name, measurements] of Object.entries(metrics)) {
      const durations = measurements.map((m) => m.duration);
      report[name] = {
        count: durations.length,
        avg: (durations.reduce((a, b) => a + b, 0) / durations.length).toFixed(
          2,
        ),
        min: Math.min(...durations).toFixed(2),
        max: Math.max(...durations).toFixed(2),
      };
    }

    return report;
  }
}
```

### Step 7.2: Integrate Monitoring

```javascript
// Use in services

import { PerformanceMonitor } from '../utils/PerformanceMonitor.js';

async search(query, entityType, options) {
  return PerformanceMonitor.measureAsync(
    `fuzzy_search_${entityType}`,
    async () => {
      // ... actual search logic ...
    }
  );
}
```

---

## ✅ Checklist Hoàn Thành

### Phase 1: Core Setup

- [ ] Cài đặt dependencies (fuse.js, dompurify)
- [ ] Tạo file structures
- [ ] Create `synonyms.json`
- [ ] Initialize services on app load

### Phase 2: Search Implementation

- [ ] Implement `FuzzySearchService`
- [ ] Implement `SemanticSearchService`
- [ ] Add `fuzzy_search` tool to DatabaseQueryTool
- [ ] Add `semantic_search` tool to DatabaseQueryTool
- [ ] Test search với typos và synonyms

### Phase 3: Intent Detection

- [ ] Implement `IntentDetector`
- [ ] Integrate into AIPanel
- [ ] Test intent detection với various queries
- [ ] Add intent-aware prompts

### Phase 4: Anti-Hallucination

- [ ] Implement `ResponseGroundingChecker`
- [ ] Integrate grounding checks into AIPanel
- [ ] Add warning display in UI
- [ ] Test với grounded và ungrounded responses

### Phase 5: Testing & Monitoring

- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Manual testing với test cases
- [ ] Add performance monitoring
- [ ] Review analytics data

---

## 📞 Support

**Tài liệu liên quan:**

- `UPGRADE_RECOMMENDATIONS.md` - Chi tiết đầy đủ
- `TOOLS_QUICK_REFERENCE.md` - Tham khảo nhanh
- `ai-panel-review.md` - Issues và improvements

**Team contacts:**

- Frontend: @frontend-team
- AI/ML: @ai-team
- Testing: @qa-team

---

_Implementation Guide v1.0.0 | Last updated: 2025-11-07_
