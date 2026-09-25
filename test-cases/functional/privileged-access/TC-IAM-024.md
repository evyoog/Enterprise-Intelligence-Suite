# TC-IAM-024: Privileged Access (User and Organization Administrator) — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-024 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A request with a duration of 0 or 481 minutes.

## Steps
1. Arrange: a request with a duration of 0 or 481 minutes.
2. Act: it is submitted.
3. Observe the response and the UI.

## Expected Result
The response is 400 and the UI shows the backend message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "durationOutsideAllowedRangeIsRejected"
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "shows the backend message for an invalid duration"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
