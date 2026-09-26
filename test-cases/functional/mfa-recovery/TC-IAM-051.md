# TC-IAM-051: Mfa Recovery — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-051 |
| Requirement ID (required) | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-008](TESTPLAN-IAM-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A member of another organization, or a caller without `MANAGE_USERS`.

## Steps
1. Arrange: a member of another organization, or a caller without `MANAGE_USERS`.
2. Act: a reset is attempted.
3. Observe the response and the UI.

## Expected Result
It is refused with 403 and nothing changes.

## Automated coverage
- `backend/…/auth/service/AdminMfaResetTest.java` — "anOrganizationAdminCannotResetAnotherOrganizationsMemberOrWithoutManageUsers"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
