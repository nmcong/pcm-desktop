# Function Calling Specification

This document lists the core service/"function calling" endpoints that the PCM requirement-analysis platform should expose so that the UI, automation agents, and LLM integrations can orchestrate tasks consistently. Each function aligns with the schemas described in `system-analysis.md`, `semantic-search.md`, `chm-ingestion.md`, and `rag-strategy.md`.

---

## Summary Table

| Function | Purpose | Key Entities |
|----------|---------|--------------|
| `registerSystem` | Create/update System/Subsystem/Project tree nodes. | `systems`, `subsystems`, `projects`, `batches` |
| `attachSourceRoot` | Map a project to a local/VCS path and schedule scan. | `project_sources`, `source_files` |
| `triggerSourceScan` | Run checksum diff, AST rebuild, vector reindex. | `source_files`, `ast_snapshots`, `vector_documents` |
| `ingestChmPackage` | Decompile/import CHM into knowledge base. | `chm_imports`, `chm_documents`, `search_corpus` |
| `upsertKnowledgeChunk` | Register arbitrary doc chunk (Markdown/PDF). | `search_corpus`, `vector_documents` |
| `runSemanticSearch` | Execute TF-IDF/FTS query. | `search_index` |
| `runVectorSearch` | Query Qdrant for embeddings. | `vector_documents` + Qdrant |
| `hybridRetrieve` | Combine lexical + vector results with ranking. | retrieval layer |
| `submitUserRequest` | Persist a requirement/question and start analysis. | `user_requests` |
| `streamAgentResponse` | Stream LLM answer while logging context. | `agent_responses`, `request_artifacts` |
| `recordFeedback` | Store rating/comments for continuous learning. | `answer_feedback` |
| `getAstNodeDetail` | Return AST info for a symbol/node. | `ast_nodes`, `ast_relationships` |
| `listRequests` | Filter/search historical requests/responses. | `user_requests`, `agent_responses` |

---

## Function Details

### 1. `registerSystem`
- **Inputs**: `{ system, subsystems[], projects[], batches[] }` payload describing hierarchy nodes (ids optional for updates).
- **Behavior**: Upsert rows in `systems`, `subsystems`, `projects`, `batches`; enforce parent-child relations per ERD.
- **Output**: Normalized tree with generated IDs; validation errors for duplicates.
- **Usage**: Admin UI when creating or editing structure.

### 2. `attachSourceRoot`
- **Inputs**: `{ project_id, root_path, vcs_type, branch, commit }`.
- **Behavior**: Write row to `project_sources` (status `pending`), optionally schedule scan.
- **Output**: Source descriptor + `source_id`.
- **Notes**: Called post directory selection; referenced in `ui-domain-overview.md` (Source Repository Manager).

### 3. `triggerSourceScan`
- **Inputs**: `{ source_id, mode }` where `mode` ∈ `full`, `incremental`.
- **Behavior**: Walk filesystem, compute checksum and update `source_files`; create `ast_snapshots`, refresh `vector_documents` & Qdrant entries; update `search_corpus` for textual data.
- **Output**: Job status, counts of files processed/skipped.
- **Dependencies**: Uses checksum logic from `system-analysis.md` §6.

### 4. `ingestChmPackage`
- **Inputs**: `{ project_id?, subsystem_id?, chm_path }`.
- **Behavior**: Insert `chm_imports` row, run extraction (per `chm-ingestion.md`), populate `chm_documents` & `chm_assets`, then call `upsertKnowledgeChunk` per page.
- **Output**: Import id, doc count, errors.

### 5. `upsertKnowledgeChunk`
- **Inputs**: `{ source_type, project_id, file_id?, label, content, metadata }` plus optional embedding.
- **Behavior**: Insert/update `search_corpus`; update FTS index (trigger); store chunk metadata in `vector_documents`; send embedding to Qdrant via `embedding_id` (payload includes metadata).
- **Output**: `document_id`, `chunk_id` references.

### 6. `runSemanticSearch`
- **Inputs**: `{ query, project_filter?, source_type? }`.
- **Behavior**: Execute FTS query using `bm25(search_index)`; return top hits with snippet and metadata.
- **Output**: Ranked list with `corpus_id`, file path, label, BM25 score.
- **Reference**: `semantic-search.md` §4.

### 7. `runVectorSearch`
- **Inputs**: `{ embedding[], filters }`.
- **Behavior**: Query Qdrant collection; return hits with payload (file path, lines, chunk_id).
- **Output**: Distances/scores + metadata.

### 8. `hybridRetrieve`
- **Inputs**: `query`, `project_id`, `top_k_semantic`, `top_k_vector`.
- **Behavior**: Call functions 6 & 7, then fuse results (reciprocal rank, weighted sum). Deduplicate by file path/lines, annotate reason.
- **Output**: Combined ranked contexts ready for prompting.
- **Reference**: `rag-strategy.md` §5.

### 9. `submitUserRequest`
- **Inputs**: `{ user_id, project_id?, subsystem_id?, title, description }`.
- **Behavior**: Insert row into `user_requests`; optionally attach files (`request_artifacts`). Kick off retrieval+LLM pipeline asynchronously.
- **Output**: `request_id`, initial status.

### 10. `streamAgentResponse`
- **Inputs**: `{ request_id, conversation_id?, prompt_override? }`.
- **Behavior**: Runs hybrid retrieval, builds prompt, streams LLM response with citations, persists to `agent_responses`/`request_artifacts` (chunks used). Handles partial outputs, errors.
- **Output**: streaming tokens + final response record.

### 11. `recordFeedback`
- **Inputs**: `{ response_id, rating, comment }`.
- **Behavior**: Insert into `answer_feedback`; update analytics metrics (RAG quality).
- **Output**: confirmation.

### 12. `getAstNodeDetail`
- **Inputs**: `{ node_id }` or `{ file_id, line }`.
- **Behavior**: Retrieve `ast_nodes` row, relationships, linked dependencies; optionally include snippet from `source_files`.
- **Output**: JSON describing symbol, references, parents.
- **Usage**: AST Explorer, LLM reasoning helpers.

### 13. `listRequests`
- **Inputs**: filter object (date range, project, status, rating).
- **Behavior**: Query `user_requests` joined with `agent_responses`, `answer_feedback`; support pagination and export.
- **Output**: list plus summary counts.

---

## Implementation Notes

- These functions can be exposed as REST/gRPC endpoints or as function-calling schema for LLM agents. Use consistent names and JSON schemas so automation can call them safely.
- Align validation with the constraints documented in `system-analysis.md` and `chm-ingestion.md`.
- All write operations should emit telemetry (duration, entity ids) to support observability described in `rag-strategy.md`.
- Any new function must update this spec to keep UI/backend/LLM integrations in sync.
