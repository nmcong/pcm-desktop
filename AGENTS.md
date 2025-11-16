# Repository Guidelines

This file describes how to work in the `pcm-desktop` repository. It applies to all source, tests, scripts, and
documentation.

## Project Structure & Modules

- Core app code lives in `src/main/java` with resources in `src/main/resources`.
- Tests live in `src/test/java` under the `com.noteflix.pcm` packages.
- Helper scripts are in `scripts/` (build, run, format, setup) and reusable binaries in `lib/`.
- Generated build artifacts go to `out/` and `target/`; do not commit anything from these folders.
- Documentation and examples are in `docs/`, `docs-old/`, and `examples/`.

## Build, Test, and Run

- `mvn clean test` – Compile and run the JUnit test suite.
- `mvn clean package` – Build the application jar into `target/`.
- `scripts/build.sh` / `scripts/build.bat` – Project’s canonical build (Java 21 enforced).
- `scripts/run.sh` / `scripts/run.bat` – Run the desktop app (see `--text`, `--api-demo`, `--sso-demo`).
- `scripts/setup.sh` / `scripts/setup.bat` – One-time environment and embeddings setup, if required.

## Coding Style & Naming

- Java 21, standard 4-space indentation; prefer expressive names over abbreviations.
- Use package structure `com.noteflix.pcm.<feature>` consistent with existing modules.
- Run `scripts/format.sh` / `scripts/format.bat` before pushing; this uses Google Java Format.
- Keep UI, domain, persistence, and integration code separated as in existing packages.

## Testing Guidelines

- Tests use JUnit 5, Mockito, and AssertJ; follow the existing assertion style.
- Name test classes with the `*Test` suffix and keep them parallel to `src/main/java` packages.
- Ensure all tests pass with `mvn test` or via the platform scripts before opening a PR.

## Commit & PR Practices

- Follow conventional, descriptive messages (e.g., `feat: add rag chunking config`, `fix: handle null embeddings`).
- Keep commits logically scoped; avoid mixing formatting-only and behavior changes.
- PRs should include: short summary, motivation/issue link, testing notes (`mvn test` / script run), and screenshots for
  UI changes.

## Agent-Specific Notes

- Prefer minimal, targeted edits aligned with existing patterns.
- Do not modify scripts in `scripts/` or artifacts in `lib/` unless explicitly requested.
- When in doubt, mirror the structure and naming used in nearby classes and tests.

