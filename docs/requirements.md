# Requirements

Distilled from the assignment brief in [README.md](../README.md) into a checkable spec. This file
is the source of truth for "done" — `docs/api.md` and `docs/data-model.md` are the concrete
translations of it, and `/spec-check` compares the implementation against it.

## Functional Requirements

### FR1 — Create the sleep log for last night

- Input: date of the sleep, the time-in-bed interval, and how the user felt in the morning
  (`BAD` | `OK` | `GOOD`).
- Total time in bed is derived from the interval (see [decisions.md](decisions.md) for whether
  it's computed server-side or accepted from the client).
- Associated with a user. No auth, but the API must be user-aware (a `userId` has to appear
  somewhere in the request).
- Acceptance: a valid request persists one sleep log and returns it (or its id).

### FR2 — Fetch last night's sleep

- Returns the most recent sleep log for a given user.
- Acceptance: returns the latest log when one exists; behavior when none exists yet is an open
  decision (see decisions.md).

### FR3 — Last 30-day averages

- Response includes:
  - the date range the averages cover
  - average total time in bed
  - average bed time and average wake-up time
  - frequency of each feeling (`BAD` / `OK` / `GOOD`) over the window
- The user can switch back to the single-log view (FR2) — this just means both endpoints coexist;
  no server-side view state is implied.
- Acceptance: averages are computed only over that user's logs from the last 30 days.

## Non-Functional Constraints (fixed by the assignment template)

- Kotlin + Spring Boot, PostgreSQL, Flyway for migrations.
- Persistence is plain JDBC (`NamedParameterJdbcTemplate`) — no Spring Data JPA in the starter.
- No auth/authz required.
- No need to tune server, DB, or build defaults — the interview is scored on the code, not on
  infra polish.
- Runs fully in Docker (established separately); no local JDK/Gradle expected.

## Deliverables Checklist

- [ ] Flyway migration(s) creating the sleep log table
- [ ] REST endpoints covering FR1–FR3
- [ ] Unit tests for the repository layer and any business/service logic
- [ ] A Postman collection or script exercising the API
- [ ] Work delivered as reviewable PRs with a meaningful commit history

## Out of Scope

- Auth/authz
- Editing or deleting past sleep logs (not requested)
- Multi-timezone support beyond whatever is explicitly decided in `decisions.md`
