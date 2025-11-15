# System Hierarchy Data Model

This document describes the standard database schema for managing enterprise systems, their subsystems, and the work items (projects and batch jobs) that belong to them.

---

## Overview

```
System ──┬─< Subsystem ──┬─< Project
         │               └─< Batch
```

- A **System** represents a top-level platform or business domain.
- Each **Subsystem** belongs to exactly one System and groups related initiatives.
- **Projects** capture long-running efforts under a Subsystem.
- **Batches** model scheduled/batch jobs associated with a Subsystem (optionally linked to projects later).

---

## Table Definitions

```sql
CREATE TABLE systems (
  system_id      BIGSERIAL PRIMARY KEY,
  code           VARCHAR(50) UNIQUE NOT NULL,
  name           VARCHAR(255) NOT NULL,
  description    TEXT,
  owner          VARCHAR(255),
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```sql
CREATE TABLE subsystems (
  subsystem_id   BIGSERIAL PRIMARY KEY,
  system_id      BIGINT NOT NULL REFERENCES systems(system_id) ON DELETE CASCADE,
  code           VARCHAR(50) NOT NULL,
  name           VARCHAR(255) NOT NULL,
  description    TEXT,
  tech_stack     VARCHAR(255),
  status         VARCHAR(50) DEFAULT 'active',
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(system_id, code)
);
```

```sql
CREATE TABLE projects (
  project_id     BIGSERIAL PRIMARY KEY,
  subsystem_id   BIGINT NOT NULL REFERENCES subsystems(subsystem_id) ON DELETE CASCADE,
  code           VARCHAR(50) NOT NULL,
  name           VARCHAR(255) NOT NULL,
  description    TEXT,
  lead           VARCHAR(255),
  status         VARCHAR(50) DEFAULT 'draft',
  start_date     DATE,
  end_date       DATE,
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(subsystem_id, code)
);
```

```sql
CREATE TABLE batches (
  batch_id       BIGSERIAL PRIMARY KEY,
  subsystem_id   BIGINT NOT NULL REFERENCES subsystems(subsystem_id) ON DELETE CASCADE,
  code           VARCHAR(50) NOT NULL,
  name           VARCHAR(255) NOT NULL,
  description    TEXT,
  schedule_cron  VARCHAR(120),
  last_run_at    TIMESTAMP,
  status         VARCHAR(50) DEFAULT 'idle',
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(subsystem_id, code)
);
```

---

## Design Notes

- **Codes** act as human-friendly identifiers and are unique per parent scope.
- **Timestamps** enable auditing; `updated_at` should be maintained via triggers or application logic.
- **CASCADE deletes** guarantee referential integrity, automatically removing descendants when a parent is deleted.
- **Indexes** on `system_id` / `subsystem_id` should be added depending on query patterns.
- Future extensions can add bridge tables for relationships such as batches tied to specific projects or ownership assignments.

Use this schema as the baseline for all environments so tooling, migrations, and documentation stay consistent.
