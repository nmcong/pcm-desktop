# Intelligent Requirement Analysis Platform – Data & Processing Design

This document captures the data schema, storage choices, and processing flow required to build a system that ingests user requirements, analyzes project source code, and delivers accurate answers (RAG + AST assisted).

---

## 1. Storage Overview

| Purpose                         | Store              | Rationale                                        |
|---------------------------------|--------------------|--------------------------------------------------|
| Operational metadata            | SQLite             | Lightweight, transactional, easy to bundle       |
| Semantic search (RAG)           | Qdrant             | Vector similarity queries, filtering, payload    |
| Optional full‑text on code/docs | (optional) Lucene/Elastic) | For large repos needing regex/full-text   |
| Optional artifacts/BLOBs        | Object storage (S3/minio/local) | Store heavy AST snapshots or uploads  |

---

## 2. SQLite Schema

### 2.1 System Hierarchy (reused)

`systems`, `subsystems`, `projects`, `batches` – see `system-hierarchy.md`.

### 2.2 Source Repository Metadata

```sql
CREATE TABLE project_sources (
  source_id      INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id     INTEGER NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
  root_path      TEXT NOT NULL,
  vcs_type       TEXT CHECK (vcs_type IN ('git','svn','none')) DEFAULT 'git',
  default_branch TEXT,
  current_commit TEXT,
  language       TEXT,
  scan_status    TEXT DEFAULT 'pending',
  last_scanned_at DATETIME
);

CREATE TABLE source_files (
  file_id        INTEGER PRIMARY KEY AUTOINCREMENT,
  source_id      INTEGER NOT NULL REFERENCES project_sources(source_id) ON DELETE CASCADE,
  relative_path  TEXT NOT NULL,
  language       TEXT,
  size_bytes     INTEGER,
  checksum       TEXT,        -- content hash (MD5/SHA-256)
  last_modified  DATETIME,    -- file timestamp captured during scan
  is_binary      INTEGER DEFAULT 0,
  UNIQUE(source_id, relative_path)
);

CREATE INDEX idx_source_files_checksum ON source_files(checksum);
```

### 2.3 Dependency & Impact Graph

```sql
CREATE TABLE file_dependencies (
  dependency_id  INTEGER PRIMARY KEY AUTOINCREMENT,
  file_id        INTEGER NOT NULL REFERENCES source_files(file_id) ON DELETE CASCADE,
  target_file_id INTEGER REFERENCES source_files(file_id) ON DELETE SET NULL,
  dependency_type TEXT NOT NULL, -- import, call, extends, etc.
  symbol_name     TEXT,
  UNIQUE(file_id, target_file_id, dependency_type, symbol_name)
);
```

### 2.4 AST Storage

```sql
CREATE TABLE ast_snapshots (
  snapshot_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  source_id      INTEGER NOT NULL REFERENCES project_sources(source_id) ON DELETE CASCADE,
  commit_hash    TEXT,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  tool_version   TEXT,
  root_checksum  TEXT
);

CREATE TABLE ast_nodes (
  node_id        INTEGER PRIMARY KEY AUTOINCREMENT,
  snapshot_id    INTEGER NOT NULL REFERENCES ast_snapshots(snapshot_id) ON DELETE CASCADE,
  file_id        INTEGER REFERENCES source_files(file_id),
  node_type      TEXT NOT NULL,
  name           TEXT,
  fq_name        TEXT,
  start_line     INTEGER,
  end_line       INTEGER,
  checksum       TEXT,
  payload        TEXT -- JSON blob for attributes
);

CREATE TABLE ast_relationships (
  relationship_id INTEGER PRIMARY KEY AUTOINCREMENT,
  snapshot_id     INTEGER NOT NULL REFERENCES ast_snapshots(snapshot_id) ON DELETE CASCADE,
  parent_node_id  INTEGER NOT NULL REFERENCES ast_nodes(node_id) ON DELETE CASCADE,
  child_node_id   INTEGER NOT NULL REFERENCES ast_nodes(node_id) ON DELETE CASCADE,
  relation_type   TEXT NOT NULL -- parent, reference, implements, calls, etc.
);
```

### 2.5 User Requests & Responses

```sql
CREATE TABLE user_requests (
  request_id     INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id        TEXT,
  subsystem_id   INTEGER REFERENCES subsystems(subsystem_id),
  project_id     INTEGER REFERENCES projects(project_id),
  title          TEXT,
  description    TEXT,
  request_type   TEXT DEFAULT 'question',
  priority       TEXT DEFAULT 'normal',
  status         TEXT DEFAULT 'received',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  resolved_at    DATETIME
);

CREATE TABLE request_artifacts (
  artifact_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  request_id     INTEGER NOT NULL REFERENCES user_requests(request_id) ON DELETE CASCADE,
  artifact_type  TEXT, -- 'file', 'code_segment', 'document', 'url'
  reference_path TEXT,
  metadata       TEXT,
  content_hash   TEXT
);

CREATE TABLE agent_responses (
  response_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  request_id     INTEGER NOT NULL REFERENCES user_requests(request_id) ON DELETE CASCADE,
  answer_text    TEXT,
  reasoning      TEXT,
  cited_sources  TEXT, -- JSON array of file references
  status         TEXT DEFAULT 'draft',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE answer_feedback (
  feedback_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  response_id    INTEGER NOT NULL REFERENCES agent_responses(response_id) ON DELETE CASCADE,
  rating         INTEGER CHECK (rating BETWEEN 1 AND 5),
  comment        TEXT,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2.6 Semantic Chunk Metadata

Metadata stored in SQLite; embeddings stored in Qdrant.

```sql
CREATE TABLE vector_documents (
  document_id    INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id     INTEGER REFERENCES projects(project_id),
  file_id        INTEGER REFERENCES source_files(file_id),
  chunk_id       TEXT UNIQUE,
  content_type   TEXT, -- code, doc, issue, etc.
  start_line     INTEGER,
  end_line       INTEGER,
  metadata       TEXT, -- JSON
  qdrant_point   TEXT NOT NULL -- reference to vector id
);
```

---

## 3. Qdrant Collections

Use separate collections per content type or a single collection with payload filtering:

- `code_chunks`: vector embedding + payload `{project_id, file_path, language, lines}`
- `doc_chunks`: `{project_id, doc_type, tags}`

Payload should store `chunk_id` to map back to `vector_documents`.

---

## 4. Processing Flow

### 4.1 Project Registration
1. User selects/creates System → Subsystem → Project in UI.
2. User picks local source root → insert into `project_sources` (status `pending`).
3. Optionally capture VCS info (branch/commit).

### 4.2 Source Scan & AST Build
1. Scheduler/worker reads `project_sources` with `scan_status='pending'`.
2. Walk filesystem:
   - Insert/Update `source_files`.
   - Detect deletions (remove stale records).
3. Dependency extraction (language parser or ctags) → fill `file_dependencies`.
4. Build AST snapshot (per commit):
   - Insert row in `ast_snapshots`.
   - Store nodes & relationships with checksums for dedup.
5. Mark `scan_status='complete'`, set `last_scanned_at`.

### 4.3 Chunking & Vectorization
1. For each relevant file (code/doc), split into chunks (e.g., 200 tokens with overlap).
2. Store metadata in `vector_documents`.
3. Generate embedding → upsert into Qdrant using `chunk_id` as point id.
4. Keep embedding payload consistent with metadata (project, path, lines).

### 4.4 Handling User Requests
1. User submits requirement/question (select subsystem/project) → `user_requests`.
2. Pipeline:
   - Retrieve contextual metadata (project, recent activity).
   - Query Qdrant with requirement text to get top‑k chunks.
   - Optionally query SQLite for AST/metadata (e.g., symbol search, dependencies).
3. Compose context (chunks + metadata) → feed to LLM.
4. Store response in `agent_responses` with `cited_sources`.
5. Attach artifacts (e.g., generated diff, diagrams) via `request_artifacts`.
6. Present answer; user can rate (`answer_feedback`).

### 4.5 Impact Analysis (example use of AST/Dependencies)
1. Select code element (from AST) referenced in requirement.
2. Walk `ast_relationships` + `file_dependencies` to list affected nodes/files.
3. Locate embeddings for those files (via `vector_documents`) and fetch documentation chunks from Qdrant for context.

---

## 5. Optional Enhancements

- **Session logging** (`analysis_sessions`) to group multiple requests/responses.
- **Knowledge base** tables (articles, tags, versions) feeding both SQLite metadata and Qdrant embeddings.
- **Full-text index** (SQLite FTS5 or external) for regex/search across large code bases.
- **Change detection** using `source_files.checksum` to trigger incremental AST rebuild and re-embedding.

---

## 6. Implementation Notes

1. **Batching:** When storing AST nodes or vector chunks, batch inserts to keep SQLite fast.
2. **File change detection:** Use `source_files.checksum`, `size_bytes`, and `last_modified` to skip unchanged files. Rebuild AST + embeddings only for records whose checksum differs from the previous scan.
3. **Compression:** AST payloads and large reasoning fields can be compressed before storing in SQLite if size is a concern.
4. **Consistency:** Use `snapshot_id` to tie AST, dependencies, and embeddings to a specific commit; store commit hash in metadata for reproducibility.
5. **Security:** If users can upload arbitrary files, sanitize before indexing to avoid executing code.

---

This schema and flow provide the backbone for a requirement-analysis assistant that understands project structure, sources, and historical interactions. Extend as needed for domain-specific artifacts or integrations.
