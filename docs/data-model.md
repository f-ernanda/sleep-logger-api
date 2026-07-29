# Data Model

## Existing baseline

`sleep/src/main/resources/db/migration/V1.0__test_db_reachable.sql` is the template's placeholder
migration (`SELECT 1;`) — it just proves Flyway/Postgres are wired up. Our schema starts at
`V1.1__...` onward, following the same `Vx.y__description.sql` naming.

## Proposed schema (draft — not yet migrated)

One table is enough for FR1–FR3; averages are computed by querying this table, not by a separate
aggregate table.

```sql
CREATE TABLE sleep_log (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    log_date            DATE         NOT NULL,      -- the "date of the sleep" from FR1
    time_in_bed_start   TIMESTAMPTZ  NOT NULL,
    time_in_bed_end     TIMESTAMPTZ  NOT NULL,
    total_time_in_bed_minutes INT    NOT NULL,       -- derived, see decisions.md
    feeling             VARCHAR(4)   NOT NULL CHECK (feeling IN ('BAD', 'OK', 'GOOD')),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_sleep_log_user_date ON sleep_log (user_id, log_date DESC);
```

The `(user_id, log_date DESC)` index covers both access patterns: "latest log for user" (FR2) and
"logs for user in the last 30 days" (FR3).

## Open questions (resolve via `/decision`, then update this file)

1. **Feeling representation** — `VARCHAR` + `CHECK` (above) vs. a native Postgres `ENUM` type.
2. **Total time in bed** — stored column (above) vs. computed on read from the interval.
3. **`log_date` semantics** — how it relates to `time_in_bed_start`/`end` when the interval spans
   midnight (e.g., is `log_date` the wake-up date or the date the user went to bed?).
4. **User identity** — `user_id` is a bare `BIGINT` here since there's no auth/users table in
   scope; confirm nothing else is needed (e.g., no FK target table).

Each of these should get a `docs/decisions.md` entry before (or as) it's implemented, and this file
should be updated to match once decided.
