# AI Assistant System - Technical Overview

## Architecture Overview

The AI Assistant system implements a **RAG (Retrieval Augmented Generation)** pattern with **Function Calling** to provide intelligent assistance for project analysis and code understanding.

```
┌─────────────────────────────────────────────────────────────┐
│                      User Interface                          │
│  ┌────────────────────────────────────────────────────────┐ │
│  │          AIAssistantPanel (Component)                  │ │
│  │  - Chat interface                                      │ │
│  │  - Message history                                     │ │
│  │  - Streaming responses                                 │ │
│  └────────────────────────────────────────────────────────┘ │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              AIAssistantService (Core Logic)                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  - Question understanding                              │ │
│  │  - Planning & reasoning                                │ │
│  │  - Function execution orchestration                    │ │
│  │  - Response generation                                 │ │
│  └────────────────────────────────────────────────────────┘ │
└───────────┬────────────────────────────────┬────────────────┘
            │                                │
            ▼                                ▼
┌───────────────────────┐      ┌───────────────────────────┐
│   ProviderRegistry    │      │     Tool Functions        │
│                       │      │                           │
│  - Claude             │      │  Database Tools:          │
│  - OpenAI GPT-4       │      │  - search_projects        │
│  - Gemini             │      │  - get_project_details    │
│  - Hugging Face       │      │  - search_screens         │
│  - ViByte Cloud       │      │  - get_screen_details     │
│                       │      │  - search_knowledge_base  │
└───────────────────────┘      │  - find_related_screens   │
                               │  - get_screen_flow        │
                               │                           │
                               │  GitHub Tools:            │
                               │  - get_file_content       │
                               │  - list_repository_files  │
                               │  - analyze_source_files   │
                               └───────────┬───────────────┘
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    ▼                                             ▼
        ┌───────────────────────┐                  ┌─────────────────────┐
        │   DatabaseManager     │                  │   GitHubService     │
        │                       │                  │                     │
        │  - IndexedDB access   │                  │  - GitHub API       │
        │  - CRUD operations    │                  │  - File content     │
        │  - Query optimization │                  │  - Repository data  │
        └───────────────────────┘                  └─────────────────────┘
```

## Component Structure

### 1. AIAssistantService

**Location:** `public/js/services/ai/AIAssistantService.js`

**Responsibilities:**

- Orchestrates the entire AI conversation flow
- Manages tool definitions and execution
- Handles iterative function calling
- Maintains conversation history
- Provides structured responses with reasoning steps

**Key Methods:**

- `ask(question, options)` - Main entry point for user questions
- `defineTools()` - Registers all available tools/functions
- `buildSystemPrompt()` - Constructs system prompt for LLM
- Tool handlers for each function (search, analyze, etc.)

**Features:**

- ✅ Supports multiple iterations for complex queries
- ✅ Stream callback for real-time progress
- ✅ Automatic conversation history management
- ✅ Error handling and recovery
- ✅ Context-aware tool selection

### 2. AIAssistantPanel

**Location:** `public/js/components/AIAssistantPanel.js`

**Responsibilities:**

- Renders chat interface
- Manages message display
- Handles user input
- Shows reasoning steps
- Provides example questions

**UI Components:**

- Header with title and clear button
- Scrollable messages container
- Message bubbles (user/assistant)
- Reasoning steps display
- Input textarea with auto-resize
- Loading indicators

**Features:**

- ✅ Real-time message streaming
- ✅ Markdown-like formatting
- ✅ Step-by-step reasoning display
- ✅ Example questions for quick start
- ✅ Clear conversation history

### 3. AIAssistantPage

**Location:** `public/js/pages/AIAssistantPage.js`

**Responsibilities:**

- Page wrapper for AI Assistant
- Lifecycle management
- Navigation integration

## Tool Categories

### Database Tools (7 tools)

These tools provide access to the IndexedDB database:

1. **search_projects** - Find projects by keyword
   - Use case: "Find all e-commerce projects"

2. **get_project_details** - Get full project information
   - Use case: "Show me project 1 with all screens"

3. **search_screens** - Find screens by keyword
   - Use case: "Find all login screens"

4. **get_screen_details** - Get screen details with events, notes, source files
   - Use case: "Show me everything about screen 5"

5. **search_knowledge_base** - Search KB articles
   - Use case: "Find solutions for database errors"

6. **find_related_screens** - Find connected screens via navigation
   - Use case: "What screens are related to the checkout?"

7. **get_screen_flow** - Get navigation flow from a screen
   - Use case: "Show me the user flow from login"

### GitHub Tools (3 tools)

These tools interact with GitHub API to fetch source code:

1. **get_file_content** - Fetch file content from repository
   - Use case: "Show me Login.tsx from project 1"

2. **list_repository_files** - List files in a directory
   - Use case: "List all files in src/components"

3. **analyze_source_files** - Analyze all source files linked to a screen
   - Use case: "Analyze the frontend code for screen 3"

## Data Flow

### Typical Query Flow

```
1. User asks question
   ↓
2. AIAssistantService.ask() called
   ↓
3. Build conversation messages
   ↓
4. Call AI Provider with tool definitions
   ↓
5. AI decides which tools to call
   ↓
6. Execute tool functions (multiple iterations possible)
   ↓
7. Provide tool results back to AI
   ↓
8. AI generates final answer
   ↓
9. Display answer with reasoning steps
```

### Function Calling Loop

```javascript
while (iteration < maxIterations) {
  // Call AI with current messages and tools
  response = await provider.chat({
    messages,
    tools: toolDefinitions,
    tool_choice: "auto",
  });

  if (response.tool_calls) {
    // Execute each tool
    for (const toolCall of response.tool_calls) {
      const result = await executeTool(toolCall);
      messages.push({ role: "tool", content: result });
    }
  } else {
    // No more tools to call, we have final answer
    break;
  }
}
```

## Integration Points

### 1. Database Integration

```javascript
// Example: Search projects
const allProjects = await databaseManager.getAll("projects");
const filtered = allProjects.filter((p) =>
  p.name.toLowerCase().includes(query.toLowerCase()),
);
```

### 2. GitHub Integration

```javascript
// Example: Get file content
const { owner, repo } = gitHubService.parseRepoURL(repositoryUrl);
const content = await gitHubService.getFileContent(
  owner,
  repo,
  filePath,
  branch,
);
```

### 3. AI Provider Integration

```javascript
// Example: Call AI with function calling
const response = await provider.chat({
  messages: [
    { role: "system", content: systemPrompt },
    { role: "user", content: question },
  ],
  tools: toolDefinitions,
  tool_choice: "auto",
});
```

## Configuration

### Required Setup

1. **AI Provider**
   - Configure in Settings → AI Provider tab
   - Supported: Claude, OpenAI, Gemini, Hugging Face, ViByte
   - Requires API key for chosen provider

2. **GitHub (Optional)**
   - Configure in Settings → GitHub Integration
   - Requires Personal Access Token
   - Enables source code analysis tools

3. **Database**
   - Automatically initialized on first run
   - No configuration needed

## Security Considerations

### 1. Read-Only Access

- AI Assistant has **read-only** access to database
- Cannot modify, delete, or create data
- Safe to use without data corruption risk

### 2. API Key Security

- API keys stored in localStorage (encrypted recommended)
- Not exposed to AI Assistant
- Only used for provider authentication

### 3. GitHub Token Security

- PAT stored in localStorage
- Requires appropriate scopes (repo:read)
- Can be revoked at any time

### 4. Code Content

- Source code fetched from GitHub is not stored
- Temporary in-memory processing only
- Respects repository access permissions

## Performance Optimization

### 1. Conversation History

- Limited to last 20 messages
- Older messages automatically pruned
- Reduces token usage and latency

### 2. Code Content Truncation

- Files limited to 10,000 characters
- Prevents excessive token usage
- Maintains conversation context

### 3. Query Result Limits

- Projects: Max 10 results
- Screens: Max 10 results
- KB items: Max 5 results
- Source files: Max 5 files

### 4. Caching Strategy

- Database queries not cached (real-time data)
- GitHub file content cached per session
- Provider responses not cached (unique per query)

## Error Handling

### 1. Provider Errors

- Connection timeout → Retry with exponential backoff
- Rate limit → Display friendly message
- Invalid API key → Prompt for reconfiguration

### 2. Database Errors

- Missing data → Graceful "not found" message
- Corrupt data → Skip and continue
- Query errors → Log and return empty result

### 3. GitHub Errors

- Authentication failure → Prompt for token
- File not found → Return error in tool result
- Rate limit → Suggest waiting or upgrading

## Testing Strategy

### Unit Tests

```javascript
// Test tool function
test("search_projects returns filtered results", async () => {
  const result = await aiAssistant.searchProjects({
    query: "login",
  });
  expect(result).toBeArray();
  expect(result[0]).toHaveProperty("id");
});
```

### Integration Tests

```javascript
// Test full conversation flow
test("AI Assistant answers project query", async () => {
  const response = await aiAssistant.ask("What projects do I have?");
  expect(response.answer).toBeDefined();
  expect(response.steps.length).toBeGreaterThan(0);
});
```

### End-to-End Tests

```javascript
// Test UI interaction
test("User can send message and receive response", async () => {
  // Simulate user typing
  const input = screen.getByPlaceholderText("Ask me anything...");
  await userEvent.type(input, "Show me project 1");

  // Submit
  const sendBtn = screen.getByRole("button", { name: /send/i });
  await userEvent.click(sendBtn);

  // Wait for response
  await waitFor(() => {
    expect(screen.getByText(/project 1/i)).toBeInTheDocument();
  });
});
```

## Monitoring & Debugging

### Logging

```javascript
// Enable detailed logging
console.log(`[AI Assistant] Calling tool: ${tool.name}`, params);
console.log(`[AI Assistant] Tool result:`, result);
```

### Metrics to Track

- **Latency**: Time from question to answer
- **Token Usage**: Total tokens per conversation
- **Tool Calls**: Number of function calls per query
- **Error Rate**: Failed queries / total queries
- **User Satisfaction**: Thumbs up/down (future feature)

### Debug Mode

```javascript
// Enable debug mode for detailed step tracking
const response = await aiAssistant.ask(question, {
  debug: true, // Future feature
  logSteps: true,
});
```

## Future Enhancements

### Phase 1 (Next Release)

- [ ] Streaming responses word-by-word
- [ ] Thumb up/down for responses
- [ ] Export conversation to markdown
- [ ] Voice input support

### Phase 2

- [ ] Multi-turn conversations with context
- [ ] Custom tool creation by users
- [ ] Integration with external APIs
- [ ] Batch processing for multiple questions

### Phase 3

- [ ] Autonomous agents for complex tasks
- [ ] Code generation and suggestions
- [ ] Automated testing generation
- [ ] Documentation generation

## Troubleshooting

### Issue: AI not responding

**Solution:**

1. Check browser console for errors
2. Verify AI provider is configured
3. Test API key validity
4. Check network connectivity

### Issue: Wrong or incomplete answers

**Solution:**

1. Rephrase question more specifically
2. Provide more context
3. Break complex questions into parts
4. Check if required data exists in database

### Issue: GitHub tools not working

**Solution:**

1. Verify GitHub token is set
2. Check repository URL and branch
3. Ensure PAT has correct permissions
4. Verify file paths are correct

## API Reference

See [AI-ASSISTANT-GUIDE.md](./AI-ASSISTANT-GUIDE.md) for user-facing documentation.

For developer API documentation:

### AIAssistantService

```typescript
class AIAssistantService {
  async ask(
    question: string,
    options?: {
      streamCallback?: (data: any) => void;
      includeHistory?: boolean;
      maxIterations?: number;
    },
  ): Promise<{
    answer: string;
    steps: Array<Step>;
    functionCalls: Array<FunctionCall>;
    iterations: number;
  }>;

  clearHistory(): void;
  getHistory(): Array<Message>;
}
```

### Tool Definition

```typescript
interface Tool {
  name: string;
  description: string;
  parameters: {
    type: "object";
    properties: Record<
      string,
      {
        type: string;
        description: string;
      }
    >;
    required: string[];
  };
  handler: (params: any) => Promise<any>;
}
```

## Contributing

When adding new tools:

1. Define tool in `AIAssistantService.defineTools()`
2. Implement handler method
3. Update documentation
4. Add unit tests
5. Test with various queries

Example:

```javascript
{
  name: "my_new_tool",
  description: "What this tool does",
  parameters: {
    type: "object",
    properties: {
      param1: { type: "string", description: "..." }
    },
    required: ["param1"]
  },
  handler: async (params) => this.myNewToolHandler(params)
}
```

---

**Version:** 1.0.0  
**Last Updated:** 2025-11-09  
**Maintainer:** PCM Team
