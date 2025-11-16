# Screen Structure & Navigation - PCM Desktop

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Mục đích:** Mô tả cấu trúc menu và màn hình của ứng dụng

---

## 1. Application Layout

### 1.1 Main Window Structure

```
┌─────────────────────────────────────────────────────────────┐
│  PCM Desktop - [Project Name]                    [_ □ ✕]   │
├─────────────────────────────────────────────────────────────┤
│  Main Menu Bar                                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┬──────────────────────────────────────┐   │
│  │             │                                       │   │
│  │  Sidebar    │        Content Area                   │   │
│  │  Navigation │        (Dynamic based on selection)   │   │
│  │             │                                       │   │
│  │  [Icons +   │                                       │   │
│  │   Labels]   │                                       │   │
│  │             │                                       │   │
│  └─────────────┴──────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Status Bar: [Status] [Progress] [User] [Time]             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Menu Structure

### 2.1 Main Menu Bar

#### **File Menu**
```
File
├─ New
│  ├─ New System          Ctrl+Shift+S
│  ├─ New Subsystem       Ctrl+Shift+U
│  ├─ New Project         Ctrl+Shift+P
│  └─ New Conversation    Ctrl+N
├─ Import
│  ├─ Import CHM File...
│  ├─ Import Source Repository...
│  └─ Import Knowledge Base...
├─ Export
│  ├─ Export Request History...
│  ├─ Export Test Plan...
│  └─ Export Analytics...
├─ ─────────────
├─ Settings               Ctrl+,
└─ Exit                   Alt+F4
```

#### **Edit Menu**
```
Edit
├─ Undo                   Ctrl+Z
├─ Redo                   Ctrl+Y
├─ ─────────────
├─ Cut                    Ctrl+X
├─ Copy                   Ctrl+C
├─ Paste                  Ctrl+V
├─ ─────────────
├─ Find                   Ctrl+F
├─ Find in Files          Ctrl+Shift+F
└─ Replace                Ctrl+H
```

#### **View Menu**
```
View
├─ Navigation Sidebar     [ √ ]
├─ Status Bar             [ √ ]
├─ Context Panel          [ √ ]
├─ ─────────────
├─ Zoom In                Ctrl++
├─ Zoom Out               Ctrl+-
├─ Reset Zoom             Ctrl+0
├─ ─────────────
├─ Theme
│  ├─ ( ) Light
│  └─ (•) Dark
└─ Full Screen            F11
```

#### **Navigate Menu**
```
Navigate
├─ Go to AI Assistant     Ctrl+1
├─ Go to System Manager   Ctrl+2
├─ Go to Source Manager   Ctrl+3
├─ Go to AST Explorer     Ctrl+4
├─ Go to Search Console   Ctrl+5
├─ Go to Knowledge Center Ctrl+6
├─ Go to Request History  Ctrl+7
├─ Go to Settings         Ctrl+8
├─ ─────────────
├─ Back                   Alt+Left
└─ Forward                Alt+Right
```

#### **Tools Menu**
```
Tools
├─ Trigger Source Scan
├─ Rebuild AST
├─ Reindex Search Corpus
├─ Clear Embedding Cache
├─ ─────────────
├─ Run Code Review
├─ Generate Test Plan
├─ ─────────────
├─ Database Manager
│  ├─ View Database Objects
│  ├─ Run SQL Query
│  └─ Backup Database
└─ Developer Console      Ctrl+Shift+D
```

#### **Help Menu**
```
Help
├─ Documentation          F1
├─ Quick Start Guide
├─ Keyboard Shortcuts
├─ ─────────────
├─ Check for Updates
├─ View Logs
├─ Report Issue
├─ ─────────────
└─ About PCM Desktop
```

---

### 2.2 Sidebar Navigation

**8 Main Screens (Icons + Labels):**

```
┌─────────────────┐
│ 💬 AI Assistant │  → Main chat interface (Default home)
├─────────────────┤
│ 🏢 Systems      │  → System hierarchy management
├─────────────────┤
│ 📁 Sources      │  → Source repository manager
├─────────────────┤
│ 🌳 AST Explorer │  → AST & dependency visualization
├─────────────────┤
│ 🔍 Search       │  → Semantic search console
├─────────────────┤
│ 📚 Knowledge    │  → CHM & docs import center
├─────────────────┤
│ 📊 History      │  → Request tracking & history
├─────────────────┤
│ ⚙️  Settings    │  → App settings & analytics
└─────────────────┘
```

---

## 3. Screen Details

### 3.1 AI Assistant Page 💬

**Route:** `/ai-assistant`  
**Shortcut:** `Ctrl+1`  
**Default:** Yes (home screen)

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  AI Assistant                            [New Chat] [⋮]    │
├──────────────┬─────────────────────────────────────────────┤
│              │                                             │
│ Conversations│  Chat Area                                 │
│ Sidebar      │  ┌─────────────────────────────────────┐   │
│              │  │ User: How does login work?         │   │
│ [New Chat]   │  │                                     │   │
│              │  │ Assistant: Based on the code...    │   │
│ [Search...]  │  │ [Source 1] AuthService.java:45-72  │   │
│              │  │ ```java                            │   │
│ ┌──────────┐ │  │ public void login(User user) {     │   │
│ │ Conv 1   │ │  │   ...                              │   │
│ │ Today    │ │  │ }                                  │   │
│ │ 150 tok  │ │  │ ```                                │   │
│ └──────────┘ │  │                                     │   │
│              │  │ [👍 👎]                              │   │
│ ┌──────────┐ │  └─────────────────────────────────────┘   │
│ │ Conv 2   │ │                                             │
│ │ Yest.    │ │  ┌─────────────────────────────────────┐   │
│ │ 320 tok  │ │  │ Type your question...               │   │
│ └──────────┘ │  │                                     │   │
│              │  │ [📎 Attach] [🎤 Voice]      [Send ➤] │   │
│              │  └─────────────────────────────────────┘   │
├──────────────┴─────────────────────────────────────────────┤
│ Context: Project [Demo ▾] | Retrieved 5 sources            │
└────────────────────────────────────────────────────────────┘
```

**Components:**
- **Conversation List** (Left sidebar)
  - New chat button
  - Search conversations
  - Conversation items (title, date, token count)
- **Chat Area** (Center)
  - Message list (scrollable)
  - User message bubbles (right-aligned)
  - Assistant message bubbles (left-aligned)
  - Code snippets (syntax highlighted)
  - Citations (clickable links to sources)
  - Feedback buttons (👍 👎)
- **Input Area** (Bottom)
  - Multi-line text input
  - Attach file button
  - Voice input button (future)
  - Send button
- **Context Panel** (Collapsible, right side)
  - Project selector dropdown
  - Retrieved sources list
  - Source preview

**Modals:**
- `NewConversationDialog` - Choose project scope
- `AttachmentDialog` - Upload files, select existing code
- `SourcePreviewDialog` - View full source code
- `FeedbackDialog` - Detailed feedback form

---

### 3.2 System Manager Page 🏢

**Route:** `/systems`  
**Shortcut:** `Ctrl+2`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  System Manager                     [+ Add] [Refresh]      │
├─────────────────────┬──────────────────────────────────────┤
│                     │                                      │
│ Hierarchy Tree      │  Detail Panel                        │
│                     │                                      │
│ ⊞ ERP System        │  ┌────────────────────────────────┐ │
│   ⊞ HR Module       │  │ System: ERP System             │ │
│     • Payroll       │  │ Code: ERP                      │ │
│     • Benefits      │  │ Name: [Enterprise Resource...] │ │
│   ⊟ Finance         │  │ Description: [Text area...]    │ │
│     • Accounting    │  │ Owner: [John Doe          ▾]  │ │
│     • Reporting     │  │                                │ │
│       ⚡ Daily Sync  │  │ Created: 2024-01-15           │ │
│ ⊞ CRM System        │  │ Updated: 2024-11-15           │ │
│                     │  │                                │ │
│                     │  │ [Save] [Cancel] [Delete]       │ │
│                     │  └────────────────────────────────┘ │
│                     │                                      │
│ Legend:             │  Statistics:                         │
│ ⊞ System            │  - 2 Systems                        │
│ ⊟ Subsystem         │  - 5 Subsystems                     │
│ • Project           │  - 12 Projects                      │
│ ⚡ Batch             │  - 3 Batches                        │
└─────────────────────┴──────────────────────────────────────┘
```

**Components:**
- **Hierarchy Tree** (Left)
  - Expandable tree view
  - Icons for each type (System, Subsystem, Project, Batch)
  - Context menu (right-click)
  - Drag-and-drop reorder (future)
- **Detail Panel** (Right)
  - Form fields (dynamic based on selection)
  - Save/Cancel/Delete buttons
  - Statistics box
- **Toolbar** (Top)
  - Add dropdown (System/Subsystem/Project/Batch)
  - Refresh button
  - Search/filter box

**Modals:**
- `AddSystemDialog` - Create new System
- `AddSubsystemDialog` - Create new Subsystem (select parent)
- `AddProjectDialog` - Create new Project (select parent)
- `AddBatchDialog` - Create new Batch (select parent)
- `DeleteConfirmDialog` - Confirm delete with cascade warning

**Forms:**
- `SystemForm`
  - code: TextField (required, unique)
  - name: TextField (required)
  - description: TextArea
  - owner: TextField
  
- `SubsystemForm`
  - system_id: ComboBox (required)
  - code: TextField (required, unique per system)
  - name: TextField (required)
  - description: TextArea
  - tech_stack: TextField
  - status: ComboBox (active, deprecated, archived)
  
- `ProjectForm`
  - subsystem_id: ComboBox (required)
  - code: TextField (required)
  - name: TextField (required)
  - description: TextArea
  - lead: TextField
  - status: ComboBox (draft, active, completed, cancelled)
  - start_date: DatePicker
  - end_date: DatePicker
  
- `BatchForm`
  - subsystem_id: ComboBox (required)
  - code: TextField (required)
  - name: TextField (required)
  - description: TextArea
  - schedule_cron: TextField (with validator)
  - status: ComboBox (idle, running, failed, disabled)

---

### 3.3 Source Manager Page 📁

**Route:** `/sources`  
**Shortcut:** `Ctrl+3`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  Source Manager                [+ Add Source] [Scan All]   │
├────────────────────────────────────────────────────────────┤
│  Project: [All Projects      ▾]  Status: [All ▾]          │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Project    │ Path            │ VCS  │ Status  │ Last  │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ Payroll    │ /code/payroll   │ git  │ ✓ Done  │ 1h ago│ │
│  │ Benefits   │ /code/benefits  │ git  │ ⏳ Scan  │ -     │ │
│  │ Accounting │ /code/accounting│ svn  │ ✗ Failed│ 2d ago│ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Selected: /code/payroll                                  │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Details:                                              │ │
│  │ - Language: Java                                      │ │
│  │ - Files: 1,234                                        │ │
│  │ - Lines: 45,678                                       │ │
│  │ - AST Nodes: 12,345                                   │ │
│  │ - Last Commit: abc123def                              │ │
│  │ - Branch: main                                        │ │
│  │                                                       │ │
│  │ [Scan Now] [View Files] [View AST] [Remove]          │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

**Components:**
- **Filter Bar** (Top)
  - Project dropdown
  - Status filter
  - Search box
- **Sources Table**
  - Project name
  - Root path
  - VCS type
  - Scan status (with icon)
  - Last scanned timestamp
  - Action buttons (Scan, View, Remove)
- **Detail Panel** (Bottom)
  - Source statistics
  - Action buttons

**Modals:**
- `AddSourceDialog`
  - Project selector (dropdown)
  - Directory picker (browse button)
  - VCS type selector (git/svn/none)
  - Branch name (if git)
  - Scan immediately? (checkbox)
  
- `ScanProgressDialog`
  - Progress bar
  - Current file being processed
  - Statistics (files scanned, nodes created)
  - Cancel button
  
- `FileListDialog`
  - Tree view of all source files
  - Language filter
  - Search box
  - Jump to file action

---

### 3.4 AST Explorer Page 🌳

**Route:** `/ast-explorer`  
**Shortcut:** `Ctrl+4`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  AST Explorer                        [Search: symbol...▾]  │
├──────────────┬─────────────────────────────────────────────┤
│              │                                             │
│ AST Tree     │  Code View                                 │
│              │  ┌─────────────────────────────────────┐   │
│ ⊞ AuthSvc.j  │  │  45 | public void login(User u) {  │   │
│   ⊟ class    │  │  46 |   if (u == null) {          │   │
│     • field  │  │  47 |     throw new IAE(...);     │   │
│       token  │  │  48 |   }                         │   │
│     ⊟ method │  │  49 |   validate(u);              │   │
│       login  │  │  50 |   String token = ...;       │   │
│       logout │  │  51 |   sessionRepo.save(...);    │   │
│     ⊞ method │  │  52 | }                           │   │
│       valid  │  └─────────────────────────────────────┘   │
│              │                                             │
│              │  ┌─────────────────┬───────────────────┐   │
│              │  │ Properties      │ Relationships     │   │
│              │  ├─────────────────┴───────────────────┤   │
│              │  │ Type: method                        │   │
│              │  │ Name: login                         │   │
│              │  │ FQ Name: ...AuthService.login       │   │
│              │  │ Modifiers: public                   │   │
│              │  │ Return: void                        │   │
│              │  │ Params: User user                   │   │
│              │  │ Complexity: 5                       │   │
│              │  │                                     │   │
│              │  │ Calls: validate, generateToken     │   │
│              │  │ Called by: handleLogin, ...        │   │
│              │  └─────────────────────────────────────┘   │
└──────────────┴─────────────────────────────────────────────┘
```

**Components:**
- **Search Bar** (Top)
  - Symbol search
  - Type filter (class, method, field, etc.)
  - Scope filter (project selector)
- **AST Tree** (Left)
  - Hierarchical tree
  - Icons for node types
  - Expandable/collapsible
- **Code View** (Top right)
  - Syntax highlighted source
  - Line numbers
  - Current node highlighted
- **Properties & Relationships** (Bottom right)
  - Tabs: Properties, Relationships, Dependencies
  - Node metadata
  - Call graph (clickable links)

**Modals:**
- `SearchSymbolDialog`
  - Advanced search options
  - Regex support
  - Result list
  
- `CallGraphDialog`
  - Visual graph (D3.js or similar)
  - Zoom/pan controls
  - Export as image
  
- `ImpactAnalysisDialog`
  - Select node
  - Show impacted files
  - Show impacted tests
  - Export report

---

### 3.5 Search Console Page 🔍

**Route:** `/search`  
**Shortcut:** `Ctrl+5`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  Search Console                                            │
├────────────────────────────────────────────────────────────┤
│  [authentication login password          ] [🔍 Search]     │
│  ☐ Hybrid (Vector + Lexical)  Project: [All ▾]            │
│  ☑ Code  ☑ Docs  ☐ CHM  ☐ Responses                       │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Results: 23 found                    [Export] [Send to AI]│
│  ┌──────────────────────────────────────────────────────┐ │
│  │ ⭐ 0.92 | CODE | AuthService.java:45-72              │ │
│  │ ... public void login(User user) { validate(user);  │ │
│  │     String token = tokenService.generate...         │ │
│  │                                            [View]    │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ ⭐ 0.87 | DOC | Authentication Guide                 │ │
│  │ ... The login process requires valid credentials... │ │
│  │                                            [View]    │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ ⭐ 0.81 | CODE | TokenService.java:102-125           │ │
│  │ ... public String generateToken(User user) {        │ │
│  │                                            [View]    │ │
│  └──────────────────────────────────────────────────────┘ │
│  [← Prev]  Page 1 of 3  [Next →]                          │
└────────────────────────────────────────────────────────────┘
```

**Components:**
- **Search Bar** (Top)
  - Query input (with autocomplete)
  - Search button
  - Mode toggle (Hybrid on/off)
- **Filters** (Below search)
  - Project selector
  - Source type checkboxes
  - Date range (advanced)
- **Results List**
  - Score/relevance
  - Source type badge
  - File path / document title
  - Preview snippet (highlighted)
  - View button
- **Actions Bar**
  - Export button
  - Send to AI button (pre-populate context)
  - Pagination

**Modals:**
- `AdvancedSearchDialog`
  - Boolean operators (AND/OR/NOT)
  - Phrase search
  - Field-specific search
  - Regular expressions
  
- `ResultPreviewDialog`
  - Full content view
  - Syntax highlighting (for code)
  - Copy button
  - Open in editor button
  
- `ExportDialog`
  - Format selection (CSV, JSON, Markdown)
  - Fields to include
  - Save location

---

### 3.6 Knowledge Center Page 📚

**Route:** `/knowledge`  
**Shortcut:** `Ctrl+6`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  Knowledge Center           [+ Import CHM] [+ New Article] │
├──────────────┬─────────────────────────────────────────────┤
│              │                                             │
│ CHM Imports  │  Import Details                            │
│              │  ┌─────────────────────────────────────┐   │
│ ☑ Show All   │  │ Import ID: 42                       │   │
│ ☐ Pending    │  │ CHM: api-docs.chm                   │   │
│ ☐ Complete   │  │ Project: Demo Project               │   │
│              │  │ Status: ✓ Complete                  │   │
│ ┌──────────┐ │  │ Imported: 2024-11-15 14:30         │   │
│ │ api-docs │ │  │                                     │   │
│ │ ✓ Done   │ │  │ Documents: 234                     │   │
│ │ 234 docs │ │  │ Assets: 45 (images, CSS)           │   │
│ └──────────┘ │  │                                     │   │
│              │  │ [View Docs] [Re-import] [Delete]    │   │
│ ┌──────────┐ │  └─────────────────────────────────────┘   │
│ │ user-man │ │                                             │
│ │ ⏳ Import │ │  TOC Browser                               │
│ │ 50% done │ │  ⊞ Getting Started                         │
│ └──────────┘ │    • Introduction                          │
│              │    • Installation                          │
│              │  ⊞ User Guide                              │
│              │    ⊟ Authentication                        │
│              │      • Login Process                       │
│              │      • Password Reset                      │
│              │  ⊞ API Reference                           │
│              │                                             │
│              │  [Preview Selected Document →]             │
└──────────────┴─────────────────────────────────────────────┘
```

**Components:**
- **Imports List** (Left)
  - Filter checkboxes
  - Import cards (name, status, doc count)
  - Progress indicator for pending
- **Import Details** (Top right)
  - Metadata
  - Statistics
  - Action buttons
- **TOC Browser** (Bottom right)
  - Hierarchical tree
  - Document preview

**Modals:**
- `ImportChmDialog`
  - File picker (*.chm)
  - Project selector
  - Notes field
  - Import button → shows progress
  
- `DocumentPreviewDialog`
  - WebView with rendered HTML
  - Navigation (prev/next)
  - Full-screen mode
  
- `NewArticleDialog`
  - Title field
  - Rich text editor (Markdown)
  - Tags field
  - Project selector
  - Save button

---

### 3.7 Request History Page 📊

**Route:** `/history`  
**Shortcut:** `Ctrl+7`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  Request History                          [Export] [Clear] │
├────────────────────────────────────────────────────────────┤
│  Date: [Last 30 days ▾]  Project: [All ▾]  Status: [All ▾]│
│  Search: [keyword...]                                      │
├────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────┐ │
│  │ ID  │ Title          │ Project  │ Status │ Rating   │ │
│  ├──────────────────────────────────────────────────────┤ │
│  │ 152 │ Login issue    │ Payroll  │ ✓ Done │ ⭐⭐⭐⭐⭐ │ │
│  │ 151 │ How to auth?   │ Demo     │ ✓ Done │ ⭐⭐⭐⭐  │ │
│  │ 150 │ Test strategy  │ Benefits │ ⏳ Proc │ -       │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  Selected Request #152:                                   │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ Title: Login issue with special characters           │ │
│  │ Description: Users with Korean names cannot login... │ │
│  │ Project: Payroll System                              │ │
│  │ Created: 2024-11-14 09:15                            │ │
│  │ Resolved: 2024-11-14 09:23                           │ │
│  │                                                       │ │
│  │ Response: [Click to expand...]                       │ │
│  │ Artifacts: 5 sources retrieved                       │ │
│  │ Feedback: ⭐⭐⭐⭐⭐ "Very helpful!"                     │ │
│  │                                                       │ │
│  │ [View Full] [Re-analyze] [Export PDF]                │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

**Components:**
- **Filter Bar** (Top)
  - Date range picker
  - Project filter
  - Status filter
  - Search box
- **Requests Table**
  - ID, Title, Project, Status, Rating
  - Sort by any column
  - Multi-select for batch operations
- **Detail Panel** (Bottom)
  - Full request details
  - Response preview
  - Artifacts list
  - Feedback
  - Action buttons

**Modals:**
- `RequestDetailDialog`
  - Full conversation view
  - Retrieved sources
  - Code snippets
  - Timeline
  
- `ExportDialog`
  - Format (PDF, Markdown, HTML)
  - Include sources? (checkbox)
  - Include code snippets? (checkbox)
  
- `ReanalyzeDialog`
  - Confirm re-analysis
  - Select different scope?
  - Keep old response? (checkbox)

---

### 3.8 Settings Page ⚙️

**Route:** `/settings`  
**Shortcut:** `Ctrl+8`

**Layout:**
```
┌────────────────────────────────────────────────────────────┐
│  Settings                                   [Save] [Cancel]│
├──────────────┬─────────────────────────────────────────────┤
│              │                                             │
│ ⚪ General   │  General Settings                           │
│ ⚫ AI Config │  ┌─────────────────────────────────────┐   │
│ ⚪ Search    │  │ LLM Provider: [OpenAI        ▾]     │   │
│ ⚪ Analytics │  │ API Key: [••••••••••••••]  [Test]  │   │
│              │  │ Model: [gpt-4o           ▾]        │   │
│              │  │ Temperature: [0.2] ◄──────────► 2.0│   │
│              │  │ Max Tokens: [2000]                 │   │
│              │  │ ☑ Enable streaming                 │   │
│              │  │                                     │   │
│              │  │ Embedding Model:                   │   │
│              │  │ [text-embedding-3-large  ▾]        │   │
│              │  │ Dimensions: [3072]                 │   │
│              │  │ ☑ Enable embedding cache           │   │
│              │  │                                     │   │
│              │  │ [Reset to Defaults]                │   │
│              │  └─────────────────────────────────────┘   │
│              │                                             │
│              │  Test Connection:                          │
│              │  ┌─────────────────────────────────────┐   │
│              │  │ Status: ✓ Connected                 │   │
│              │  │ Latency: 234ms                      │   │
│              │  │ Model: gpt-4o (available)           │   │
│              │  └─────────────────────────────────────┘   │
└──────────────┴─────────────────────────────────────────────┘
```

**Tabs:**

1. **General**
   - Theme (Light/Dark)
   - Language (English, Vietnamese, Korean, etc.)
   - Default project
   - Auto-save interval
   - Keyboard shortcuts

2. **AI Configuration**
   - LLM provider (OpenAI, Anthropic, Local)
   - API key
   - Model selection
   - Temperature slider
   - Max tokens
   - Streaming toggle
   - Embedding model
   - Cache settings

3. **Search**
   - Vector search top-k
   - Lexical search top-k
   - Fusion strategy (RRF, Weighted)
   - Qdrant URL
   - Index optimization schedule

4. **Analytics**
   - Request volume chart (last 30 days)
   - Average rating chart
   - Retrieval latency histogram
   - Popular topics word cloud
   - Export analytics button

**Modals:**
- `TestConnectionDialog`
  - Progress spinner
  - Connection status
  - Error messages (if fail)
  
- `ResetDefaultsDialog`
  - Confirm reset
  - Which sections? (checkboxes)
  
- `ExportAnalyticsDialog`
  - Date range
  - Metrics to include
  - Format (CSV, JSON)

---

## 4. Common UI Components

### 4.1 Shared Modals

**ConfirmDialog**
- Title
- Message
- Yes/No buttons
- Optional "Don't show again" checkbox

**ErrorDialog**
- Error title
- Error message
- Stack trace (collapsible, for developers)
- Copy error button
- OK button

**ProgressDialog**
- Task description
- Progress bar (determinate or indeterminate)
- Current operation label
- Cancel button (if cancellable)

**NotificationToast**
- Type (info, success, warning, error)
- Message
- Auto-dismiss (3-5 seconds)
- Action button (optional)

---

### 4.2 Shared Components

**ProjectSelector**
- ComboBox with hierarchy
- Format: "System > Subsystem > Project"
- Search/filter capability

**CodeSnippet**
- Syntax highlighted
- Line numbers
- Copy button
- Open in editor button

**FilePathBreadcrumb**
- Clickable path segments
- Copy full path button

**RatingWidget**
- 5 stars
- Click to rate
- Hover preview
- Display current rating

**TagInput**
- Comma-separated tags
- Autocomplete from existing tags
- Remove tag (× button)

---

## 5. Navigation Flow

### 5.1 Typical User Journeys

**Setup New Project:**
```
1. Systems Page → Add System
2. Systems Page → Add Subsystem
3. Systems Page → Add Project
4. Sources Page → Add Source Root
5. Wait for scan → View AST Explorer
```

**Ask Question:**
```
1. AI Assistant (default home)
2. Type question
3. Optional: Attach files
4. Send
5. View response with citations
6. Rate response
```

**Import Documentation:**
```
1. Knowledge Center → Import CHM
2. Select file → Choose project
3. Wait for import
4. Browse TOC → Preview docs
```

**Review Code:**
```
1. Tools → Run Code Review
2. View review comments
3. Click comment → Jump to code
4. Mark resolved
```

---

## 6. Screen State Management

### 6.1 Page States

Each screen can have multiple states:

- **Loading** - Show spinner, disable inputs
- **Empty** - No data, show empty state with action
- **Populated** - Show data normally
- **Error** - Show error message with retry action
- **Refreshing** - Update data, show subtle indicator

### 6.2 Form States

- **Pristine** - No changes, Save disabled
- **Dirty** - Has changes, Save enabled
- **Validating** - Check inputs
- **Submitting** - Disabled, show spinner
- **Success** - Show success message, reset form
- **Error** - Show validation errors

---

## 7. Accessibility & UX

### 7.1 Keyboard Navigation

- All screens accessible via shortcuts
- Tab navigation within forms
- Escape to close modals
- Enter to submit forms
- Arrow keys in lists/trees

### 7.2 Screen Reader Support

- Proper ARIA labels
- Semantic HTML
- Focus management
- Status announcements

### 7.3 Responsive Behavior

- Minimum window size: 1024x768
- Sidebar collapsible
- Panels resizable
- Mobile (future): Simplified layout

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Next:** See `03-screen-field-specifications.md` for detailed field specs

