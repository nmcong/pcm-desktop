# AI Database Query System

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [How It Works](#how-it-works)
- [Available Functions](#available-functions)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Extending the System](#extending-the-system)
- [Technical Implementation](#technical-implementation)
- [Best Practices](#best-practices)

---

## Overview

The AI Database Query System enables AI assistants to directly query and retrieve data from IndexedDB without requiring
a backend server. This provides intelligent, context-aware responses based on real application data.

### Key Features

- **🗄️ Direct IndexedDB Access**: AI can query local database
- **🤖 Auto Context Injection**: Automatically detects user intent and injects relevant data
- **🚀 Zero Backend**: Everything runs client-side
- **🔒 Privacy First**: Data never leaves the browser
- **📊 7 Query Functions**: Comprehensive data access
- **⚡ Real-time**: Instant responses with fresh data

### Use Cases

1. **Project Discovery**: "Show me all projects in the Authentication subsystem"
2. **Screen Analysis**: "What events are defined on the Login screen?"
3. **Workflow Understanding**: "Explain the user registration workflow"
4. **Technology Search**: "Which screens use JSP files?"
5. **Architecture Overview**: "List all subsystems and their projects"

---

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                         User Input                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      AIPanel.js                              │
│  - Receives user message                                     │
│  - Checks if enableDatabaseAccess is true                    │
│  - Calls DatabaseQueryTool.detectAndInjectContext()          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│               DatabaseQueryTool.js                           │
│  - Keyword detection (project, screen, subsystem, etc.)      │
│  - Executes appropriate query function                       │
│  - Queries IndexedDB via DatabaseManager                     │
│  - Formats results as JSON                                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  DatabaseManager.js                          │
│  - IndexedDB CRUD operations                                 │
│  - Returns projects, screens, subsystems, workflows          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│            Context Injected into User Message                │
│  "Tell me about project X"                                   │
│  +                                                            │
│  "\n\n**Relevant Projects:**\n{JSON data}"                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    AI Provider                               │
│  - Receives enriched message with context                    │
│  - Generates response based on injected data                 │
│  - Returns accurate, data-driven answer                      │
└─────────────────────────────────────────────────────────────┘
```

### File Structure

```
apps/pcm-webapp/public/js/
├── services/
│   ├── DatabaseManager.js          # IndexedDB operations
│   ├── DatabaseQueryTool.js        # NEW: Query functions & context injection
│   └── ChatHistoryManager.js       # Chat history
├── components/
│   └── AIPanel.js                  # UPDATED: Database access integration
└── utils/
    └── constants.js
```

---

## How It Works

### 1. Auto Context Injection (RAG-Style)

When a user sends a message, the system:

1. **Detects Keywords**: Scans message for database-related terms
2. **Executes Query**: Runs appropriate IndexedDB query
3. **Injects Context**: Appends results to user message
4. **Sends to AI**: Provider receives enriched message
5. **AI Responds**: Generates answer based on real data

### 2. Keyword Detection

The system detects the following patterns:

| Keyword Pattern             | Action          | Example                           |
|-----------------------------|-----------------|-----------------------------------|
| `project`, `dự án`          | Search projects | "Tell me about project X"         |
| `screen`, `màn hình`        | Search screens  | "What screens are in the system?" |
| `subsystem`, `hệ thống con` | List subsystems | "Show all subsystems"             |
| `list all`                  | List subsystems | "List all available systems"      |

### 3. Context Injection Format

```javascript
// Original user message
"Tell me about the Login project"

// After context injection
"Tell me about the Login project

**Relevant Projects:**
[
  {
    "id": 1,
    "name": "Login System",
    "shortName": "LOGIN",
    "description": "User authentication and authorization",
    "subsystem": "Authentication",
    "repoUrl": "https://github.com/org/login",
    "environments": {
      "dev": "https://dev.example.com/login",
      "qa": "https://qa.example.com/login",
      "prod": "https://prod.example.com/login"
    },
    "screenCount": 8,
    "isFavorite": true
  }
]"
```

---

## Available Functions

### 1. `search_projects`

Search for projects by name or description.

**Parameters:**

```javascript
{
  query: string; // Search query
}
```

**Returns:**

```javascript
{
  success: true,
  count: 2,
  projects: [
    {
      id: 1,
      name: "Project Name",
      shortName: "PRJ",
      description: "Project description",
      subsystem: "Subsystem Name",
      repoUrl: "https://...",
      environments: { dev: "...", qa: "...", prod: "..." },
      devops: { dev: "...", qa: "...", prod: "..." },
      screenCount: 10,
      isFavorite: true
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("search_projects", { query: "login" });
```

---

### 2. `get_project_details`

Get detailed information about a specific project.

**Parameters:**

```javascript
{
  project_id: number; // Project ID
}
```

**Returns:**

```javascript
{
  success: true,
  project: {
    id: 1,
    name: "Project Name",
    shortName: "PRJ",
    description: "...",
    subsystem: "Subsystem Name",
    repoUrl: "...",
    environments: { ... },
    devops: { ... },
    workflows: [
      {
        id: "wf_1",
        name: "Workflow Name",
        steps: [...]
      }
    ],
    screens: [
      {
        id: 1,
        name: "Screen Name",
        description: "...",
        eventCount: 5,
        screenshotCount: 3
      }
    ]
  }
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("get_project_details", {
  project_id: 1,
});
```

---

### 3. `search_screens`

Search for screens across all projects or within a specific project.

**Parameters:**

```javascript
{
  query: string,         // Search query
  project_id?: number    // Optional: filter by project
}
```

**Returns:**

```javascript
{
  success: true,
  count: 5,
  screens: [
    {
      id: 1,
      name: "Screen Name",
      description: "...",
      project: "Project Name",
      projectId: 1,
      events: [...],
      permissions: [...],
      sourceFiles: ["file1.jsp", "file2.java"],
      databaseTables: ["users", "roles"],
      notes: "..."
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("search_screens", {
  query: "dashboard",
  project_id: 1,
});
```

---

### 4. `get_screen_events`

Get all events defined on a specific screen.

**Parameters:**

```javascript
{
  screen_id: number; // Screen ID
}
```

**Returns:**

```javascript
{
  success: true,
  screen: {
    id: 1,
    name: "Screen Name"
  },
  events: [
    {
      label: "Login Button",
      description: "Navigate to dashboard after login",
      actionType: "navigate",
      targetScreen: "Dashboard Screen",
      branchQuestion: null,
      branchOptions: []
    },
    {
      label: "User Type Check",
      description: "Branch based on user role",
      actionType: "branch",
      targetScreen: null,
      branchQuestion: "What is the user type?",
      branchOptions: [
        {
          answer: "Admin",
          actionType: "navigate",
          targetScreen: "Admin Dashboard"
        },
        {
          answer: "User",
          actionType: "navigate",
          targetScreen: "User Dashboard"
        }
      ]
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("get_screen_events", { screen_id: 5 });
```

---

### 5. `list_subsystems`

List all subsystems with their projects.

**Parameters:**

```javascript
{
} // No parameters required
```

**Returns:**

```javascript
{
  success: true,
  count: 3,
  subsystems: [
    {
      id: 1,
      name: "Subsystem Name",
      description: "Subsystem description",
      projectCount: 5,
      projects: [
        {
          id: 1,
          name: "Project Name",
          shortName: "PRJ"
        }
      ]
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("list_subsystems", {});
```

---

### 6. `get_workflow`

Get workflow information for a project.

**Parameters:**

```javascript
{
  project_id: number; // Project ID
}
```

**Returns:**

```javascript
{
  success: true,
  project: {
    id: 1,
    name: "Project Name"
  },
  workflows: [
    {
      id: "wf_123",
      name: "User Registration Workflow",
      description: "Steps for new user registration",
      steps: [
        {
          screenName: "Registration Form",
          screenId: 1
        },
        {
          screenName: "Email Verification",
          screenId: 2
        },
        {
          screenName: "Welcome Screen",
          screenId: 3
        }
      ]
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("get_workflow", { project_id: 1 });
```

---

### 7. `search_by_technology`

Search for projects/screens using specific technology or file types.

**Parameters:**

```javascript
{
  technology: string; // Technology or file type (e.g., "java", "jsp", "html")
}
```

**Returns:**

```javascript
{
  success: true,
  technology: "jsp",
  count: 12,
  results: [
    {
      screen: {
        id: 1,
        name: "Login Screen",
        sourceFiles: ["login.jsp", "LoginController.java"]
      },
      project: {
        id: 1,
        name: "Authentication System"
      }
    }
  ]
}
```

**Example Query:**

```javascript
await databaseQueryTool.executeFunction("search_by_technology", {
  technology: "java",
});
```

---

## Configuration

### Enable/Disable Database Access

**Via UI:**

1. Open AI Panel
2. Click Settings (⚙️) icon
3. Scroll to "Conversation Settings"
4. Check/uncheck "🗄️ Enable database access"
5. Click "Save"

**Via Code:**

```javascript
// In AIPanel.js constructor
this.enableDatabaseAccess = true; // or false

// Or in localStorage
localStorage.setItem("ai-database-access", JSON.stringify(true));
```

### Settings Storage

Settings are stored in `localStorage`:

```javascript
// Key
"ai-database-access";

// Value
true; // or false
```

---

## Usage Examples

### Example 1: Finding Projects

**User Query:**

```
"Show me all authentication projects"
```

**System Processing:**

1. Detects keyword: "project"
2. Extracts query: "authentication"
3. Calls: `search_projects({ query: "authentication" })`
4. Injects results into message

**AI Response:**

```
I found 2 authentication projects:

1. **Login System** (LOGIN)
   - Subsystem: Authentication
   - Repository: https://github.com/org/login
   - Screens: 8 screens
   - Environments: Dev, QA, Prod

2. **SSO Integration** (SSO)
   - Subsystem: Authentication
   - Repository: https://github.com/org/sso
   - Screens: 5 screens
   - Environments: Dev, QA, Prod
```

---

### Example 2: Understanding Workflows

**User Query:**

```
"What is the workflow for project ID 1?"
```

**System Processing:**

1. Detects keyword: "workflow"
2. Extracts ID: 1
3. Calls: `get_workflow({ project_id: 1 })`
4. Injects workflow data

**AI Response:**

```
The Login System has the following workflow:

**User Login Workflow:**
1. Login Screen → User enters credentials
2. Authentication Check → Validates credentials
3. Branch: User Type
   - If Admin → Admin Dashboard
   - If Regular User → User Dashboard
4. Dashboard → Main application interface

This workflow has 4 main steps and includes conditional branching based on user roles.
```

---

### Example 3: Technology Stack Analysis

**User Query:**

```
"Which screens use Java files?"
```

**System Processing:**

1. Detects: technology search
2. Calls: `search_by_technology({ technology: "java" })`
3. Injects results

**AI Response:**

```
I found 15 screens using Java files:

1. **Login Screen** (Authentication System)
   - Files: LoginController.java, AuthService.java

2. **User Management** (Admin System)
   - Files: UserController.java, UserService.java

3. **Dashboard** (Core System)
   - Files: DashboardController.java

... (showing 15 total)
```

---

### Example 4: Screen Events

**User Query:**

```
"What events are on the Dashboard screen?"
```

**System Processing:**

1. Detects: "screen" keyword
2. Searches for "Dashboard" screen
3. Calls: `get_screen_events({ screen_id: X })`
4. Injects event data

**AI Response:**

```
The Dashboard screen has 5 events:

1. **Navigate to Profile** (navigate)
   - Trigger: Click user avatar
   - Target: User Profile Screen

2. **Navigate to Settings** (navigate)
   - Trigger: Click settings icon
   - Target: Settings Screen

3. **User Type Branch** (branch)
   - Question: What is the user type?
   - Options:
     • Admin → Admin Panel
     • User → User Dashboard

4. **Logout** (navigate)
   - Trigger: Click logout button
   - Target: Login Screen

5. **Refresh Data** (action)
   - Trigger: Pull to refresh
   - Action: Reload dashboard data
```

---

### Example 5: Subsystem Overview

**User Query:**

```
"List all subsystems in the application"
```

**System Processing:**

1. Detects: "subsystem" or "list all"
2. Calls: `list_subsystems({})`
3. Injects complete hierarchy

**AI Response:**

```
The application has 4 subsystems:

1. **Authentication** (5 projects)
   - Login System
   - SSO Integration
   - Password Reset
   - MFA System
   - Session Management

2. **User Management** (3 projects)
   - User CRUD
   - Role Management
   - Permission System

3. **Core** (7 projects)
   - Dashboard
   - Notifications
   - Search Engine
   - Reports
   - Settings
   - Help Center
   - Admin Panel

4. **Integration** (2 projects)
   - API Gateway
   - External Services
```

---

## Extending the System

### Adding New Query Functions

To add a new query function, follow these steps:

#### Step 1: Define Function in `DatabaseQueryTool.js`

```javascript
// In defineFunctions()
{
  name: "your_function_name",
  description: "Clear description for AI to understand when to use this",
  parameters: {
    type: "object",
    properties: {
      param1: {
        type: "string",
        description: "Parameter description"
      },
      param2: {
        type: "number",
        description: "Another parameter"
      }
    },
    required: ["param1"]
  },
  handler: this.yourFunctionHandler.bind(this)
}
```

#### Step 2: Implement Handler Method

```javascript
async yourFunctionHandler({ param1, param2 }) {
  try {
    // 1. Query IndexedDB via databaseManager
    const data = await databaseManager.yourQueryMethod(param1, param2);

    // 2. Process and format results
    const results = data.map(item => ({
      id: item.id,
      name: item.name,
      // ... format as needed
    }));

    // 3. Return success response
    return {
      success: true,
      count: results.length,
      data: results
    };
  } catch (error) {
    // 4. Return error response
    return {
      success: false,
      error: error.message
    };
  }
}
```

#### Step 3: Add Keyword Detection (Optional)

If you want auto-injection, add keyword detection in `detectAndInjectContext()`:

```javascript
async detectAndInjectContext(userMessage) {
  const message = userMessage.toLowerCase();
  let context = "";

  // ... existing detections ...

  // Your new detection
  if (message.includes("your_keyword") || message.includes("related_term")) {
    const result = await this.yourFunctionHandler({ param1: "value" });
    if (result.success) {
      context += `\n\n**Your Data:**\n${JSON.stringify(result.data, null, 2)}`;
    }
  }

  return context;
}
```

### Example: Adding "Get Recent Changes" Function

```javascript
// 1. Define function
{
  name: "get_recent_changes",
  description: "Get recently modified projects and screens in the last N days",
  parameters: {
    type: "object",
    properties: {
      days: {
        type: "number",
        description: "Number of days to look back (default: 7)"
      }
    }
  },
  handler: this.getRecentChanges.bind(this)
}

// 2. Implement handler
async getRecentChanges({ days = 7 }) {
  try {
    const cutoffDate = Date.now() - (days * 24 * 60 * 60 * 1000);

    const projects = await databaseManager.getProjects();
    const screens = await databaseManager.getScreens();

    const recentProjects = projects.filter(p => p.updatedAt >= cutoffDate);
    const recentScreens = screens.filter(s => s.updatedAt >= cutoffDate);

    return {
      success: true,
      days: days,
      projects: recentProjects.length,
      screens: recentScreens.length,
      changes: {
        projects: recentProjects.map(p => ({
          id: p.id,
          name: p.name,
          updatedAt: new Date(p.updatedAt).toISOString()
        })),
        screens: recentScreens.map(s => ({
          id: s.id,
          name: s.name,
          projectId: s.projectId,
          updatedAt: new Date(s.updatedAt).toISOString()
        }))
      }
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}

// 3. Add keyword detection
if (message.includes("recent") || message.includes("changes") || message.includes("updates")) {
  const result = await this.getRecentChanges({ days: 7 });
  if (result.success) {
    context += `\n\n**Recent Changes:**\n${JSON.stringify(result.changes, null, 2)}`;
  }
}
```

---

## Technical Implementation

### DatabaseQueryTool Class Structure

```javascript
export class DatabaseQueryTool {
  constructor() {
    this.availableFunctions = this.defineFunctions();
  }

  // Core Methods
  defineFunctions()                    // Define all available functions
  executeFunction(name, params)        // Execute a specific function
  getFunctionDefinitions()             // Get definitions for AI providers
  detectAndInjectContext(message)      // Auto-detect and inject context

  // Query Handlers
  async searchProjects({ query })
  async getProjectDetails({ project_id })
  async searchScreens({ query, project_id })
  async getScreenEvents({ screen_id })
  async listSubsystems()
  async getWorkflow({ project_id })
  async searchByTechnology({ technology })
}
```

### Integration Points

#### 1. AIPanel Integration

```javascript
// In AIPanel.js

// Property
this.enableDatabaseAccess = true;

// Before sending to AI
if (this.enableDatabaseAccess) {
  const context = await databaseQueryTool.detectAndInjectContext(userMessage);
  if (context) {
    messages[messages.length - 1].content += context;
  }
}
```

#### 2. Settings UI

```javascript
// Checkbox in settings
<input
  type="checkbox"
  id="database-access"
  ${this.enableDatabaseAccess ? "checked" : ""}
/>
<label>🗄️ Enable database access</label>

// Save handler
const enabled = form.querySelector("#database-access").checked;
localStorage.setItem("ai-database-access", JSON.stringify(enabled));
```

#### 3. Storage

```javascript
// Save
localStorage.setItem("ai-database-access", JSON.stringify(true));

// Load
const saved = localStorage.getItem("ai-database-access");
this.enableDatabaseAccess = saved ? JSON.parse(saved) : true;
```

---

## Best Practices

### 1. Function Design

✅ **DO:**

- Clear, descriptive function names
- Comprehensive parameter descriptions
- Consistent return format: `{ success, data/error }`
- Handle errors gracefully
- Include counts in results
- Use optional parameters with defaults

❌ **DON'T:**

- Return raw database objects without formatting
- Throw uncaught exceptions
- Return inconsistent data structures
- Forget error handling
- Include sensitive data (passwords, tokens)

### 2. Context Injection

✅ **DO:**

- Inject relevant, focused data
- Format as readable JSON
- Include explanatory headers (e.g., "**Relevant Projects:**")
- Limit data size to avoid token overflow
- Log when context is injected (for debugging)

❌ **DON'T:**

- Inject entire database dumps
- Include duplicate data
- Inject without user enabling the feature
- Forget to format data nicely

### 3. Keyword Detection

✅ **DO:**

- Use lowercase for comparison
- Support multiple languages (e.g., "project" and "dự án")
- Use specific, unambiguous keywords
- Test with real user queries
- Be conservative (better false negative than false positive)

❌ **DON'T:**

- Use overly broad keywords
- Detect without clear intent
- Inject wrong data
- Trigger on partial matches incorrectly

### 4. Performance

✅ **DO:**

- Cache frequently accessed data
- Limit result counts (e.g., top 10)
- Use indexes in IndexedDB queries
- Implement pagination for large datasets
- Measure query performance

❌ **DON'T:**

- Query entire database for every message
- Return thousands of results
- Block UI during queries
- Ignore performance issues

### 5. Security & Privacy

✅ **DO:**

- Keep all data in the browser
- Allow users to disable feature
- Filter sensitive information
- Validate all inputs
- Use read-only operations

❌ **DON'T:**

- Send data to external servers
- Include passwords or API keys in results
- Allow data modification via AI
- Expose internal system details
- Trust AI output without validation

---

## Troubleshooting

### Issue: Context Not Injecting

**Symptoms:**

- AI doesn't have access to database data
- Responses are generic

**Solutions:**

1. Check if database access is enabled in settings
2. Verify keyword detection:
   ```javascript
   console.log("📊 Database context injected:", contextInjection);
   ```
3. Ensure IndexedDB has data
4. Check browser console for errors

---

### Issue: Wrong Data Returned

**Symptoms:**

- AI receives incorrect or irrelevant data

**Solutions:**

1. Review keyword detection logic
2. Test query functions independently:
   ```javascript
   const result = await databaseQueryTool.executeFunction("search_projects", {
     query: "test",
   });
   console.log(result);
   ```
3. Verify DatabaseManager queries
4. Check data formatting

---

### Issue: Performance Degradation

**Symptoms:**

- AI responses are slow
- UI freezes during queries

**Solutions:**

1. Limit result counts
2. Add pagination
3. Cache frequent queries
4. Optimize IndexedDB indexes
5. Consider debouncing queries

---

### Issue: Token Limit Exceeded

**Symptoms:**

- AI provider returns errors about token limits
- Responses are cut off

**Solutions:**

1. Reduce injected data size
2. Summarize results instead of full objects
3. Implement data truncation
4. Use pagination with "Show more" option
5. Adjust `maxTokens` in conversation settings

---

## Future Enhancements

### Planned Features

1. **Function Calling Support**
    - Direct integration with OpenAI/Claude function calling
    - More precise query execution
    - Better parameter validation

2. **Semantic Search**
    - Vector embeddings for better search
    - Similarity-based retrieval
    - Natural language understanding

3. **Query History**
    - Track what AI queries
    - Analytics and insights
    - Optimize common queries

4. **Advanced Filters**
    - Date ranges
    - Multiple criteria
    - Complex boolean logic

5. **Caching Layer**
    - Cache frequent queries
    - Invalidation on data changes
    - Performance optimization

6. **Export Capabilities**
    - Export query results
    - CSV/JSON/Excel formats
    - Share with team

---

## Conclusion

The AI Database Query System provides a powerful, privacy-first approach to enabling AI assistants to work with real
application data. By keeping everything client-side and using intelligent context injection, users get accurate,
data-driven responses without compromising security or requiring backend infrastructure.

### Key Takeaways

- ✅ **Zero Backend**: Everything runs in the browser
- ✅ **7 Query Functions**: Comprehensive data access
- ✅ **Auto Context Injection**: Smart detection and enrichment
- ✅ **Privacy First**: Data never leaves the browser
- ✅ **Extensible**: Easy to add new functions
- ✅ **Production Ready**: Error handling, validation, logging

---

## References

- **Main Implementation**: `apps/pcm-webapp/public/js/services/DatabaseQueryTool.js`
- **Integration**: `apps/pcm-webapp/public/js/components/AIPanel.js`
- **Database**: `apps/pcm-webapp/public/js/services/DatabaseManager.js`

---

**Last Updated**: November 6, 2025  
**Version**: 1.0.0  
**Author**: PCM Development Team
