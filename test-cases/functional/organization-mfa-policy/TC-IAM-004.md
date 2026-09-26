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
No session is issued. The response is 401 with `platformMfaEnrollmentRequired: true` and an `mfaEnrollmentChallengeId`, and the login dialog moves to authenticator set-up.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/MfaPolicyServiceTest.java` — "orgRequiringMfaBlocksALoginThatDidNotUseOtp"
- `backend/…/auth/service/SignInMfaGateTest.java` — "policyWithoutAuthenticatorAsksForSetUpOnPasswordAndFederatedSignIn"
- `frontend/src/components/home/MfaSignInEnrollment.test.tsx` — "switches to set-up when the organization requires MFA, then shows the recovery codes once"

**Manual check:** Sign in through `/auth/login` without OTP and check the 401 body has `platformMfaEnrollmentRequired: true`.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
