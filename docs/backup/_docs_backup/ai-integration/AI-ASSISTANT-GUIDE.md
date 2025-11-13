# AI Assistant Guide

## Tổng Quan

AI Assistant là một trợ lý thông minh được tích hợp vào PCM WebApp, cho phép bạn:

- **Phân tích dự án và màn hình** - Hiểu cấu trúc, quan hệ, và luồng điều hướng
- **Truy vấn cơ sở dữ liệu** - Tìm kiếm projects, screens, events, và knowledge base
- **Phân tích source code** - Đọc và phân tích mã nguồn từ GitHub repositories
- **Giải quyết vấn đề** - Tìm solutions từ knowledge base và đưa ra recommendations

## Cách Hoạt Động

AI Assistant sử dụng **Function Calling** pattern để:

1. **Hiểu câu hỏi** của bạn
2. **Lập kế hoạch** (planning) các bước cần thực hiện
3. **Gọi các tools/functions** để thu thập thông tin
4. **Phân tích và tổng hợp** kết quả
5. **Trả lời** một cách khoa học, từng bước

### Kiến Trúc

```
┌─────────────────┐
│  User Question  │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│   AI Provider (LLM)     │
│  - Claude               │
│  - OpenAI GPT-4         │
│  - Gemini               │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│          Function Calling               │
│  ┌─────────────────────────────────┐   │
│  │  Database Tools                 │   │
│  │  - search_projects              │   │
│  │  - get_project_details          │   │
│  │  - search_screens               │   │
│  │  - get_screen_details           │   │
│  │  - search_knowledge_base        │   │
│  │  - find_related_screens         │   │
│  │  - get_screen_flow              │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │  GitHub Tools                   │   │
│  │  - get_file_content             │   │
│  │  - list_repository_files        │   │
│  │  - analyze_source_files         │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────┐
│   Structured Answer     │
│   with Steps           │
└─────────────────────────┘
```

## Available Tools

### Database Tools

#### 1. `search_projects`

Tìm kiếm projects theo tên hoặc mô tả.

**Parameters:**

- `query` (string, required): Từ khóa tìm kiếm
- `subsystemId` (number, optional): Lọc theo subsystem

**Example:**

```
"Find all projects related to authentication"
```

#### 2. `get_project_details`

Lấy thông tin chi tiết về một project.

**Parameters:**

- `projectId` (number, required): ID của project

**Example:**

```
"Show me details of project 1"
```

#### 3. `search_screens`

Tìm kiếm screens theo tên hoặc mô tả.

**Parameters:**

- `query` (string, required): Từ khóa tìm kiếm
- `projectId` (number, optional): Lọc theo project

**Example:**

```
"Find all login screens"
```

#### 4. `get_screen_details`

Lấy thông tin chi tiết về một screen.

**Parameters:**

- `screenId` (number, required): ID của screen

**Example:**

```
"Show me details of screen 5"
```

#### 5. `search_knowledge_base`

Tìm kiếm trong knowledge base.

**Parameters:**

- `query` (string, required): Từ khóa tìm kiếm
- `category` (string, optional): Lọc theo category (java, database, frontend, etc.)
- `type` (string, optional): Lọc theo type (bug, feature, optimization, etc.)

**Example:**

```
"Find solutions for database connection errors"
```

#### 6. `find_related_screens`

Tìm các màn hình liên quan thông qua navigation.

**Parameters:**

- `screenId` (number, required): ID của screen
- `depth` (number, optional): Độ sâu tìm kiếm (default: 2)

**Example:**

```
"What screens are related to screen 3?"
```

#### 7. `get_screen_flow`

Lấy luồng điều hướng từ một màn hình.

**Parameters:**

- `screenId` (number, required): ID của screen

**Example:**

```
"Show me the user flow from screen 1"
```

### GitHub Tools

#### 1. `get_file_content`

Lấy nội dung của một file từ GitHub.

**Parameters:**

- `projectId` (number, required): ID của project (để lấy repository info)
- `filePath` (string, required): Đường dẫn file

**Example:**

```
"Show me the content of src/components/Login.tsx from project 1"
```

#### 2. `list_repository_files`

Liệt kê files trong một thư mục của repository.

**Parameters:**

- `projectId` (number, required): ID của project
- `path` (string, optional): Đường dẫn thư mục (default: root)

**Example:**

```
"List all files in the src directory of project 2"
```

#### 3. `analyze_source_files`

Phân tích tất cả source files liên kết với một screen.

**Parameters:**

- `screenId` (number, required): ID của screen
- `fileType` (string, optional): Lọc theo loại (frontend, backend, script, config, test, docs)

**Example:**

```
"Analyze the frontend source code for screen 5"
```

## Cách Sử Dụng

### 1. Truy vấn cơ bản

**Câu hỏi:**

```
What projects do I have?
```

**AI Assistant sẽ:**

1. Gọi `search_projects` với query rỗng để lấy tất cả projects
2. Tổng hợp và hiển thị danh sách

---

**Câu hỏi:**

```
Show me the screens in project 1
```

**AI Assistant sẽ:**

1. Gọi `get_project_details` với projectId = 1
2. Hiển thị thông tin project và danh sách screens

### 2. Phân tích phức tạp

**Câu hỏi:**

```
Analyze the login flow in my application
```

**AI Assistant sẽ:**

1. Gọi `search_screens` với query = "login"
2. Tìm thấy Login screen
3. Gọi `get_screen_flow` để lấy navigation flow
4. Gọi `find_related_screens` để tìm screens liên quan
5. Tổng hợp và giải thích luồng đăng nhập

---

**Câu hỏi:**

```
Find database connection issues and their solutions
```

**AI Assistant sẽ:**

1. Gọi `search_knowledge_base` với query = "database connection", category = "database"
2. Tìm thấy các KB items liên quan
3. Tổng hợp các vấn đề phổ biến và solutions

### 3. Source Code Analysis

**Câu hỏi:**

```
Analyze the implementation of screen 5
```

**AI Assistant sẽ:**

1. Gọi `get_screen_details` với screenId = 5
2. Gọi `analyze_source_files` với screenId = 5
3. Đọc nội dung các files (frontend, backend, etc.)
4. Phân tích và giải thích implementation

---

**Câu hỏi:**

```
Compare the authentication implementation in project 1 and project 2
```

**AI Assistant sẽ:**

1. Tìm screens liên quan đến authentication trong cả 2 projects
2. Phân tích source files của mỗi screen
3. So sánh và highlight differences

### 4. Câu hỏi phức hợp

**Câu hỏi:**

```
I'm getting a database error on the checkout screen. Can you help me find:
1. The screen details
2. Related source code
3. Any similar issues in the knowledge base
4. The navigation flow to understand the context
```

**AI Assistant sẽ:**

1. Gọi `search_screens` để tìm checkout screen
2. Gọi `get_screen_details` và `analyze_source_files`
3. Gọi `search_knowledge_base` với query = "database error"
4. Gọi `get_screen_flow` để hiểu context
5. Tổng hợp tất cả thông tin và đưa ra recommendations

## Best Practices

### ✅ DO

1. **Câu hỏi rõ ràng, cụ thể**

   ```
   ✅ "Analyze the login implementation in project 1"
   ❌ "Tell me about login"
   ```

2. **Cung cấp context khi cần**

   ```
   ✅ "Find database errors related to Oracle in the knowledge base"
   ❌ "Find errors"
   ```

3. **Yêu cầu phân tích từng bước**

   ```
   ✅ "Explain step-by-step how the checkout flow works"
   ```

4. **Tham khảo specific items khi biết ID**
   ```
   ✅ "What's the relationship between screen 3 and screen 5?"
   ```

### ❌ DON'T

1. **Câu hỏi quá chung chung**

   ```
   ❌ "Tell me everything"
   ❌ "What can you do?"
   ```

2. **Hỏi về thông tin không có trong hệ thống**

   ```
   ❌ "What's the weather today?"
   ❌ "Write me a new feature"
   ```

3. **Yêu cầu thay đổi code/database**
   ```
   ❌ "Delete project 1"
   ❌ "Update screen 5's description to XYZ"
   ```

## Example Questions

### Project Management

- "What projects do I have?"
- "Show me all projects in subsystem 2"
- "Find projects related to e-commerce"
- "What are the details of project 3?"

### Screen Analysis

- "List all screens in project 1"
- "Find all login screens"
- "Show me screen 5's events and navigation"
- "What screens are related to the checkout screen?"
- "Explain the user flow starting from screen 1"

### Source Code

- "Analyze the frontend code for screen 3"
- "Show me the content of src/Login.tsx from project 1"
- "Compare the authentication implementation across projects"
- "List all files in the components directory"

### Knowledge Base

- "Find solutions for database connection errors"
- "Search for Java-related bugs"
- "Show me all optimization tips"
- "Find articles about React performance"

### Complex Queries

- "I need to understand the complete checkout flow. Show me:
    - All screens involved
    - Their navigation paths
    - Related source code
    - Any known issues from KB"

- "Compare how authentication is implemented in project 1 vs project 2"

- "Find all screens that navigate to the home screen and analyze their purpose"

## Limitations

1. **Read-only** - AI Assistant chỉ đọc và phân tích, không thể modify data
2. **Context Window** - Mỗi câu hỏi có giới hạn về số lượng code/data được analyze
3. **GitHub Authentication** - Cần cấu hình GitHub token trong Settings
4. **AI Provider** - Cần có active AI provider (Claude, OpenAI, etc.)

## Configuration

### 1. Configure AI Provider

Vào **Settings** → **AI Provider** tab:

- Chọn provider (Claude, OpenAI, Gemini, etc.)
- Nhập API Key
- Test connection

### 2. Configure GitHub (Optional)

Vào **Settings** → **GitHub Integration** tab:

- Connect GitHub account
- Provide Personal Access Token
- Link projects to repositories

### 3. Populate Knowledge Base

Vào **Knowledge Base** page:

- Tạo categories (Java, Database, Frontend, etc.)
- Thêm KB items với problems và solutions
- Tag items để dễ search

## Troubleshooting

### AI Assistant không phản hồi

1. Check AI Provider configuration
2. Verify API key is valid
3. Check network connection
4. View browser console for errors

### Không load được source code

1. Verify GitHub connection
2. Check repository URL và branch
3. Ensure PAT has correct permissions
4. Verify file paths are correct

### Kết quả không chính xác

1. Refine your question - be more specific
2. Provide more context
3. Break complex questions into smaller parts
4. Check if data exists in database

## Tips & Tricks

### 1. Chain Multiple Questions

Instead of:

```
"What's project 1?"
(wait for response)
"Show me its screens"
(wait for response)
"Analyze screen 3"
```

Ask:

```
"Tell me about project 1, its screens, and analyze screen 3 in detail"
```

### 2. Use Context from Previous Messages

AI Assistant remembers conversation history:

```
User: "Show me project 1's details"
AI: [responds with project 1 info]
User: "Now analyze its login screen"  ← AI knows "its" = project 1
```

### 3. Request Specific Format

```
"List all screens in project 1 in a table format"
"Summarize the checkout flow in 3 bullet points"
"Give me a step-by-step guide to the registration process"
```

### 4. Combine Database + Code Analysis

```
"For the login screen (screen 5):
- Show me the events
- Analyze the frontend code
- Find related KB articles about login issues"
```

## Advanced Use Cases

### 1. Impact Analysis

```
"If I change screen 3, what other screens will be affected?"
```

AI will:

- Find screens that navigate to/from screen 3
- Analyze navigation paths
- Identify dependent screens

### 2. Documentation Generation

```
"Generate documentation for the entire checkout flow"
```

AI will:

- Map all screens in checkout process
- Analyze events and navigation
- Review source code
- Compile comprehensive documentation

### 3. Debugging Support

```
"I'm getting a 500 error on screen 7 when clicking 'Submit'.
Can you help me debug by:
- Showing the event configuration
- Analyzing the backend code
- Finding similar errors in KB"
```

### 4. Onboarding New Developers

```
"Explain to a new developer how our authentication system works,
including all screens, flows, and key source files"
```

## FAQ

**Q: Có thể yêu cầu AI Assistant tạo code mới không?**
A: Có, AI có thể suggest code snippets, nhưng không thể tự động add vào project.

**Q: AI Assistant có thể modify database không?**
A: Không, AI chỉ có read-only access.

**Q: Conversation history được lưu ở đâu?**
A: Lưu tạm trong memory, mất khi refresh page. Có thể clear bằng "Clear" button.

**Q: Hỗ trợ ngôn ngữ nào?**
A: AI Assistant hiểu cả tiếng Việt và English.

**Q: Chi phí sử dụng như thế nào?**
A: Phụ thuộc vào AI provider bạn chọn. Mỗi API call đến provider sẽ tính phí theo pricing của họ.

## Support

Nếu gặp vấn đề:

1. Check browser console for errors
2. Review AI-ASSISTANT-GUIDE.md
3. Contact development team
4. Report issues on GitHub

---

**Version:** 1.0.0  
**Last Updated:** 2025-11-09  
**Maintainer:** PCM Team
