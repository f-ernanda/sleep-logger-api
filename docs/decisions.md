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

## Open decisions carried over from other docs

These are flagged in `requirements.md` / `data-model.md` / `api.md` and still need an entry here
before (or as) they're implemented:

- How `userId` is supplied on requests (header / query param / body field)
- Total time in bed: stored column vs. computed on read
- Feeling storage: `VARCHAR` + `CHECK` vs. native Postgres `ENUM`
- `log_date` semantics when the sleep interval spans midnight
- Response for "no log yet" on `GET /sleep-logs/latest` (404 vs empty 200)
- Error response shape
- Repository test strategy: mock `JdbcTemplate` vs. a real Postgres integration test
  (e.g. Testcontainers)
