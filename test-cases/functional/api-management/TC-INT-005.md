# TC-INT-005: Every endpoint is also served under /v1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-005 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional, Security |
| Automated | Yes |

## Preconditions
An active key.

## Steps
1. Call `/api/v1/me/api-keys` with the key, and without credentials.
2. Call `/api/v1/admin/events` as a user without `MANAGE_INTEGRATIONS`.

## Expected Result
Same result as the unversioned path; 401 without credentials; 403 for the admin path; every response has `API-Version: 1`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `everyEndpointIsAlsoServedUnderV1`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
