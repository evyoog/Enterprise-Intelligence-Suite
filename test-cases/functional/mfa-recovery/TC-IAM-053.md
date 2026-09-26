# TC-IAM-053: Mfa Recovery — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-053 |
| Requirement ID (required) | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-008](TESTPLAN-IAM-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator.

## Steps
1. Arrange: a platform administrator.
2. Act: they reset an account's MFA by email.
3. Observe the response and the UI.

## Expected Result
It is reset (email match ignores case); an unknown email returns 404.

## Automated coverage
- `backend/…/auth/service/AdminMfaResetTest.java` — "aPlatformAdminResetsAnyAccountByEmail"
- `frontend/src/components/security/ResetMfaDialog.test.tsx` — "lets a platform admin reset any account by email"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
