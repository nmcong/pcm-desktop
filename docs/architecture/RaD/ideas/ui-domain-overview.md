# UI & Domain Overview

This document consolidates the entities and screens managed by the PCM Desktop system. It references the data models
described in the other RaD documents (system analysis, semantic search, CHM ingestion, RAG strategy) and defines how
users interact with each area.

---

## 1. Core Domain Entities

| Entity                  | Description                                                       | Source Docs                                      |
|-------------------------|-------------------------------------------------------------------|--------------------------------------------------|
| **System**              | Top-level platform or business domain.                            | `system-analysis.md` §2.1                        |
| **Subsystem**           | Logical grouping within a system (e.g., module, capability).      | `system-analysis.md` §2.1                        |
| **Project**             | Specific initiative/application inside a subsystem.               | `system-analysis.md` §2.1                        |
| **Batch**               | Scheduled or background job tied to a subsystem.                  | `system-analysis.md` §2.1                        |
| **Project Source**      | Repository metadata (root path, branch, commit).                  | `system-analysis.md` §2.2                        |
| **Source File**         | File-level metadata (language, size, checksum, status).           | `system-analysis.md` §2.2                        |
| **AST Snapshot/Node**   | Structural representation of source code for analysis.            | `system-analysis.md` §2.4                        |
| **Vector Document**     | Chunk metadata for embeddings.                                    | `system-analysis.md` §2.6 / `rag-strategy.md` §3 |
| **Search Corpus Entry** | Text record for FTS/semantic search (code, docs, CHM, responses). | `semantic-search.md` §2                          |
| **CHM Import/Document** | Legacy docs ingested from CHM packages.                           | `chm-ingestion.md` §8                            |
| **User Request**        | Question/requirement coming from end users.                       | `system-analysis.md` §2.5                        |
| **Agent Response**      | Answer produced by LLM, with citations.                           | `system-analysis.md` §2.5 / `rag-strategy.md` §5 |
| **Feedback**            | Evaluation of responses for quality loop.                         | `system-analysis.md` §2.5                        |

These entities interconnect per the ERD (`system-analysis-erd.md`) and the RAG flow uses them to fetch context.

---

## 2. Screen Catalogue

### 2.1 System Hierarchy Management

- **Purpose:** Maintain System → Subsystem → Project → Batch structure.
- **Key actions:** create/edit/delete; view ownership; assign repos.
- **Data sources:** `systems`, `subsystems`, `projects`, `batches`, `project_sources`.
- **Dependencies:** CRUD operations must respect parent-child constraints described in `system-analysis.md`.

### 2.2 Source Repository Manager

- **Purpose:** Register project source roots, track scan status, trigger re-scan.
- **Key actions:** select folder, set VCS info, view checksum/change status, launch AST/vector re-build.
- **Data sources:** `project_sources`, `source_files`, `ast_snapshots`; monitors change via checksum
  logic (`system-analysis.md` §6).

### 2.3 AST & Dependency Explorer

- **Purpose:** Visualize AST nodes, relationships, dependencies for impact analysis.
- **Features:** Tree view per snapshot; search symbol; show
  references (`ast_nodes`, `ast_relationships`, `file_dependencies`).
- **Linkages:** Connects to vector docs for context preview; references `rag-strategy.md` for retrieval hints.

### 2.4 Semantic Search Console

- **Purpose:** Run TF-IDF/BM25 queries over `search_corpus`.
- **UI elements:** query box, filters (system/subsystem/project/source_type), result table with highlight.
- **Integration:** Option to send hits to RAG pipeline (hybrid retrieval). Based on `semantic-search.md`.

### 2.5 Knowledge Import Center

- **Purpose:** Manage CHM and other knowledge sources.
- **Workflow:** upload CHM → track status (`chm_imports.status`) → view extracted docs (`chm_documents`), assets.
- **Actions:** re-ingest, delete import, preview doc; set tags/mapping to subsystems.
- **Docs:** `chm-ingestion.md` for pipeline + schema.

### 2.6 RAG Workspace (AI Assistant Page)

- **Purpose:** User chat interface for requirement analysis (current `AIAssistantPage`).
- **Components:** conversation sidebar, message pane, input area.
- **Data:** `user_requests`, `agent_responses`, `vector_documents`, `search_corpus` (retrieval results). Behavior
  specified in `rag-strategy.md`.
- **Enhancements:** display trace (chunks, citations), feedback (rating).

### 2.7 Request Tracking & History

- **Purpose:** Dashboard to review all `user_requests`, statuses, assigned project.
- **Features:** filters, link to responses, view attached artifacts.
- **Dependencies:** interplay with conversation service (persist messages); ensures `system-analysis.md` §2.5 flows are
  respected.

### 2.8 Batch Jobs Monitor

- **Purpose:** Manage scheduled batches within a subsystem (align with `BatchJobsPage`).
- **Data:** `batchesplus` any runtime status (view model `BatchJobsViewModel`). Could incorporate batch requests
  affecting analysis.

### 2.9 Settings & Analytics

- **Purpose:** Provide theme/system settings plus analytics (retrieval latency, answer quality).
- **Data:** `settings` (via `SettingsViewModel`), telemetry (log, maybe new tables).
- **Reference:** `rag-strategy.md` (quality metrics) for available KPIs.

---

## 3. Interaction Flow Summary

1. **Admin defines structure** (System/SubSystem/Project) → `System Hierarchy`.
2. **Register source repos** → `Source Repository Manager` triggers scans & AST builds.
3. **Ingest knowledge** (docs, CHM) → `Knowledge Import Center`.
4. **User requests analysis** → `RAG Workspace` leverages hybrid retrieval (vector + FTS) per `rag-strategy.md`.
5. **Answers logged** → `Request Tracking` shows history, `Feedback` records quality.
6. **Investigate impact** → `AST Explorer` / `Semantic Search Console`.

Each step touches the data structures described in `system-analysis.md` and is visualized via the UI screens above.

---

## 4. Consistency Notes

- **Entity relationships** must remain aligned with `system-analysis-erd.md`; any UI extension should update that ERD.
- **Semantic search** view should filter on the same `source_type` enumerations defined in `semantic-search.md`.
- **CHM pages** displayed in Knowledge Import should map 1:1 with `chm_documents` to guarantee traceability.
- **RAG context** presented to users must cite `vector_documents` and `search_corpus` entries to maintain auditability
  per `rag-strategy.md`.
- **Feedback loop** (ratings) ties back to `agent_responses` and can be surfaced both in RAG Workspace and Analytics.

Keep this overview in sync when new screens or entities are introduced so frontend and backend share a common mental
model.
