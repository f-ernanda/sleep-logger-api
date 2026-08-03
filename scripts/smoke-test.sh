#!/usr/bin/env bash
# Exercises FR1-FR3 end-to-end against a running `docker compose up` stack.
# Requires: curl, jq.
set -euo pipefail

if ! command -v jq &> /dev/null; then
  echo "jq is required to run this script (https://jqlang.github.io/jq/)" >&2
  exit 1
fi

BASE_URL="${BASE_URL:-http://localhost:8080}"
USER_ID="$(date +%s)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

pass=0
fail=0

check_status() {
  local description="$1" expected="$2" actual="$3"
  if [[ "$actual" == "$expected" ]]; then
    echo "PASS: $description (HTTP $actual)"
    pass=$((pass + 1))
  else
    echo "FAIL: $description (expected HTTP $expected, got $actual)"
    fail=$((fail + 1))
  fi
}

check_value() {
  local description="$1" expected="$2" actual="$3"
  if [[ "$actual" == "$expected" ]]; then
    echo "PASS: $description ($actual)"
    pass=$((pass + 1))
  else
    echo "FAIL: $description (expected $expected, got $actual)"
    fail=$((fail + 1))
  fi
}

echo "Running smoke tests against $BASE_URL for user $USER_ID"
echo

# FR2: no logs yet -> 404
status=$(curl -sS -o "$TMP_DIR/latest_before.json" -w "%{http_code}" \
  "$BASE_URL/users/$USER_ID/sleep-logs/latest")
check_status "GET latest before any log" 404 "$status"

# FR3: averages with zero logs -> 200, zeroed
status=$(curl -sS -o "$TMP_DIR/averages_empty.json" -w "%{http_code}" \
  "$BASE_URL/users/$USER_ID/sleep-logs/averages")
check_status "GET averages before any log" 200 "$status"
check_value "averages with no logs has zero average minutes" "0" \
  "$(jq -r '.averageTotalTimeInBedMinutes' "$TMP_DIR/averages_empty.json")"

# FR1: create a log -> 201, total time in bed computed server-side
status=$(curl -sS -o "$TMP_DIR/create.json" -w "%{http_code}" -X POST \
  "$BASE_URL/users/$USER_ID/sleep-logs" \
  -H "Content-Type: application/json" \
  -d '{"logDate":"2026-07-28","timeInBedStart":"2026-07-27T23:15:00Z","timeInBedEnd":"2026-07-28T07:00:00Z","feeling":"GOOD"}')
check_status "POST creates a sleep log" 201 "$status"
created_id=$(jq -r '.id' "$TMP_DIR/create.json")
check_value "created log has computed totalTimeInBedMinutes" "465" \
  "$(jq -r '.totalTimeInBedMinutes' "$TMP_DIR/create.json")"

# FR2: latest now returns what was just created
status=$(curl -sS -o "$TMP_DIR/latest_after.json" -w "%{http_code}" \
  "$BASE_URL/users/$USER_ID/sleep-logs/latest")
check_status "GET latest after creating" 200 "$status"
check_value "latest matches the created log" "$created_id" \
  "$(jq -r '.id' "$TMP_DIR/latest_after.json")"

# FR3: averages now reflect the one log
status=$(curl -sS -o "$TMP_DIR/averages.json" -w "%{http_code}" \
  "$BASE_URL/users/$USER_ID/sleep-logs/averages")
check_status "GET averages after creating" 200 "$status"
check_value "averages reflect the one log" "465" \
  "$(jq -r '.averageTotalTimeInBedMinutes' "$TMP_DIR/averages.json")"

# Error cases: consistent {"error": "..."} shape
status=$(curl -sS -o "$TMP_DIR/bad_feeling.json" -w "%{http_code}" -X POST \
  "$BASE_URL/users/$USER_ID/sleep-logs" \
  -H "Content-Type: application/json" \
  -d '{"logDate":"2026-07-28","timeInBedStart":"2026-07-27T23:15:00Z","timeInBedEnd":"2026-07-28T07:00:00Z","feeling":"GREAT"}')
check_status "POST with invalid feeling" 400 "$status"
check_value "error response has the documented shape" "true" \
  "$(jq 'has("error")' "$TMP_DIR/bad_feeling.json")"

status=$(curl -sS -o /dev/null -w "%{http_code}" \
  "$BASE_URL/users/not-a-number/sleep-logs/latest")
check_status "GET with non-numeric userId" 400 "$status"

echo
echo "Results: $pass passed, $fail failed"
[[ "$fail" -eq 0 ]]
