# API Contract

Draft — refine as decisions in `decisions.md` are made, and keep this in sync with the actual
controllers (`/spec-check` checks for drift).

All endpoints are scoped by a `userId` path parameter (see `decisions.md`).

## `POST /users/{userId}/sleep-logs`

Create the sleep log for last night. (FR1)

Request:

```json
{
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
  "feeling": "GOOD",
  "createdAt": "2026-07-28T07:05:00Z",
  "totalTimeInBedMinutes": 465
}
```

`timeInBedEnd` must be strictly after `timeInBedStart`, or this returns `400`.

## `GET /users/{userId}/sleep-logs/latest`

Fetch the most recent sleep log for a user. (FR2)

Response `200 OK` — same shape as the create response.

Response `404 Not Found` — the user has no sleep logs yet.

## `GET /users/{userId}/sleep-logs/averages`

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

With no logs in the last 30 days, this still returns `200 OK` with `averageTotalTimeInBedMinutes`
at `0.0`, `averageBedTime`/`averageWakeTime` as `null`, and every `feelingFrequency` value at `0`
— see `decisions.md`.

## Errors

Errors use a consistent shape — `{"error": "<message>"}` with a matching HTTP status — via a
single `@ControllerAdvice`, not Spring Boot's default error body. This covers validation
failures and malformed requests (`400`), an unsupported HTTP verb on a valid path (`405`), and
routes that don't exist at all (`404`) — not just the endpoints' own domain errors.
