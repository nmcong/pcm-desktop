# CHM Knowledge Ingestion Guide

This guide explains how to extract content from Microsoft Compiled HTML Help (CHM) files and feed it into the PCM Desktop knowledge system so the RAG pipeline can reference the material.

---

## 1. Why ingest CHM?

- Legacy documentation bundles frequently ship as CHM.
- CHM contains an internal TOC + set of HTML pages that can be mapped directly to our chunking/indexing pipeline.
- Once extracted, the resulting HTML/text can be treated like any other knowledge article (stored in SQLite, embedded in Qdrant, indexed via FTS5).

---

## 2. Tooling Options

| Tool              | Platform | Notes                                                   |
|-------------------|----------|---------------------------------------------------------|
| `hh.exe -decompile` | Windows | Native tool; requires Windows environment.              |
| `chmlib` / `extract_chmLib` | Linux/macOS | Open-source library + CLI to extract CHM contents. |
| `libmspack`       | Cross    | Provides `mht2htm`, `chmdump`, used by many utilities.   |
| `pychm`           | Cross    | Python bindings around `chmlib`; easy for scripting.    |

Recommendation: use `extract_chmLib` (Linux/macOS) or `hh.exe` (Windows). Wrap in a script so users can point to a CHM file and produce a folder of HTML/TOC.

---

## 3. Extraction Workflow

1. **User Input**  
   - Provide UI or CLI command asking for CHM file path + target project/subsystem.
2. **Decompile**  
   - Run `extract_chmLib help.chm output_dir/` or `hh.exe -decompile output_dir help.chm`.
   - Result: directory containing `.htm/.html` files, images, `hhc/hhk` (TOC/index).
3. **Normalize HTML**  
   - Convert to UTF-8.
   - Strip scripts, inline styles if not needed.
   - Preserve headings (H1–H6) and breadcrumb info.
4. **Metadata Capture**  
   - For each HTML page, store:
     - `source_path` (relative path inside CHM)
     - `title` (from `<title>` or top heading)
     - `toc_path` (hierarchy derived from `.hhc`).
   - Insert into `search_corpus` (`source_type='chm_doc'`) and knowledge base tables if needed.
5. **Chunking & Embedding**  
   - Split HTML/text into 300–500 token chunks with overlap.
   - Store chunks in `vector_documents` (+ metadata).
   - Generate embeddings and upsert into Qdrant.
6. **FTS Index**  
   - Insert full text into `search_corpus` so FTS5 handles keyword queries.

---

## 4. Implementation Steps

### 4.1 Extraction Script (pseudo)

```bash
extract_chmLib "$CHM_PATH" "$OUTPUT_DIR"
python scripts/ingest_chm.py \
  --source "$OUTPUT_DIR" \
  --project-id 123 \
  --subsystem-id 5 \
  --commit "CHM-2025-01"
```

### 4.2 Parser Tasks

`scripts/ingest_chm.py` should:

1. Walk extracted directory, read `.hhc` to build hierarchy.
2. For each HTML file:
   - Parse title + headings (BeautifulSoup or jsoup).
   - Convert to Markdown/plain text.
   - Compute checksum (for dedupe).
   - Upsert row in `search_corpus`.
3. Generate chunks (`vector_documents`), produce embeddings, push to Qdrant.
4. Log inserted documents in `request_artifacts` or a custom table for audit.

### 4.3 Incremental Updates

- Store `checksum` in `search_corpus` so re-ingesting the same CHM only updates changed pages.
- Keep `metadata` JSON with `chm_file`, `toc_path`, `order_index`.

---

## 5. UI Integration

1. Add a “CHM Import” option in the knowledge management screen.
2. Flow:
   - User selects CHM file.
   - Choose target System/SubSystem/Project.
   - Trigger extraction script (background task) and show progress.
   - Once done, display imported sections; allow tagging/categories.
3. Provide ability to re-scan (if CHM updated) and to delete imported docs.

---

## 6. Best Practices

- **Security**: sanitize extracted HTML to prevent XSS when rendering in UI.
- **Link handling**: convert relative links between CHM pages into in-app navigation or knowledge base cross-links.
- **Images**: copy image assets; store path in metadata so the UI can fetch them.
- **TOC ordering**: use the `.hhc` tree to preserve logical order when chunking to improve retrieval quality.
- **Versioning**: tie imports to `analysis_snapshots` or a dedicated `chm_imports` table with timestamp/commit so you can trace which CHM version supported a response.

---

## 7. Integration with RAG

- Once ingested, CHM content behaves like any other document:
  - Qdrant payload includes `source_type='chm_doc'` for filtering.
  - FTS5 queries can filter by `source_type`.
  - Provide citations referencing the original CHM path and section.

This process ensures legacy CHM manuals become first-class knowledge sources within the platform.
