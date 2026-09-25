# TC-IAM-004: Organization MFA Policy — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-004 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An organization that requires MFA.

## Steps
1. Arrange: an organization that requires MFA.
2. Act: a member signs in without a one-time password.
3. Observe the response and the UI.

## Expected Result
Login is refused with 403 and `organizationMfaRequired: true`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/MfaPolicyServiceTest.java` — "orgRequiringMfaBlocksALoginThatDidNotUseOtp"

**Manual check:** Sign in through `/auth/login` without OTP and check the 403 body has `organizationMfaRequired: true`.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
