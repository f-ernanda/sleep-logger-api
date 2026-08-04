# Sleep Logger API

Take-home backend interview assignment: a REST API for logging sleep (Kotlin + Spring Boot +
PostgreSQL + Flyway), no auth but user-aware. Full assignment brief: [README.md](README.md).

## Stack

- Kotlin, Spring Boot 2.7, Gradle (wrapper is committed — no local Gradle/JDK needed)
- PostgreSQL 13, Flyway migrations under `sleep/src/main/resources/db/migration`
- Persistence via plain JDBC (`NamedParameterJdbcTemplate`) — no Spring Data JPA
- Everything runs in Docker

## Run / build / test

- `docker compose up --build` — build the image and start Postgres + the API
  (API on `localhost:8080`, Postgres on `localhost:5432`)
- `docker compose up --build -d` then `docker compose logs -f sleep_api` — same, detached + follow
- `docker compose down` — stop and remove containers
- Tests do **not** run during `docker compose up --build` (the Dockerfile runs
  `./gradlew build -x test`, since Testcontainers-based tests need Docker socket access that
  isn't available during an image build). Run them explicitly:
  `docker compose run --rm -T -v /var/run/docker.sock:/var/run/docker.sock sleep_api ./gradlew test`
  (`-T` disables pseudo-TTY allocation — needed when this runs non-interactively, e.g. from a
  git hook)
- Live-reload (`bootRun --continuous` + devtools) was evaluated and dropped — not worth the
  complexity for this project's size. Rebuild-on-change (`docker compose up --build`) is the
  workflow.
- CI (`.github/workflows/ci.yml`) runs on every push to `main`: build, test, start the stack, then
  `scripts/smoke-test.sh` — the exact same commands documented here, not a separate CI-only path.

## Docs — spec-driven workflow

This project follows a lightweight spec-first workflow. Before implementing or changing behavior,
check:

- `docs/requirements.md` — what to build: functional/non-functional requirements, acceptance
  criteria
- `docs/data-model.md` — DB schema and migration plan
- `docs/api.md` — REST contract: endpoints, request/response shapes, status codes
- `docs/testing.md` — test strategy, and how to run tests / the Postman collection
- `docs/decisions.md` — why non-obvious choices were made; log new ones with `/decision`

Use `/spec-check` to check the current implementation against the docs above.

## Conventions

- Package root: `com.noom.interview.fullstack.sleep`
- Migrations: `Vx.y__description.sql` under `sleep/src/main/resources/db/migration`, continuing
  from the existing `V1.0__test_db_reachable.sql`
- Trunk-based: commit directly to `main` in small, atomic, conventional commits — no feature
  branches or PRs (deviates from the assignment's README; see `docs/decisions.md`). Each commit
  should leave `main` buildable and passing tests, since there's no PR gate to catch it after
  the fact.
- A `pre-commit` hook (`.githooks/pre-commit`) enforces that: it builds `sleep_api` and runs its
  tests (socket-mounted, per above) when staged changes touch `sleep/`. Wired up via
  `git config core.hooksPath .githooks` — re-run that if cloning fresh.
