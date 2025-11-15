# RAG Strategy Proposal

This document outlines the recommended approach, components, and tooling to build a Retrieval-Augmented Generation (RAG) pipeline tailored to the PCM Desktop system analysis platform.

---

## 1. Goals

1. Deliver accurate, traceable answers to user requirements by grounding LLM responses in project-specific artifacts.
2. Support both code-centric and documentation-centric queries with minimal latency.
3. Ensure reproducibility and observability for future tuning.

---

## 2. Overall Architecture

```
User Request → Intent & Filter → Retriever Layer → Context Builder → LLM → Response Logging
```

- **Intent & Filter**: classify request, infer project/subsystem, extract keywords.
- **Retriever Layer** (hybrid):
  - Vector search (Qdrant) on code/doc chunks.
  - Semantic keyword search (SQLite FTS5 TF-IDF).
  - Optional metadata filters (system/subsystem/project/status).
- **Context Builder**: deduplicate, rank, chunk-limiter, add structural metadata (file path, lines, AST info).
- **LLM**: streaming API (OpenAI, Anthropic, local LLM) with configurable prompt templates.
- **Response Logging**: `agent_responses`, `request_artifacts`, `answer_feedback`.

---

## 3. Recommended Technologies

| Layer            | Option                              | Notes                                                     |
|------------------|-------------------------------------|-----------------------------------------------------------|
| Embeddings       | OpenAI text-embedding-3-large / local BGE models | choose per deployment; store in Qdrant                    |
| Vector DB        | **Qdrant**                          | filtering, payload, HNSW, handles on-disk collections     |
| Keyword Search   | SQLite FTS5                         | already covered in `semantic-search.md`                   |
| Parser / Chunker | tree-sitter, Markdown parser        | language-aware chunking, align with AST lines             |
| LLM              | OpenAI GPT-4/4o, Anthropic Claude 3.5, or local (Llama3, Mixtral) | configurable via provider registry                        |
| Orchestration    | Java service (Spring Boot/Quarkus) or Kotlin + coroutine tasks | integrate with existing application stack                 |
| Prompt Framework | Simple template engine or LangChain4j/LightRAG (optional)     | ensure deterministic prompts                              |

---

## 4. Data Preparation

1. **Chunking Strategy**
   - Code: 150–300 tokens with 20% overlap; keep symbol names and path in metadata.
   - Docs: 300–500 tokens; preserve headings for context.
   - Agent responses / KB: 200–400 tokens.
2. **Metadata**
   - Always include `system_id`, `subsystem_id`, `project_id`, `file_path`, `start_line`, `end_line`, `language`, `commit_hash`.
3. **Embeddings**
   - Use the same tokenizer/model for all chunk types to keep vector space consistent.
   - Batch requests to minimize API cost; cache per checksum to avoid recompute.

---

## 5. Retrieval Workflow

1. **Filter Candidates**
   - Use request metadata (selected subsystem/project) to restrict search.
   - If unspecified, infer from keywords via classification model or heuristics.
2. **Hybrid Retrieval**
   - Run Qdrant nearest-neighbor query with the request embedding.
   - Execute FTS5 search on `search_index` for lexical matches (function names, config keys).
   - Combine using reciprocal rank fusion or weighted scoring.
3. **Context Assembly**
   - Deduplicate by `file_path + start_line`.
   - Limit total tokens (e.g., 2–4k) to keep LLM prompt manageable.
   - Add structural hints (symbol signature, AST snippet) to each chunk.
4. **Prompting**
   - Template: include requirement text, system metadata, retrieved chunks with citations.
   - Add instructions: cite sources, highlight uncertainty, request confirmation for destructive actions.
5. **Post-processing**
   - Extract cited sources (file path + lines) → `request_artifacts`.
   - Log raw prompt & completion for audit (secured store).

---

## 6. Quality & Monitoring

1. **Feedback Loop**
   - Capture `answer_feedback` and `rating` to identify low-confidence answers.
   - Tag negative cases for manual review and dataset augmentation.
2. **Metrics**
   - Retrieval latency, number of chunks used, average BM25 score, embedding similarity.
3. **Eval Harness**
   - Maintain a corpus of question→ground truth contexts (golden set) to run offline evaluation (precision@k, recall@k).
4. **Alerting**
   - Monitor Qdrant cluster health, embedding queue backlog, LLM error rates.

---

## 7. Deployment Considerations

- **Offline mode**: allow local embeddings/LLM when internet is restricted (bge-large, local Qdrant).
- **Security**: scrub secrets before indexing; enforce access control per system/subsystem.
- **Scalability**: sharding per subsystem is optional; Qdrant can scale horizontally.
- **Cost control**: deduplicate embeddings by checksum; reuse chunk metadata between branches when possible.

---

## 8. Next Steps

1. Implement ingestion pipeline aligning with `system-analysis.md`.
2. Build hybrid retriever service exposing gRPC/REST API.
3. Integrate request UI with retrieval service; add response viewer with citations.
4. Iterate via offline eval + user feedback.

This approach leverages the existing metadata (SQLite, AST) and adds a robust retrieval layer to support accurate RAG responses. Update this proposal as you finalize technology choices or adapt to new requirements.
