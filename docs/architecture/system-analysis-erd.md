# System Analysis ERD

Below is a Mermaid ER diagram that captures the main entities and relationships introduced in the requirement-analysis design.

```mermaid
erDiagram
    systems ||--o{ subsystems : contains
    subsystems ||--o{ projects : owns
    subsystems ||--o{ batches : schedules
    projects ||--o{ project_sources : tracks
    project_sources ||--o{ source_files : includes
    source_files ||--o{ file_dependencies : "depends on"
    project_sources ||--o{ ast_snapshots : versions
    ast_snapshots ||--o{ ast_nodes : contains
    ast_nodes ||--o{ ast_relationships : links
    projects ||--o{ vector_documents : chunks
    source_files ||..o{ vector_documents : "provides context for"
    subsystems ||..o{ user_requests : originates
    projects ||..o{ user_requests : relevant
    user_requests ||--o{ request_artifacts : attaches
    user_requests ||--o{ agent_responses : answers
    agent_responses ||--o{ answer_feedback : rated
```

## Legend

- `||--o{` indicates one-to-many.
- `||..o{` denotes optional relationships (the foreign key may be null).

This diagram mirrors the schema defined in `system-analysis.md`; update both artifacts together when the data model evolves.
