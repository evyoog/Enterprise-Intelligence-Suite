# TC-IAM-052: Mfa Recovery — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-052 |
| Requirement ID (required) | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-008](TESTPLAN-IAM-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The administrator's own account, or a user with no authenticator.

## Steps
1. Arrange: the administrator's own account, or a user with no authenticator.
2. Act: a reset is attempted.
3. Observe the response and the UI.

## Expected Result
It is refused with 400 and the dialog shows the backend message.

## Automated coverage
- `backend/…/auth/service/AdminMfaResetTest.java` — "nobodyResetsTheirOwnMfaHereAndThereMustBeSomethingToReset"
- `frontend/src/components/security/ResetMfaDialog.test.tsx` — "shows the backend refusal and keeps the dialog open"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
