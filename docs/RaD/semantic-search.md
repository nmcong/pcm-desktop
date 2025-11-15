# Semantic Search (FTS5 + TF‑IDF) Design

This document describes how to extend the requirement-analysis platform with a semantic/keyword search layer based on SQLite FTS5 (TF-IDF scoring) and how it integrates with existing metadata, AST, and vector search (Qdrant).

---

## 1. Objectives

1. Provide fast lexical/semantic search over code, documentation, and historical answers.
2. Support TF-IDF/BM25 style scoring using SQLite’s FTS5 module.
3. Combine FTS results with vector search (hybrid retrieval) to boost answer accuracy.
4. Keep the index synchronized with incremental scans (reuse checksum logic).

---

## 2. Data Model Extensions

### 2.1 Base Tables

```sql
CREATE TABLE search_corpus (
  corpus_id      INTEGER PRIMARY KEY AUTOINCREMENT,
  project_id     INTEGER REFERENCES projects(project_id),
  file_id        INTEGER REFERENCES source_files(file_id),
  source_type    TEXT NOT NULL,   -- code, doc, kb_article, response
  label          TEXT,            -- e.g. filename or article title
  content        TEXT NOT NULL,
  last_indexed_at DATETIME,
  checksum       TEXT             -- checksum of content for change detection
);
```

### 2.2 FTS5 Virtual Table

```sql
CREATE VIRTUAL TABLE search_index USING fts5(
  corpus_id UNINDEXED,
  project_id UNINDEXED,
  source_type,
  label,
  content,
  tokenize = 'unicode61 remove_diacritics 2',
  detail = 'column'
);
```

### 2.3 Sync Triggers

```sql
CREATE TRIGGER search_index_insert AFTER INSERT ON search_corpus
BEGIN
  INSERT INTO search_index(rowid, corpus_id, project_id, source_type, label, content)
  VALUES (new.corpus_id, new.corpus_id, new.project_id, new.source_type, new.label, new.content);
END;

CREATE TRIGGER search_index_update AFTER UPDATE ON search_corpus
BEGIN
  UPDATE search_index
     SET project_id = new.project_id,
         source_type = new.source_type,
         label = new.label,
         content = new.content
   WHERE rowid = new.corpus_id;
END;

CREATE TRIGGER search_index_delete AFTER DELETE ON search_corpus
BEGIN
  DELETE FROM search_index WHERE rowid = old.corpus_id;
END;
```

> Note: FTS5 already computes TF-IDF/BM25 scores via the `bm25()` function. Use `fts5_rank` or `bm25(search_index)` when querying.

---

## 3. Ingestion Flow

1. **Source Detection**  
   During the “Source Scan & AST Build” stage (see `system-analysis.md`), identify textual artifacts that should be searchable:
   - Code files (after stripping comments? optional)
   - Markdown/HTML docs
   - Knowledge base articles
   - Agent responses (for retro queries)

2. **Change Check**  
   For each artifact, compute checksum:
   - If `search_corpus` already has the same checksum → skip.
   - Otherwise upsert content and let FTS triggers update `search_index`.

3. **Normalization**  
   - Optional: store normalized text (lowercase, remove sensitive data) in `content`.
   - Keep original path/title in `label` for display.

4. **Chunk Control**  
   - For very large files, split into sections (i.e., align with `vector_documents` chunk_id) and store each chunk as a separate corpus row.

---

## 4. Query Flow

1. **Receive requirement** (from `user_requests`).
2. **Intent classification** (optional) to identify project/subsystem filters.
3. **FTS Search**  
   ```sql
   SELECT corpus_id, project_id, source_type, label,
          bm25(search_index, 1.2, 0.75) AS score
     FROM search_index
    WHERE search_index MATCH :query
      AND project_id = :projectId
    ORDER BY score
    LIMIT 50;
   ```
4. **Vector Search** (Qdrant) using the same query as embedding.
5. **Score Fusion**  
   - Normalize FTS and vector scores (min-max or z-normalization).
   - Weighted sum or reciprocal rank fusion to produce final candidate list.
6. **Context Assembly**  
   - Fetch metadata via `search_corpus` (paths, line numbers).
   - Pair with AST info if needed (via `file_id` → `source_files` → `ast_nodes`).
7. **LLM Prompting**  
   - Provide fused context to LLM along with request.
   - Log chosen contexts in `request_artifacts`.

---

## 5. API / Service Contracts

- `POST /search/fts`: takes text query, optional filters (project, source_type), returns ranked hits and metadata.
- `POST /search/hybrid`: executes both FTS and vector search, returns fused results plus trace (scores, chosen context).
- `POST /corpus/reindex`: optionally re-index a set of files/projects; triggers recalculation of `search_corpus` entries.

Each endpoint logs latency and number of hits for observability.

---

## 6. Maintenance & Performance Tips

1. **Vacuum** FTS tables periodically: `INSERT INTO search_index(search_index) VALUES('optimize');`
2. **FTS external content**: if dataset grows large, switch to the “external content” mode to keep FTS index slim and rely on `search_corpus` as the content table.
3. **Stop-word tuning**: configure `tokenize = 'unicode61'` with custom stop-word list to match domain vocabulary.
4. **Query rewriting**: support quotes for exact matches, prefix search (`token*`), and boolean operators (AND/OR/NOT).
5. **Security**: sanitize user queries to prevent injection (FTS uses its own syntax).
6. **Monitoring**: track `bm25` score distributions to detect drift; low scores may indicate stale corpus or domain mismatch.

---

## 7. Integration with Existing Docs

- Align ingestion with `system-analysis.md` (Section 4.2/4.3).
- Reference `vector_documents` to avoid duplicate chunking.
- ER relationships: `search_corpus` connects to `projects` and `source_files`. Update `system-analysis-erd.md` if the new table is adopted.

This semantic search layer allows the platform to answer keyword-heavy queries rapidly while still benefiting from deep understanding via RAG/LLM.
