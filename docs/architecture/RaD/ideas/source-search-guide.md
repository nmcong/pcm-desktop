# Source Code Search Guide

This guide describes how the PCM Desktop platform performs and exposes source-code search, ensuring consistency with the
schemas defined in `system-analysis.md` and `semantic-search.md`.

---

## 1. Search Objectives

1. Quickly locate files, symbols, or snippets by keyword/regex.
2. Support semantic search via embeddings when lexical search is insufficient.
3. Allow UI and automation (LLMs, scripts) to reuse the same APIs.

---

## 2. Data Foundations

- **`source_files`**: stores per-file metadata (language, checksum, last_modified). Used for directory browsing and
  change detection.
- **`search_corpus` + `search_index`**: contains text chunks (code + docs) indexed by FTS5 with BM25 scoring (
  see `semantic-search.md`). Each row references `project_id` and optionally `file_id`.
- **`vector_documents`**: holds chunk metadata for embeddings; used when semantic matches are needed.
- **`ast_nodes`**: optional augmentation for symbol-aware search (class/method names, references).

---

## 3. Search Modes

### 3.1 Lexical (FTS5/BM25)

- Query `search_index` using the MATCH syntax (supports boolean operators, prefix search `token*`).
- Filter by `project_id`, `source_type='code'`, `language`, etc.
- Highlight results with snippet + file path from `search_corpus`.

### 3.2 Structural (AST)

- For symbol lookups, query `ast_nodes` by `name`, `fq_name`, or `checksum`.
- Use `ast_relationships` to navigate parents/children (e.g., find all methods in a class).
- Combine with lexical results for precise navigation.

### 3.3 Semantic (Embeddings)

- Convert query text to embedding → call Qdrant (per `rag-strategy.md`).
- Use payload filters: `project_id`, `language`, `chunk_type='code'`.
- Works well for natural-language descriptions or when naming differs.

### 3.4 Regex / Raw Search

- For advanced users, run on-demand file scans (e.g., ripgrep) limited to the selected project root. Cache results or
  feed back into `search_corpus` if needed.

---

## 4. API Workflow

1. User enters query in UI → specify mode (lexical / semantic / both).
2. **Lexical path**: call `runSemanticSearch` (function spec) with filters.
3. **Semantic path**: call `runVectorSearch` with embedding + filters.
4. Combine results via `hybridRetrieve` when both are enabled.
5. For structural context, call `getAstNodeDetail` for selected hits.
6. Allow export or follow-up actions (open file, start impact analysis, add to context for RAG).

---

## 5. UI Features

- Filter chips: system, subsystem, project, language, source type (code/doc/CHM).
- Result list includes: file path, score (BM25 or similarity), preview snippet, metadata badges (
  e.g., `CHM`, `Doc`, `Code`).
- Quick actions: open file viewer, copy citation, add to prompt context.
- Advanced panel: toggle regex search, choose AST search (symbol type dropdown).

---

## 6. Implementation Tips

- Keep FTS index in sync via triggers (`search_index`), and periodically
  run `INSERT INTO search_index(search_index) VALUES('optimize');`.
- Normalize case and remove sensitive data when populating `search_corpus`.
- When incremental scans detect file changes (`checksum` difference), update both `search_corpus` and embeddings.
- Provide caching for recent queries to avoid recomputing embeddings for identical text.

---

## 7. Consistency with Other Docs

- Retrieval results feed directly into the RAG pipeline described in `rag-strategy.md`.
- CHM-derived documents appear as `source_type='chm_doc'` (see `chm-ingestion.md`).
- The UI flows described here match the “Semantic Search Console” in `ui-domain-overview.md`.

By following this guide, developers and UX designers can implement a coherent search experience across the desktop app,
CLI utilities, and any automation hooks.
