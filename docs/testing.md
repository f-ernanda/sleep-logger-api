# Testing Strategy

## Current state

- `SleepApplicationTests.contextLoads` is the template's placeholder test. It runs under the
  `unittest` Spring profile (`application-unittest.properties`), which disables Flyway and (via
  `DatabaseConfiguration`'s `@Profile("!unittest")`) skips creating a real `DataSource`/JDBC
  connection at startup — so this profile is meant for fast, DB-less context tests.

## Plan

- **Business/service logic** (e.g. average calculation, total-time-in-bed derivation): plain
  JUnit 5 + AssertJ, no Spring context — these are pure functions over data, so they don't need
  Spring at all. `SleepAverageCalculatorTest` is the first of these, including the midnight-
  crossing bed/wake time averaging case. `NewSleepLogTest` covers the `timeInBedEnd`-after-
  `timeInBedStart` invariant.
- **Repository layer**: Testcontainers, not mocks — tests run the real queries against an
  ephemeral Postgres container, since the repository's whole job is translating to/from SQL
  correctly (see `decisions.md`). `SleepLogRepositoryTest` is the first of these.
- **Controllers**: not planned as a separate test layer unless a specific routing/serialization
  concern needs it — `scripts/smoke-test.sh` below covers the HTTP contract end-to-end.

## Running tests

- `docker compose up --build` compiles and packages, but does **not** run tests — the
  `Dockerfile` builds with `./gradlew build -x test`, since Testcontainers needs Docker socket
  access that isn't available during an image build.
- Run tests explicitly, with the socket mounted so Testcontainers can start its own Postgres:
  `docker compose run --rm -T -v /var/run/docker.sock:/var/run/docker.sock sleep_api ./gradlew test`
  (`-T` disables pseudo-TTY allocation, needed when running non-interactively, e.g. from the
  pre-commit hook)

## Manual / API-level testing

`scripts/smoke-test.sh` exercises FR1–FR3 end-to-end against a running `docker compose up` stack
(create → latest → averages, plus the 404-before-any-log, empty-averages, and error-shape cases).
Requires `curl` and `jq`. Run it with the stack up:

```
docker compose up -d
./scripts/smoke-test.sh
```

It uses a timestamp-derived `userId` so reruns don't collide with data from a previous run.

## CI

`.github/workflows/ci.yml` runs on every push to `main` (plus manual dispatch): builds the image,
runs the Gradle test suite (socket-mounted, same as above), starts the full stack, waits for it to
be ready, then runs `scripts/smoke-test.sh` against it. Deliberately reuses the exact commands
documented on this page rather than a CI-specific path, so local and CI verification can't drift
apart.
