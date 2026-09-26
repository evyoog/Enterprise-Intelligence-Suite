# TC-IAM-050: Mfa Recovery — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-050 |
| Requirement ID (required) | [REQ-IAM-008](../../../docs/02-requirements/FRD/mfa-recovery/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/mfa-recovery/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-008](TESTPLAN-IAM-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization administrator and an enrolled member of their organization.

## Steps
1. Arrange: an organization administrator and an enrolled member of their organization.
2. Act: the administrator resets the member's MFA.
3. Observe the response and the UI.

## Expected Result
The authenticator, recovery codes and held sign-ins are removed, the member is notified and an `MFA_RESET_BY_ADMIN` audit record exists.

## Automated coverage
- `backend/…/auth/service/AdminMfaResetTest.java` — "anOrganizationAdminResetsAMembersMfaWhichIsAuditedAndNotified"
- `frontend/src/components/security/ResetMfaDialog.test.tsx` — "lets an organization admin reset a member after confirming"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
