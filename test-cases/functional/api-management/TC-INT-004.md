# TC-INT-004: Rate limits answer 429 with Retry-After

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-004 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Limits of 2 per key, 3 per user, 1 per IP (test values; defaults 120 / 300 / 60 per minute).

## Steps
1. Send requests from one IP without signing in.
2. Send requests as a signed-in user, then with a key.

## Expected Result
Every response carries `X-RateLimit-Limit` and `X-RateLimit-Remaining`. The request over the limit gets 429, code `RATE_LIMITED`, and `Retry-After` = seconds to the next minute. Another IP, user or key has its own count.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/RateLimitFilterTest.java`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
