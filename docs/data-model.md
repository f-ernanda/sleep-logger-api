# Data Model

## Existing baseline

`sleep/src/main/resources/db/migration/V1.0__test_db_reachable.sql` is the template's placeholder
migration (`SELECT 1;`) — it just proves Flyway/Postgres are wired up. Our schema starts at
`V1.1__...` onward, following the same `Vx.y__description.sql` naming.

## Schema

One table is enough for FR1–FR3; averages are computed by querying this table, not by a separate
aggregate table.

```sql
CREATE TABLE sleep_log (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    log_date            DATE         NOT NULL,      -- what day this entry is for; independent of
                                                      -- the interval below, see decisions.md
    time_in_bed_start   TIMESTAMPTZ  NOT NULL,
    time_in_bed_end     TIMESTAMPTZ  NOT NULL,
    feeling             VARCHAR(4)   NOT NULL CHECK (feeling IN ('BAD', 'OK', 'GOOD')),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_sleep_log_user_date ON sleep_log (user_id, log_date DESC);
```

The `(user_id, log_date DESC)` index covers both access patterns: "latest log for user" (FR2) and
"logs for user in the last 30 days" (FR3).

Notes (reasoning behind each is in `docs/decisions.md`):

- **No `total_time_in_bed` column.** It's always derived from `time_in_bed_end - time_in_bed_start`
  — in the single-log response and in the averages query — so there's one source of truth instead
  of a value that could drift from the interval.
- **`feeling` is `VARCHAR` + `CHECK`, not a native Postgres `ENUM`.** Persistence here is plain
  JDBC, not JPA, so there's no ORM layer to abstract an enum's mapping — a `CHECK` constraint gets
  the same safety without query-side casting.
- **`user_id` is a bare `BIGINT`.** No auth/users table is in scope, so there's nothing to
  foreign-key against.
