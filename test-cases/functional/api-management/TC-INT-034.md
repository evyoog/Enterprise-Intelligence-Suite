# TC-INT-034: Deprecated endpoints carry Deprecation, Sunset and Link headers

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-034 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A deprecation configured: `app.api.deprecations[0]` with a path prefix, a sunset date and a successor.

## Steps
1. Call an endpoint under the prefix.
2. Call an endpoint outside it, and one whose path only starts with the same letters (for example `/api/foobar` for the prefix `/api/foo`).

## Expected Result
Step 1: `Deprecation: true`, `Sunset: <date as an HTTP date>` and `Link: <successor>; rel="successor-version"`. Step 2: none of these headers.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiDeprecationFilterTest.java`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
