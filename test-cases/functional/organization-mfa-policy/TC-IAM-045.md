# TC-IAM-045: Organization Mfa Policy — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-045 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization that requires MFA and a SAML identity provider.

## Steps
1. Arrange: an organization that requires MFA and a SAML identity provider.
2. Act: a member with no authenticator signs in through SAML.
3. Observe the response and the UI.

## Expected Result
No session cookie is set, the browser goes to `/?mfaEnroll=<id>`, and the session is issued only after a valid first code; the recovery codes are shown once.

## Automated coverage
- `backend/…/federation/service/SamlSignInMfaPolicyTest.java` — "firstSamlSignInToAnMfaRequiredOrganizationSetsUpAnAuthenticatorBeforeAnySession"
- `frontend/src/components/home/MfaSignInEnrollment.test.tsx` — "opens straight into set-up after a SAML redirect with ?mfaEnroll="

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
