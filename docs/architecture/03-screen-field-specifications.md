# Screen Field Specifications - PCM Desktop

**Phiên bản:** 1.0  
**Ngày tạo:** 2025-11-15  
**Mục đích:** Chi tiết từng field trong mỗi màn hình và modal

---

## 1. System Manager - Forms & Modals

### 1.1 System Form

**Dialog:** `AddSystemDialog` / `EditSystemDialog`

| Field         | Type      | Required | Validation                                       | Default      | Notes                   |
|---------------|-----------|----------|--------------------------------------------------|--------------|-------------------------|
| `code`        | TextField | ✅        | Pattern: `^[A-Z0-9_-]{2,50}$`<br>Unique globally | -            | Auto-uppercase          |
| `name`        | TextField | ✅        | Max length: 255<br>Non-empty                     | -            |                         |
| `description` | TextArea  | ❌        | Max length: 2000                                 | -            | Resizable, 5 rows       |
| `owner`       | TextField | ❌        | Max length: 255                                  | Current user | Autocomplete from users |

**Buttons:**

- `[Save]` - Enabled when form valid and dirty
- `[Cancel]` - Close without saving
- `[Delete]` - Only in edit mode, shows confirm dialog

**Validation Messages:**

- "Code is required"
- "Code must be 2-50 characters (A-Z, 0-9, _, -)"
- "Code already exists"
- "Name is required"
- "Name must not exceed 255 characters"

---

### 1.2 Subsystem Form

**Dialog:** `AddSubsystemDialog` / `EditSubsystemDialog`

| Field         | Type      | Required | Validation                                         | Default  | Notes                                     |
|---------------|-----------|----------|----------------------------------------------------|----------|-------------------------------------------|
| `system_id`   | ComboBox  | ✅        | Must select valid system                           | -        | Dropdown list of systems                  |
| `code`        | TextField | ✅        | Pattern: `^[A-Z0-9_-]{2,50}$`<br>Unique per system | -        | Auto-uppercase                            |
| `name`        | TextField | ✅        | Max length: 255                                    | -        |                                           |
| `description` | TextArea  | ❌        | Max length: 2000                                   | -        |                                           |
| `tech_stack`  | TextField | ❌        | Max length: 255                                    | -        | Autocomplete: Java, Python, Node.js, etc. |
| `status`      | ComboBox  | ✅        | Enum: active, deprecated, archived                 | `active` |                                           |

**ComboBox Options:**

- `status`:
    - `active` ✅ (default)
    - `deprecated` ⚠️
    - `archived` 📦

**Validation Messages:**

- "System is required"
- "Code is required and must be unique within this system"
- "Name is required"
- "Status must be: active, deprecated, or archived"

---

### 1.3 Project Form

**Dialog:** `AddProjectDialog` / `EditProjectDialog`

| Field          | Type       | Required | Validation                                            | Default      | Notes                            |
|----------------|------------|----------|-------------------------------------------------------|--------------|----------------------------------|
| `subsystem_id` | ComboBox   | ✅        | Must select valid subsystem                           | -            | Hierarchical: System > Subsystem |
| `code`         | TextField  | ✅        | Pattern: `^[A-Z0-9_-]{2,50}$`<br>Unique per subsystem | -            | Auto-uppercase                   |
| `name`         | TextField  | ✅        | Max length: 255                                       | -            |                                  |
| `description`  | TextArea   | ❌        | Max length: 2000                                      | -            |                                  |
| `lead`         | TextField  | ❌        | Max length: 255                                       | Current user | Autocomplete from users          |
| `status`       | ComboBox   | ✅        | Enum: draft, active, completed, cancelled             | `draft`      |                                  |
| `start_date`   | DatePicker | ❌        | Date format                                           | Today        |                                  |
| `end_date`     | DatePicker | ❌        | Must be >= start_date                                 | -            | Validation on blur               |

**ComboBox Options:**

- `status`:
    - `draft` 📝 (default)
    - `active` ▶️
    - `completed` ✅
    - `cancelled` ❌

**Validation Messages:**

- "Subsystem is required"
- "Code must be unique within this subsystem"
- "End date must be after start date"

---

### 1.4 Batch Form

**Dialog:** `AddBatchDialog` / `EditBatchDialog`

| Field           | Type      | Required | Validation                                            | Default | Notes                     |
|-----------------|-----------|----------|-------------------------------------------------------|---------|---------------------------|
| `subsystem_id`  | ComboBox  | ✅        | Must select valid subsystem                           | -       |                           |
| `code`          | TextField | ✅        | Pattern: `^[A-Z0-9_-]{2,50}$`<br>Unique per subsystem | -       | Auto-uppercase            |
| `name`          | TextField | ✅        | Max length: 255                                       | -       |                           |
| `description`   | TextArea  | ❌        | Max length: 2000                                      | -       |                           |
| `schedule_cron` | TextField | ❌        | Valid cron expression                                 | -       | Validator + helper button |
| `status`        | ComboBox  | ✅        | Enum: idle, running, failed, disabled                 | `idle`  |                           |

**Helper UI:**

- Cron expression builder button 🕐
- Opens `CronBuilderDialog` for visual construction
- Examples: "Daily at midnight", "Every Monday at 9 AM"

**ComboBox Options:**

- `status`:
    - `idle` ⏸️ (default)
    - `running` ▶️
    - `failed` ❌
    - `disabled` 🚫

**Validation Messages:**

- "Subsystem is required"
- "Invalid cron expression (use * * * * * format)"

---

### 1.5 Delete Confirmation Dialog

**Dialog:** `DeleteConfirmDialog`

**Content:**

```
┌────────────────────────────────────────┐
│ ⚠️  Delete [Entity Type]?              │
├────────────────────────────────────────┤
│                                        │
│ Are you sure you want to delete:      │
│ "[Entity Name]"?                       │
│                                        │
│ ⚠️  This will also delete:             │
│ • X subsystem(s)                       │
│ • Y project(s)                         │
│ • Z source(s)                          │
│                                        │
│ This action cannot be undone.          │
│                                        │
│ Type DELETE to confirm:                │
│ [                    ]                 │
│                                        │
│ [Delete] [Cancel]                      │
└────────────────────────────────────────┘
```

**Fields:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `confirm_text` | TextField | ✅ | Must type "DELETE" exactly |

**Delete button:** Disabled until "DELETE" typed correctly

---

## 2. Source Manager - Forms & Modals

### 2.1 Add Source Dialog

**Dialog:** `AddSourceDialog`

| Field              | Type               | Required | Validation                                       | Default     | Notes                                |
|--------------------|--------------------|----------|--------------------------------------------------|-------------|--------------------------------------|
| `project_id`       | ComboBox           | ✅        | Must select valid project                        | -           | Hierarchical dropdown                |
| `root_path`        | TextField + Button | ✅        | Must be valid directory path<br>Must be readable | -           | Browse button opens directory picker |
| `vcs_type`         | ComboBox           | ✅        | Enum: git, svn, none                             | `git`       |                                      |
| `default_branch`   | TextField          | ❌        | Only if vcs_type=git                             | `main`      | Auto-detect from repo                |
| `language`         | ComboBox           | ❌        | Enum: Java, Python, JS, etc.                     | Auto-detect | Override auto-detection              |
| `scan_immediately` | CheckBox           | ✅        | -                                                | `true`      | Start scan after save                |

**Directory Picker:**

- Native file dialog
- Show hidden files toggle
- Recent directories dropdown

**ComboBox Options:**

- `vcs_type`:
    - `git`
    - `svn`
    - `none` (plain directory)

- `language`:
    - Auto-detect (default)
    - Java
    - Python
    - JavaScript/TypeScript
    - C/C++
    - Go
    - Rust
    - PHP
    - Other

**Validation Messages:**

- "Project is required"
- "Root path is required"
- "Directory does not exist or is not readable"
- "This directory is already added to another project"
- "Default branch is required for Git repositories"

---

### 2.2 Scan Progress Dialog

**Dialog:** `ScanProgressDialog`

**Content:**

```
┌────────────────────────────────────────┐
│ Scanning Source Code...                │
├────────────────────────────────────────┤
│                                        │
│ [████████░░░░░░░░░░░] 45%             │
│                                        │
│ Current: parsing AuthService.java...  │
│                                        │
│ Statistics:                            │
│ • Files scanned: 234 / 520            │
│ • AST nodes created: 12,345           │
│ • Dependencies found: 456             │
│ • Elapsed time: 00:02:15              │
│                                        │
│ [Cancel]                               │
└────────────────────────────────────────┘
```

**Fields:**

- Progress bar (indeterminate or percentage)
- Current operation label (real-time update)
- Statistics (updated every second)
- Cancel button (stops scan, keeps partial results)

---

### 2.3 File List Dialog

**Dialog:** `FileListDialog`

**Content:**

```
┌────────────────────────────────────────────────┐
│ Source Files - /code/payroll          [✕]     │
├────────────────────────────────────────────────┤
│ Language: [All ▾]  Search: [filename...]      │
├────────────────────────────────────────────────┤
│ ⊞ src/                                        │
│   ⊞ main/                                     │
│     ⊟ java/                                   │
│       ⊟ com/example/                          │
│         • AuthService.java       ☑ 234 lines │
│         • TokenService.java      ☑ 156 lines │
│     ⊞ resources/                              │
│   ⊞ test/                                     │
│                                                │
│ Total: 520 files, 45,678 lines                │
│                                                │
│ [Export List] [Close]                          │
└────────────────────────────────────────────────┘
```

**Features:**

- Tree view with file hierarchy
- Language filter dropdown
- Search box (filename/path filter)
- Checkbox for parsed files
- Line count display
- Export to CSV button

---

## 3. Knowledge Center - Forms & Modals

### 3.1 Import CHM Dialog

**Dialog:** `ImportChmDialog`

| Field          | Type               | Required    | Validation                                       | Default         | Notes                           |
|----------------|--------------------|-------------|--------------------------------------------------|-----------------|---------------------------------|
| `chm_path`     | TextField + Button | ✅           | Must be *.chm file<br>Must exist and be readable | -               | Browse button for file picker   |
| `scope_type`   | RadioButton Group  | ✅           | One of: System, Subsystem, Project               | `Project`       | Determines which dropdown shows |
| `system_id`    | ComboBox           | Conditional | If scope_type=System                             | -               |                                 |
| `subsystem_id` | ComboBox           | Conditional | If scope_type=Subsystem                          | -               |                                 |
| `project_id`   | ComboBox           | Conditional | If scope_type=Project                            | Current project |                                 |
| `notes`        | TextArea           | ❌           | Max length: 1000                                 | -               |                                 |

**File Picker:**

- Filter: *.chm files only
- Recent CHM files dropdown
- Drag-and-drop support

**Layout:**

```
Choose CHM file:
[C:\docs\api-docs.chm        ] [Browse...]

Import scope:
( ) System    [Select System      ▾]
( ) Subsystem [Select Subsystem   ▾]
(•) Project   [Demo Project       ▾]

Notes (optional):
[                                     ]
[                                     ]

[Import] [Cancel]
```

**Import Process:**
After clicking Import:

1. Dialog transforms to progress view
2. Shows extraction progress
3. Shows parsing progress
4. On complete: Close or "View Documents"

---

### 3.2 Import Progress (embedded in dialog)

**Content:**

```
┌────────────────────────────────────────┐
│ Importing api-docs.chm...              │
├────────────────────────────────────────┤
│                                        │
│ Step 1/4: Extracting CHM ✓            │
│ Step 2/4: Parsing TOC ✓               │
│ Step 3/4: Parsing documents... 70%    │
│ [████████████░░░░░] 175/250           │
│ Step 4/4: Indexing (pending)          │
│                                        │
│ Current: processing install.html...   │
│                                        │
│ [Cancel Import]                        │
└────────────────────────────────────────┘
```

**On completion:**

```
┌────────────────────────────────────────┐
│ ✓ Import Complete!                     │
├────────────────────────────────────────┤
│                                        │
│ Successfully imported:                 │
│ • 250 documents                        │
│ • 45 images                            │
│ • 12 CSS files                         │
│                                        │
│ [View Documents] [Close]               │
└────────────────────────────────────────┘
```

---

### 3.3 New Knowledge Article Dialog

**Dialog:** `NewArticleDialog`

| Field        | Type             | Required | Validation                       | Default         | Notes                         |
|--------------|------------------|----------|----------------------------------|-----------------|-------------------------------|
| `title`      | TextField        | ✅        | Max length: 255<br>Non-empty     | -               |                               |
| `project_id` | ComboBox         | ❌        | -                                | Current project | Optional scope                |
| `content`    | Rich Text Editor | ✅        | Max length: 50,000<br>Non-empty  | -               | Markdown support              |
| `tags`       | TagInput         | ❌        | Max 10 tags<br>Each max 50 chars | -               | Comma-separated, autocomplete |

**Rich Text Editor Features:**

- Markdown formatting toolbar
- Preview tab
- Insert code block
- Insert link
- Insert image (upload or URL)
- Syntax highlighting

**Layout:**

```
┌──────────────────────────────────────────────────┐
│ New Knowledge Article                     [✕]   │
├──────────────────────────────────────────────────┤
│ Title: [How to authenticate users           ]   │
│ Project: [Demo Project                  ▾] (opt)│
│ Tags: [authentication, security, login      ]   │
├──────────────────────────────────────────────────┤
│ [B I U Code Link Image]   [Edit] [Preview]     │
├──────────────────────────────────────────────────┤
│                                                  │
│ # Authentication Guide                           │
│                                                  │
│ This guide explains...                           │
│                                                  │
│ ```java                                          │
│ public void login(User user) {                   │
│   ...                                            │
│ }                                                │
│ ```                                              │
│                                                  │
├──────────────────────────────────────────────────┤
│ [Save] [Cancel]                                  │
└──────────────────────────────────────────────────┘
```

---

### 3.4 Document Preview Dialog

**Dialog:** `DocumentPreviewDialog`

**Content:**

```
┌──────────────────────────────────────────────────┐
│ api-docs.chm > Getting Started > Install [✕]   │
├──────────────────────────────────────────────────┤
│ [◀ Prev] [▶ Next] [🔍+] [🔍-] [⛶ Full Screen]  │
├──────────────────────────────────────────────────┤
│                                                  │
│ [WebView with rendered HTML content]            │
│                                                  │
│ Installation Instructions                        │
│ ========================                         │
│                                                  │
│ 1. Download the installer...                     │
│ 2. Run setup.exe...                              │
│                                                  │
│ [Images and formatting preserved]                │
│                                                  │
├──────────────────────────────────────────────────┤
│ [Copy URL] [Export PDF] [Close]                 │
└──────────────────────────────────────────────────┘
```

**Navigation:**

- Previous/Next buttons (within TOC order)
- Breadcrumb path (clickable)
- Zoom controls
- Full-screen toggle (F11)

---

## 4. AI Assistant - Input & Feedback

### 4.1 Chat Input Area

**Component:** `ChatInputArea`

| Field         | Type      | Required | Validation                      | Default | Notes                   |
|---------------|-----------|----------|---------------------------------|---------|-------------------------|
| `message`     | TextArea  | ✅        | Max length: 10,000<br>Non-empty | -       | Multi-line, auto-resize |
| `attachments` | File List | ❌        | Max 10 files<br>Each max 10MB   | -       | Drag-and-drop support   |

**Attachment Types Supported:**

- Code files (*.java, *.py, *.js, etc.)
- Text files (*.txt, *.md)
- Images (*.png, *.jpg) - for screenshots
- Documents (*.pdf, *.docx) - future

**UI Elements:**

```
┌────────────────────────────────────────────────┐
│ Type your question...                          │
│                                                │
│                                                │
├────────────────────────────────────────────────┤
│ [📎 Attach] [🎤 Voice] [⚙️ Options]   [Send ➤] │
└────────────────────────────────────────────────┘

Attached files:
× AuthService.java (2.3 KB)
× Screenshot.png (450 KB)
```

**Options Menu (gear icon):**

- Project scope selector
- Temperature slider (0.0 - 2.0)
- Max tokens input
- Include conversation history? (checkbox)

**Keyboard Shortcuts:**

- `Enter` - Send (if not multi-line)
- `Shift+Enter` - New line
- `Ctrl+Enter` - Send (always)
- `Esc` - Clear input

---

### 4.2 Feedback Dialog

**Dialog:** `FeedbackDialog`

Triggered by clicking 👍 or 👎 after response

| Field        | Type           | Required | Validation       | Default              | Notes                      |
|--------------|----------------|----------|------------------|----------------------|----------------------------|
| `rating`     | Star Rating    | ✅        | 1-5 stars        | Thumb → Star mapping | 👎=1-2 stars, 👍=4-5 stars |
| `comment`    | TextArea       | ❌        | Max length: 2000 | -                    | Optional detailed feedback |
| `issue_type` | CheckBox Group | ❌        | -                | -                    | Multiple selection         |

**Issue Type Options:**

- ☐ Incorrect information
- ☐ Missing context
- ☐ Irrelevant sources
- ☐ Poor code examples
- ☐ Hard to understand
- ☐ Too slow

**Layout:**

```
┌────────────────────────────────────────┐
│ Feedback on Response                   │
├────────────────────────────────────────┤
│ How would you rate this response?      │
│ ☆ ☆ ☆ ☆ ☆  (click to rate)            │
│                                        │
│ What could be improved? (optional)     │
│ ☐ Incorrect information                │
│ ☐ Missing context                      │
│ ☐ Irrelevant sources                   │
│ ☐ Poor code examples                   │
│ ☐ Hard to understand                   │
│ ☐ Too slow                             │
│                                        │
│ Additional comments: (optional)        │
│ [                                  ]   │
│ [                                  ]   │
│                                        │
│ [Submit] [Skip]                        │
└────────────────────────────────────────┘
```

---

### 4.3 Attachment Dialog

**Dialog:** `AttachmentDialog`

**Tabs:** Upload File | Select from Project

**Tab 1: Upload File**

```
┌────────────────────────────────────────┐
│ Drag files here or click to browse    │
│                                        │
│     [📁 Click to Upload]               │
│                                        │
│ Supported: .java, .py, .js, .txt, .md │
│ Max size: 10 MB per file              │
│ Max files: 10                          │
└────────────────────────────────────────┘

Uploaded:
• AuthService.java (2.3 KB) ×
• config.yml (1.1 KB) ×
```

**Tab 2: Select from Project**

```
Project: [Demo Project        ▾]

☑ AuthService.java (src/main/...)
☐ TokenService.java
☐ README.md
☑ config.yml (src/main/resources/...)

[Add Selected] [Cancel]
```

---

## 5. Search Console - Advanced Search

### 5.1 Advanced Search Dialog

**Dialog:** `AdvancedSearchDialog`

| Field              | Type                | Required    | Validation             | Default   | Notes                    |
|--------------------|---------------------|-------------|------------------------|-----------|--------------------------|
| `query`            | TextField           | ✅           | Non-empty              | -         |                          |
| `search_mode`      | RadioButton Group   | ✅           | -                      | `keyword` |                          |
| `boolean_operator` | ComboBox            | Conditional | If mode=boolean        | `AND`     |                          |
| `field_specific`   | CheckBox + ComboBox | ❌           | -                      | -         | Search in specific field |
| `use_regex`        | CheckBox            | ❌           | Valid regex if checked | `false`   |                          |
| `case_sensitive`   | CheckBox            | ❌           | -                      | `false`   |                          |
| `whole_word`       | CheckBox            | ❌           | -                      | `false`   |                          |

**Search Modes:**

- ( ) Keyword search (default)
- ( ) Boolean search (AND, OR, NOT)
- ( ) Phrase search ("exact phrase")
- ( ) Regular expression

**Boolean Operators:**

- AND (default)
- OR
- NOT

**Field-Specific Search:**

- ☐ Search in specific field: [Field ▾]
    - File path
    - File name
    - Content only
    - Comments only
    - FQ name (for AST)

**Layout:**

```
┌────────────────────────────────────────────────┐
│ Advanced Search                         [✕]   │
├────────────────────────────────────────────────┤
│ Query: [authentication login              ]   │
│                                                │
│ Search Mode:                                   │
│ (•) Keyword search                             │
│ ( ) Boolean search   Operator: [AND ▾]        │
│ ( ) Phrase search                              │
│ ( ) Regular expression                         │
│                                                │
│ Options:                                       │
│ ☐ Case sensitive                               │
│ ☐ Whole word only                              │
│ ☑ Search in specific field: [Content ▾]       │
│                                                │
│ Examples:                                      │
│ • login AND password                           │
│ • "exact phrase search"                        │
│ • auth.* (regex)                               │
│                                                │
│ [Search] [Cancel]                              │
└────────────────────────────────────────────────┘
```

---

### 5.2 Result Preview Dialog

**Dialog:** `ResultPreviewDialog`

**Content:**

```
┌────────────────────────────────────────────────┐
│ AuthService.java:45-72                  [✕]   │
├────────────────────────────────────────────────┤
│ src/main/java/com/example/auth/AuthService... │
│                                                │
│ Score: 0.92 | Type: CODE | Language: Java     │
├────────────────────────────────────────────────┤
│                                                │
│   45 | public void login(User user) {         │
│   46 |   if (user == null) {                  │
│   47 |     throw new IllegalArgumentExcep...  │
│   48 |   }                                    │
│   49 |   // Validate credentials             │
│   50 |   if (!validateCredentials(user)) {   │
│   51 |     throw new AuthenticationExcep...  │
│   52 |   }                                    │
│   53 |   String token = tokenService.gen...  │
│   54 |   sessionRepository.save(new Sess...  │
│   55 | }                                      │
│                                                │
├────────────────────────────────────────────────┤
│ [Copy] [Open in Editor] [Send to AI] [Close]  │
└────────────────────────────────────────────────┘
```

**Features:**

- Syntax highlighting
- Line numbers
- Highlighted search terms
- Copy button (copy to clipboard)
- Open in editor button (if configured)
- Send to AI button (pre-populate chat context)

---

## 6. Settings - Configuration Forms

### 6.1 General Settings Tab

| Field                  | Type     | Required | Validation               | Default | Notes             |
|------------------------|----------|----------|--------------------------|---------|-------------------|
| `theme`                | ComboBox | ✅        | Enum: Light, Dark        | `Light` | Apply immediately |
| `language`             | ComboBox | ✅        | Enum: EN, VI, KO, ZH, JA | `EN`    | Restart required  |
| `default_project`      | ComboBox | ❌        | Valid project            | -       | For new chats     |
| `auto_save_interval`   | Spinner  | ✅        | 30-300 seconds           | `60`    |                   |
| `enable_notifications` | CheckBox | ✅        | -                        | `true`  |                   |
| `show_line_numbers`    | CheckBox | ✅        | -                        | `true`  | In code views     |

---

### 6.2 AI Configuration Tab

| Field                    | Type          | Required    | Validation                     | Default  | Notes                     |
|--------------------------|---------------|-------------|--------------------------------|----------|---------------------------|
| `llm_provider`           | ComboBox      | ✅           | Enum: OpenAI, Anthropic, Local | `OpenAI` |                           |
| `api_key`                | PasswordField | Conditional | Required if cloud provider     | -        | Masked input              |
| `model`                  | ComboBox      | ✅           | Provider-specific models       | -        | Dynamic based on provider |
| `temperature`            | Slider        | ✅           | 0.0 - 2.0                      | `0.2`    | Step: 0.1                 |
| `max_tokens`             | Spinner       | ✅           | 100 - 4000                     | `2000`   |                           |
| `enable_streaming`       | CheckBox      | ✅           | -                              | `true`   |                           |
| `embedding_model`        | ComboBox      | ✅           | Provider models                | -        |                           |
| `embedding_dimensions`   | TextField     | ✅           | Read-only                      | -        | Auto from model           |
| `enable_embedding_cache` | CheckBox      | ✅           | -                              | `true`   |                           |

**Model Options (dynamic):**

- OpenAI: gpt-4o, gpt-4-turbo, gpt-3.5-turbo
- Anthropic: claude-3-5-sonnet, claude-3-opus, claude-3-haiku
- Local: llama3, mixtral, codellama

**Test Connection Button:**

- Click → Shows progress spinner
- Success: Green checkmark + latency
- Failure: Red X + error message

---

### 6.3 Search Configuration Tab

| Field                         | Type          | Required    | Validation                         | Default                 | Notes                   |
|-------------------------------|---------------|-------------|------------------------------------|-------------------------|-------------------------|
| `vector_top_k`                | Spinner       | ✅           | 10 - 200                           | `50`                    |                         |
| `lexical_top_k`               | Spinner       | ✅           | 10 - 200                           | `50`                    |                         |
| `fusion_strategy`             | ComboBox      | ✅           | Enum: RRF, Weighted, Cross-Encoder | `RRF`                   |                         |
| `rrf_k`                       | Spinner       | Conditional | 1 - 100                            | `60`                    | Only if fusion=RRF      |
| `vector_weight`               | Slider        | Conditional | 0.0 - 1.0                          | `0.7`                   | Only if fusion=Weighted |
| `lexical_weight`              | Slider        | Conditional | 0.0 - 1.0                          | `0.3`                   | Only if fusion=Weighted |
| `qdrant_url`                  | TextField     | ✅           | Valid URL                          | `http://localhost:6333` |                         |
| `qdrant_api_key`              | PasswordField | ❌           | -                                  | -                       | Optional                |
| `index_optimization_schedule` | TextField     | ❌           | Valid cron                         | `0 2 * * *`             | Daily at 2 AM           |

---

### 6.4 Analytics Tab

**Not a form - Display only with export**

- Request volume chart (last 30 days) - Line chart
- Average rating chart - Bar chart
- Retrieval latency histogram - Histogram
- Popular topics - Word cloud

**Export Button:**

- Opens `ExportAnalyticsDialog`
- Select date range
- Select metrics to include
- Choose format (CSV, JSON)
- Save location

---

## 7. Common Validation Patterns

### 7.1 Required Field

```
Error: "This field is required"
Display: Red border + error text below field
```

### 7.2 Pattern Validation

```
Error: "Invalid format. Expected: [pattern description]"
Example: "Code must be 2-50 characters (A-Z, 0-9, _, -)"
```

### 7.3 Unique Constraint

```
Error: "This [field] already exists in [scope]"
Example: "Code 'HR' already exists in System 'ERP'"
```

### 7.4 Range Validation

```
Error: "[Field] must be between [min] and [max]"
Example: "Temperature must be between 0.0 and 2.0"
```

### 7.5 Date Range Validation

```
Error: "End date must be after start date"
Display: Highlight both fields in red
```

### 7.6 Async Validation (e.g., API key)

```
Progress: Spinner icon in field
Success: Green checkmark
Failure: Red X + error tooltip
```

---

## 8. Field Types Reference

### 8.1 Standard Controls

- **TextField**: Single-line text input
- **TextArea**: Multi-line text input (resizable)
- **PasswordField**: Masked text input (show/hide toggle)
- **ComboBox**: Dropdown selection
- **CheckBox**: Boolean toggle
- **RadioButton**: Single choice from group
- **Slider**: Numeric value selection (visual)
- **Spinner**: Numeric value with +/- buttons
- **DatePicker**: Calendar popup for date selection
- **ColorPicker**: Color selection dialog
- **FileChooser**: Native file selection dialog

### 8.2 Custom Controls

- **TagInput**: Comma-separated tags with autocomplete
- **RatingWidget**: Star rating (1-5)
- **CodeEditor**: Syntax-highlighted code input
- **MarkdownEditor**: Rich text with Markdown support
- **CronBuilder**: Visual cron expression builder
- **DirectoryPicker**: Directory-only file chooser
- **HierarchicalComboBox**: Tree-style dropdown (System > Subsystem > Project)

---

## 9. Accessibility Attributes

All fields should have:

- `aria-label` or `<label for="">` association
- `aria-required="true"` for required fields
- `aria-invalid="true"` when validation fails
- `aria-describedby` linking to error messages
- Keyboard navigation (Tab, Shift+Tab)
- Screen reader announcements for dynamic changes

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-15  
**Next:** See `04-architecture-layers.md` for code structure

