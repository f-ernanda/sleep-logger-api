# Testing Strategy

## Current state

- `SleepApplicationTests.contextLoads` is the template's placeholder test. It runs under the
  `unittest` Spring profile (`application-unittest.properties`), which disables Flyway and (via
  `DatabaseConfiguration`'s `@Profile("!unittest")`) skips creating a real `DataSource`/JDBC
  connection at startup — so this profile is meant for fast, DB-less context tests.

## Plan

- **Business/service logic** (e.g. average calculation, total-time-in-bed derivation): plain
  JUnit 5 + AssertJ, no Spring context — these are pure functions over data, so they don't need
  Spring at all.
- **Repository layer**: Testcontainers, not mocks — tests run the real queries against an
  ephemeral Postgres container, since the repository's whole job is translating to/from SQL
  correctly (see `decisions.md`). `SleepLogRepositoryTest` is the first of these.
- **Controllers**: not planned as a separate test layer unless a specific routing/serialization
  concern needs it — the Postman collection below covers the HTTP contract end-to-end.

## Running tests

- `docker compose up --build` compiles and packages, but does **not** run tests — the
  `Dockerfile` builds with `./gradlew build -x test`, since Testcontainers needs Docker socket
  access that isn't available during an image build.
- Run tests explicitly, with the socket mounted so Testcontainers can start its own Postgres:
  `docker compose run --rm -v /var/run/docker.sock:/var/run/docker.sock sleep_api ./gradlew test`

## Manual / API-level testing

A Postman collection or an equivalent curl script (still to be decided — whichever is faster to
keep in sync) will live at the repo root (e.g. `postman/sleep-api.postman_collection.json`) and
exercise FR1–FR3 end-to-end against the running `docker compose up` stack. This gets created once
the endpoints in `api.md` are implemented, not before.
