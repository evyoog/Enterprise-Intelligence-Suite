# TC-IAM-044: Privileged Access — AC-15

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-044 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved PLATFORM-scope grant past its expiry.

## Steps
1. Arrange: an approved PLATFORM-scope grant past its expiry.
2. Act: the platform administrator opens the page.
3. Observe the response and the UI.

## Expected Result
It is not listed as active.

## Automated coverage
- `backend/…/PrivilegedAccessServiceTest.java` — "expiredPlatformGrantsAreNotListedAsActive"
- `frontend/src/pages/admin/AdminPrivilegedAccessPage.test.tsx` — "has no detectable a11y violations"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
