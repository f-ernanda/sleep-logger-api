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
- **Repository layer**: open decision (see `decisions.md`) between two approaches —
  1. Mock `NamedParameterJdbcTemplate` with Mockito and assert the SQL/params passed, or
  2. Run the real queries against Postgres (e.g. via Testcontainers) for higher confidence at the
     cost of slower tests.
     Whichever is chosen, record it in `decisions.md` and update this section.
- **Controllers**: not planned as a separate test layer unless a specific routing/serialization
  concern needs it — the Postman collection below covers the HTTP contract end-to-end.

## Running tests

- Full build (includes tests, matches what the Dockerfile does): `docker compose up --build`
- Tests only, without rebuilding the run image: `docker compose run --rm sleep_api ./gradlew test`

## Manual / API-level testing

A Postman collection or an equivalent curl script (still to be decided — whichever is faster to
keep in sync) will live at the repo root (e.g. `postman/sleep-api.postman_collection.json`) and
exercise FR1–FR3 end-to-end against the running `docker compose up` stack. This gets created once
the endpoints in `api.md` are implemented, not before.
