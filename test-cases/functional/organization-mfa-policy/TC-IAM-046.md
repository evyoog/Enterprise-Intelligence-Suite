# TC-IAM-046: Organization Mfa Policy — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-046 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A member who already set up an authenticator.

## Steps
1. Arrange: a member who already set up an authenticator.
2. Act: they sign in through SAML or with a password.
3. Observe the response and the UI.

## Expected Result
The sign-in waits for their code (`/?mfaChallenge=<id>` after SAML) before a session is issued.

## Automated coverage
- `backend/…/federation/service/SamlSignInMfaPolicyTest.java` — "firstSamlSignInToAnMfaRequiredOrganizationSetsUpAnAuthenticatorBeforeAnySession"
- `backend/…/auth/service/SignInMfaGateTest.java` — "anEnrolledMemberAlwaysGetsTheCodeStep"
- `frontend/src/components/home/MfaSignInEnrollment.test.tsx` — "opens straight into the code step after a SAML redirect with ?mfaChallenge="

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
