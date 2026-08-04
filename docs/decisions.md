# Decisions Log

A running log of non-obvious architectural choices and why they were made. Append-only — don't
edit or renumber past entries; if a decision is reversed, add a new entry that supersedes it and
say so.

New entries are added with `/decision "<title>"`, using this template:

```
## YYYY-MM-DD — <Title>

**Context:** what prompted this decision.

**Decision:** what was decided.

**Consequences:** what this implies or rules out.
```

---

## 2026-07-28 — Adopted a lightweight spec-driven doc structure

**Context:** This is a small take-home backend assignment. We want architectural decisions to be
explicit and reviewable without building process overhead the project doesn't need.

**Decision:** Specs live under `docs/` (`requirements.md`, `data-model.md`, `api.md`,
`testing.md`, this file), decisions are logged as dated entries in a single running file rather
than one-file-per-decision, and `.claude/commands/` holds two small slash commands
(`/spec-check`, `/decision`) to keep the docs and the code honest as work progresses.

**Consequences:** Lower ceremony than a full ADR directory; relies on this file staying short and
dated entries staying skimmable. If the project grew significantly, per-decision files would be
worth revisiting.

---

## 2026-07-29 — Direct commits to main instead of a PR-based workflow

**Context:** The assignment brief ([assignment.md](assignment.md)) asks for changes to be "merged
in as PRs to the repository." This project is also adopting trunk-based development to keep the
solo take-home workflow lightweight.

**Decision:** Work directly on `main` with small, atomic, [conventional
commits](https://www.conventionalcommits.org/) instead of opening a PR per change. No feature
branches, no long-lived branches.

**Consequences:** This deviates from the assignment brief's literal instruction — worth calling
out to a reviewer rather than leaving it to look like an oversight. Since there's no PR description to
carry rationale, commit messages have to do that job (see `CLAUDE.md` conventions). Since there's
no PR review gate, each commit needs to leave `main` in a buildable, passing-tests state on its
own — review happens before committing, not after.

---

## 2026-07-29 — Local pre-commit hook as the build/test gate

**Context:** With no PR review under the direct-commits-to-main workflow (see the decision
above), nothing currently stops a broken commit from landing on `main`.

**Decision:** Added a git `pre-commit` hook (`.githooks/pre-commit`, wired up via
`git config core.hooksPath .githooks` since `.git/hooks` isn't tracked). It runs
`docker compose build sleep_api` — which runs `./gradlew build`, i.e. compiles and runs unit
tests — but only when the staged changes touch `sleep/`, so docs-only commits stay fast.

**Consequences:** This is the enforcement mechanism for "every commit leaves `main` buildable and
passing." It only runs locally, via a git config that isn't itself versioned — re-run
`git config core.hooksPath .githooks` after a fresh clone.

---

## 2026-07-29 — `userId` passed as a path parameter

**Context:** All three endpoints are user-scoped with no auth. A header, query param, or body
field would all work for `POST`, but `GET` requests don't have a body, so a body field would mean
two different mechanisms depending on the verb.

**Decision:** `userId` is a path parameter on every endpoint: `/users/{userId}/sleep-logs`,
`/users/{userId}/sleep-logs/latest`, `/users/{userId}/sleep-logs/averages`.

**Consequences:** Uniform across `GET`/`POST`, visible and curl-able without custom headers.
Every route now carries `{userId}`, which is fine at this scale; a future real-auth setup would
replace the path segment with a token-derived identity rather than trusting a client-supplied one.

---

## 2026-07-29 — Total time in bed is always derived, never stored

**Context:** Storing a `total_time_in_bed_minutes` column (as originally drafted in
`data-model.md`) creates a second source of truth that could drift from `time_in_bed_start` /
`time_in_bed_end`.

**Decision:** No stored column. Total time in bed is computed from the interval wherever it's
needed — the single-log response and the FR3 averages query.

**Consequences:** One source of truth, no drift risk, at the cost of computing it on every read
instead of reading a column — negligible at this data volume.

---

## 2026-07-30 — Feeling stored as `VARCHAR` + `CHECK`, not a native Postgres `ENUM`

**Context:** Persistence is plain JDBC (`NamedParameterJdbcTemplate`), not JPA — there's no ORM
layer to abstract a native enum's mapping.

**Decision:** `feeling VARCHAR(4) CHECK (feeling IN ('BAD', 'OK', 'GOOD'))`.

**Consequences:** No casting (`::feeling_enum`) needed in queries. Slightly less storage-efficient
and less self-documenting via `psql \dT` than a native enum, which isn't a real cost here.

---

## 2026-07-30 — `logDate` is an independent field, not derived from the interval

**Context:** The sleep interval can span midnight, so "the date of the sleep" doesn't map cleanly
to either the start or end timestamp.

**Decision:** `logDate` is supplied by the client (or defaults to the server's current date),
representing "what day this entry is for." It is not validated or derived from
`timeInBedStart`/`timeInBedEnd`.

**Consequences:** Matches the requirement's own wording ("the date of the sleep (today)").
Nothing stops a client from sending a `logDate` inconsistent with the interval — accepted, since
validating that isn't asked for and adds complexity for a case out of scope.

---

## 2026-07-30 — `404` for "no sleep log yet" on the latest-log endpoint

**Context:** `GET /users/{userId}/sleep-logs/latest` needs defined behavior for a user with no
logs yet.

**Decision:** Return `404 Not Found`, not `200` with an empty/null body.

**Consequences:** "Latest sleep log" is a singular resource; its absence is a Not Found, not a
Found-but-empty. Clients need a status check rather than a truthy check on a field — the more
idiomatic and testable choice.

---

## 2026-07-30 — Consistent JSON error shape via a single exception handler

**Context:** Spring Boot's default error body is verbose and inconsistent with a clean REST
contract.

**Decision:** A single `@ControllerAdvice` maps domain exceptions to `{"error": "<message>"}`
with a matching HTTP status, replacing the default error body everywhere.

**Consequences:** One extra class, but every error response is predictable and easy to assert on
in tests — no per-endpoint error handling needed.

---

## 2026-07-30 — Repository tests use Testcontainers, not mocks

**Context:** The assignment asks for "unit tests for the repository." Mocking
`NamedParameterJdbcTemplate` only proves the mock was called correctly, not that the SQL itself is
correct — which is the actual risk in a repository (typos in column names, wrong types, off-by-one
errors in the 30-day range query).

**Decision:** Repository tests run against a real, ephemeral Postgres container via Testcontainers
rather than mocking the JDBC template.

**Consequences:** Higher confidence, slower tests (a container boot per test class). More
importantly: the `Dockerfile`'s `RUN ./gradlew build` step builds the image with no Docker socket
access, so it can't run these tests. Test execution needs to move out of that step into a separate
one with the socket mounted (e.g. `docker compose run` with
`/var/run/docker.sock:/var/run/docker.sock`) — to be wired up once the repository layer exists.
Until then, `CLAUDE.md`'s "tests run as part of the image build" claim and the pre-commit hook
both describe the current state, not the target one, and will need updating at that point.

---

## 2026-07-30 — Averages with zero logs in range return `200` with zeroed values, not `404`

**Context:** `GET /users/{userId}/sleep-logs/averages` needs defined behavior when a user has no
sleep logs in the last 30 days — unlike the single-log `latest` endpoint (see the `404` decision
above), which is a different kind of endpoint.

**Decision:** Return `200 OK` with `averageTotalTimeInBedMinutes: 0.0`, `averageBedTime`/
`averageWakeTime: null`, and `feelingFrequency` at `0` for every value — not `404`.

**Consequences:** Averages is a report over a range, not a lookup of a single resource; "no data
in this range" is a valid (if empty) answer to that report, not a missing resource. This is why
`averageTotalTimeInBedMinutes` defaults to `0.0` (a sensible empty-duration value) while
`averageBedTime`/`averageWakeTime` default to `null` instead of `00:00` (a clock time doesn't have
a sensible zero value that isn't misleading).

---

## 2026-07-31 — Controllers return domain objects directly, no Response DTOs

**Context:** `SleepLog` and `SleepAverages` already match the documented API response shape
field-for-field — a separate `SleepLogResponse`/`SleepAveragesResponse` DTO layer would just
duplicate the same fields with a mapping function in between. `CreateSleepLogRequest` is kept as
its own type, since the request genuinely has a different shape (no `id`, no `userId` — that
comes from the path).

**Decision:** `SleepLogController` returns `SleepLog`/`SleepAverages` directly from
`GET`/`POST` responses instead of mapping to dedicated Response DTOs.

**Consequences:** Less code, no mapping boilerplate for an assignment this size. The real cost
showed up immediately: `SleepLog.createdAt` — a field `api.md` never specified — started
appearing in every response simply because it exists on the domain type. Resolved by updating
`api.md` to document it rather than suppressing it with `@JsonIgnore`, since it's harmless,
useful metadata. This is the general risk of skipping a DTO buffer: any future field added to the
domain model for internal reasons is exposed over the API by default unless deliberately hidden.

---

## 2026-08-03 — A curl-based smoke-test script instead of a Postman collection

**Context:** The assignment accepts either "a simple script or Postman collection." All manual
verification throughout this project has already been plain `curl` calls, requiring no additional
software to install or open to review.

**Decision:** `scripts/smoke-test.sh` — a bash script using `curl` and `jq` — exercises FR1–FR3
end-to-end against a running `docker compose up` stack, rather than a Postman collection.

**Consequences:** Reviewable as plain text in a diff, no Postman installation needed to check it.
Adds a `jq` dependency for whoever runs it (checked for explicitly at the top of the script, with
a clear error if missing). Uses a timestamp-derived `userId` per run so it's safe to re-run
without manual cleanup, at the cost of not testing against a fixed, inspectable fixture user.

---

## 2026-08-03 — Reject sleep logs where `timeInBedEnd` isn't after `timeInBedStart`

**Context:** A full review pass found that nothing stopped a client from submitting a reversed
or zero-length interval, which would silently persist a negative or zero `totalTimeInBedMinutes`.
Unlike the `logDate`-vs-interval inconsistency (already an accepted, documented gap), this one
had never been discussed and produces a nonsensical value, not just an unvalidated one.

**Decision:** `NewSleepLog`'s `init` block rejects the interval (via `require`) unless
`timeInBedEnd` is strictly after `timeInBedStart`; `RestExceptionHandler` maps
`IllegalArgumentException` to `400`.

**Consequences:** The check lives on `NewSleepLog` itself (not just the controller/DTO), so the
invariant holds regardless of entry point. Equal start/end is rejected too — a zero-length sleep
isn't meaningful.

---

## 2026-08-03 — Closed two gaps in the consistent-error-shape guarantee

**Context:** The same review pass found the original `@ControllerAdvice` (see the "Consistent
JSON error shape" decision above) didn't actually cover everything it implied: an unsupported
HTTP verb on a valid path was falling through to the generic `Exception` handler as `500` instead
of `405`, and a completely unmapped route bypassed the advice entirely, returning Spring Boot's
default whitebox error body instead of `{"error": "..."}`. Both confirmed by hitting them
directly before and after the fix.

**Decision:** Added a specific handler for `HttpRequestMethodNotSupportedException` → `405`, and
set `spring.mvc.throw-exception-if-no-handler-found=true` plus
`spring.web.resources.add-mappings=false` so unmapped routes actually throw
`NoHandlerFoundException` (handled → `404`) instead of being silently answered by the static
resource handler first.

**Consequences:** Both properties are required together — `throw-exception-if-no-handler-found`
alone has no effect while the default static-resource mapping is still active, since that handler
answers unmapped paths before `DispatcherServlet` ever gets a chance to throw. No functional loss
from disabling static resource mapping, since this is a pure JSON API with no static content.

---

## 2026-08-03 — Added CI now that a remote exists

**Context:** GitHub Actions needs a GitHub-hosted repo to run against, which didn't exist until
now. With no PR workflow (see the direct-commits-to-main decision), CI's only meaningful trigger
is a push to `main`.

**Decision:** `.github/workflows/ci.yml` triggers on push to `main` (plus manual dispatch) and
runs the exact same commands documented in `CLAUDE.md`/`testing.md`: build the image, run the
Gradle test suite with the Docker socket mounted, start the full stack, wait for it to be ready,
then run `scripts/smoke-test.sh` against it.

**Consequences:** No separate CI-only script or command path to maintain — local and CI
verification use identical commands, so they can't silently drift apart. The wait-for-ready loop
polls `GET /users/0/sleep-logs/averages` (always `200`, even with no data) as a stand-in health
check, since there's no dedicated health endpoint. No branch protection is configured — CI is a
signal, not an enforced gate, consistent with the lightweight scope of this project.

---

## 2026-08-03 — README.md became a normal project README; the assignment brief moved to docs/assignment.md

**Context:** The repo root `README.md` was still Noom's original assignment brief. With a remote
now in place and the project functionally complete, a reviewer landing on the repo would benefit
from a normal front door (what this is, how to run it, where things are) rather than the
interview instructions.

**Decision:** `README.md` is now a standard project README (features, quickstart, API summary,
links to `docs/`). The original assignment brief moved to `docs/assignment.md`, unedited apart
from a one-line provenance note at the top pointing back to this decision.

**Consequences:** Every prior reference to "the assignment brief ([README.md](../README.md))" —
in `CLAUDE.md`, `requirements.md`, and the direct-commits-to-main decision above — had to be
repointed to `assignment.md`; grepped for stragglers after making the change rather than trusting
memory of where they all were.

---

## 2026-08-03 — Fixed a jq-version-dependent flake in the smoke test

**Context:** The first real CI run failed two checks that pass locally:
`averageTotalTimeInBedMinutes` compared as the string `"0"`/`"465"` locally (`jq` 1.6, which
normalizes `0.0` → `0` when reformatting a number for raw output) but as `"0.0"`/`"465.0"` on the
GitHub Actions runner (`jq` 1.7+, which preserves the literal decimal form instead). The API's
JSON response was correct and unchanged in both cases — the script's assertion was comparing
`jq`'s reformatted string instead of the actual numeric value.

**Decision:** Those two checks now use `jq`'s own numeric `==` (e.g.
`.averageTotalTimeInBedMinutes == 0`, asserting the resulting `"true"`) instead of comparing
`jq -r`'s raw string output against a hardcoded literal.

**Consequences:** The comparison happens inside `jq` before any number-to-string formatting, so
it's correct regardless of which `jq` version runs it. `totalTimeInBedMinutes` (a `Long`, not a
`Double`) was never affected — plain integers don't have this ambiguity.
