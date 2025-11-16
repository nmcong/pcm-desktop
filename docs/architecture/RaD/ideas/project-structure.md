# Project Structure & Layering Guide

This document describes a recommended folder/package structure for PCM Desktop that aligns with Clean Architecture and
SOLID principles. Adapt it to Maven/Gradle conventions while keeping the separation of concerns intact.

---

## 1. High-Level Layout

```
pmc-desktop/
├─ docs/                 # documentation (RaD, architecture, guides)
├─ scripts/              # helper scripts (setup, format, build)
├─ data/                 # sample data, local SQLite DB, embeddings cache
├─ models/               # external ML/embedding models (if bundled)
├─ src/
│  ├─ main/
│  │   ├─ java/
│  │   │    └─ com/noteflix/pcm/
│  │   │         ├─ application/      # use cases/orchestration
│  │   │         ├─ domain/           # entities, repositories (interfaces)
│  │   │         ├─ infrastructure/   # adapters, datasource, external clients
│  │   │         ├─ ui/               # JavaFX UI (views, controllers, viewmodels)
│  │   │         └─ shared/           # utilities, config, constants
│  │   └─ resources/
│  │        ├─ db/migrations/         # SQL migrations
│  │        ├─ i18n/                  # localization files
│  │        ├─ styles/                # CSS for UI
│  │        ├─ templates/             # prompt templates, HTML snippets
│  │        └─ config/                # YAML/JSON configuration (if needed)
│  └─ test/
│       └─ java/ ... mirrors main for unit/integration tests
├─ logs/
├─ out/ or target/
└─ README, build files, etc.
```

---

## 2. Package Breakdown

### 2.1 `application` Layer (Use Cases)

Purpose: orchestrate business workflows, independent of UI/infrastructure.

Suggested sub-packages:

- `application.request` – submit/track user requests, call retrieval.
- `application.retrieval` – hybrid search orchestration.
- `application.review` – run code review pipeline.
- `application.testcase` – plan tests based on impact.
- `application.ingestion` – source scans, CHM import use cases.

Each use case class depends on interfaces defined in `domain` (
e.g., `RequestRepository`, `AstRepository`, `SemanticSearchService`). Dependency injection (manual or via framework)
provides concrete implementations from `infrastructure`.

### 2.2 `domain` Layer

Contains pure domain logic: entities, value objects, repository/service interfaces.

Sub-packages:

- `domain.system` – System, Subsystem, Project aggregates.
- `domain.code` – SourceFile, AstNode, Snapshot, Impact models.
- `domain.request` – UserRequest, AgentResponse, Feedback.
- `domain.testcase` – TestCase, Recommendation.
- `domain.review` – ReviewComment, ReviewRule.

These classes should be free of framework annotations, focusing on invariants and business rules.

### 2.3 `infrastructure` Layer

Implements adapters to external tech:

- `infrastructure.persistence` – SQLite DAOs, repository implementations (e.g., `SqliteRequestRepository`).
- `infrastructure.vectorstore` – Qdrant client wrappers.
- `infrastructure.search` – FTS5 (semantic search) service.
- `infrastructure.ingestion` – file scanners, CHM extractor.
- `infrastructure.llm` – OpenAI/Anthropic clients, provider registry.

These adapters satisfy interfaces declared in `domain`. Keep them modular so they can be swapped during testing.

### 2.4 `ui` Layer

JavaFX/Swing UI code lives here, separated by responsibility:

- `ui.pages` – major screens (AI Assistant, System Manager, Knowledge Import, Review Panel).
- `ui.components` – reusable UI controls.
- `ui.viewmodel` – ViewModel classes (MVVM pattern) that call `application` use cases.
- `ui.layout` – layout containers (e.g., MainLayer).

Controllers/ViewModels depend on `application` use cases, not infrastructure directly.

### 2.5 `shared`

Utilities that don’t belong to a specific layer:

- `shared.config` – configuration management.
- `shared.events` – event bus or domain events.
- `shared.utils` – logging helpers, JSON utilities.
- `shared.constants` – application constants (icon paths, theme settings).

Keep shared code minimal to avoid an anemic “dumping ground.”

---

## 3. Testing Structure

Under `src/test/java/com/noteflix/pcm/` mirror the main package hierarchy:

- `application/...` – use case tests (can mock repositories).
- `domain/...` – pure unit tests for entities/value objects.
- `infrastructure/...` – integration tests for DAOs/clients.
- `ui/...` – UI tests if applicable (TestFX, etc.).

Consider a separate `src/integrationTest` module if tests require heavy setup (DB/Qdrant).

---

## 4. Dependency Rules (Clean Architecture)

1. `application` depends on `domain` abstractions.
2. `domain` depends on no other layer.
3. `infrastructure` depends on `domain` (to implement interfaces) and may depend on third-party libs.
4. `ui` depends on `application` (use cases) and `domain` (view models referencing domain objects). It should avoid
   direct `infrastructure` calls.
5. `shared` should not depend on `ui`; limit cross-layer coupling.

This ensures SOLID adherence (especially Dependency Inversion) and makes testing easier.

---

## 5. Additional Recommendations

- **Configuration**: Use `src/main/resources/config/` for environment-specific settings; inject via `shared.config`.
- **Migrations**: Keep SQLite migration scripts in `src/main/resources/db/migrations/` (e.g., `V0001__init.sql`).
- **Scripts**: Place automation scripts (setup, backup) in `scripts/` and document them in `README`.
- **Docs**: Continue storing RaD descriptions under `docs/RaD/`, architecture notes under `docs/architecture/`, etc.
- **Models**: If shipping local LLM/embedding models, store them under `models/` with README describing usage.

Adhering to this structure keeps the codebase maintainable, supports modular testing, and aligns with Clean Architecture
principles.
