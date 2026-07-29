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

**Context:** The assignment brief ([README.md](../README.md)) asks for changes to be "merged in
as PRs to the repository." This project is also adopting trunk-based development to keep the
solo take-home workflow lightweight.

**Decision:** Work directly on `main` with small, atomic, [conventional
commits](https://www.conventionalcommits.org/) instead of opening a PR per change. No feature
branches, no long-lived branches.

**Consequences:** This deviates from the README's literal instruction — worth calling out to a
reviewer rather than leaving it to look like an oversight. Since there's no PR description to
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
