---
description: Check the current implementation against docs/requirements.md and docs/api.md, report drift
---

Compare the current state of the `sleep/` implementation against `docs/requirements.md` and
`docs/api.md`.

For each functional requirement (FR1, FR2, FR3) and each documented endpoint:

- State whether it's implemented, partially implemented, or missing.
- Flag any behavior that contradicts the documented contract — status codes, field names/types,
  whether a value is computed vs. accepted from the client, etc.

If the code does something reasonable that isn't documented, call it out as a candidate for a new
`docs/decisions.md` entry rather than silently ignoring it or editing the docs yourself.

Do not modify code or docs — this is a read-only report. Present findings as a concise checklist
grouped by requirement ID.
