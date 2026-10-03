# TC-INT-002: A key acts as its owner

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-002 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active key.

## Steps
1. Call `GET /api/me/api-keys` with `X-API-Key`.

## Expected Result
200 with the owner's keys; the key's last-used time and request count increase.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `theFullKeyIsShownOnceAndTheKeyActsAsItsOwner`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
