# API Contract

Draft — refine as decisions in `decisions.md` are made, and keep this in sync with the actual
controllers (`/spec-check` checks for drift).

All endpoints are user-scoped; how `userId` is supplied (header, query param, or request body
field) is an open decision — see below.

## `POST /sleep-logs`

Create the sleep log for last night. (FR1)

Request:

```json
{
  "userId": 1,
  "logDate": "2026-07-28",
  "timeInBedStart": "2026-07-27T23:15:00Z",
  "timeInBedEnd": "2026-07-28T07:00:00Z",
  "feeling": "GOOD"
}
```

Response `201 Created`:

```json
{
  "id": 42,
  "userId": 1,
  "logDate": "2026-07-28",
  "timeInBedStart": "2026-07-27T23:15:00Z",
  "timeInBedEnd": "2026-07-28T07:00:00Z",
  "totalTimeInBedMinutes": 465,
  "feeling": "GOOD"
}
```

## `GET /sleep-logs/latest`

Fetch the most recent sleep log for a user. (FR2)

Response `200 OK` — same shape as the create response.

Open: response when the user has no logs yet — `404` vs `200` with an empty/null body. Pick one
and record it in `decisions.md`.

## `GET /sleep-logs/averages`

Last 30-day averages for a user. (FR3)

Response `200 OK`:

```json
{
  "rangeStart": "2026-06-28",
  "rangeEnd": "2026-07-28",
  "averageTotalTimeInBedMinutes": 452.3,
  "averageBedTime": "23:10",
  "averageWakeTime": "06:58",
  "feelingFrequency": {
    "GOOD": 20,
    "OK": 7,
    "BAD": 3
  }
}
```

## Errors

Open decision: adopt a consistent error shape (e.g. `{"error": "message"}` with a matching HTTP
status) rather than leaking Spring Boot's default error body. Record the chosen shape in
`decisions.md` once picked, and list it here.

## Open Decisions Referenced Above

- How `userId` is supplied on each request
- 404 vs empty-200 for "no logs yet"
- Error response shape
