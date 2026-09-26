# TC-IAM-048: Organization Mfa Policy — AC-10

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-048 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization that does not require MFA.

## Steps
1. Arrange: an organization that does not require MFA.
2. Act: a member with no authenticator signs in through SAML.
3. Observe the response and the UI.

## Expected Result
The session is issued straight away, as before.

## Automated coverage
- `backend/…/federation/service/SamlSignInMfaPolicyTest.java` — "anOrganizationWithoutTheMfaPolicyStillSignsInDirectly"
- `backend/…/auth/service/SignInMfaGateTest.java` — "noPolicyAndNoAuthenticatorMeansNoStep"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
