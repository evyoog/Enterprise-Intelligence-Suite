# TC-IAM-062: Oidc Federation — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-062 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization that requires MFA, or a member whose membership was deactivated.

## Steps
1. Arrange: an organization that requires MFA, or a member whose membership was deactivated.
2. Act: they sign in through OIDC.
3. Observe the response and the UI.

## Expected Result
The sign-in goes to authenticator set-up, or is refused.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "theOrganizationMfaPolicyAppliesAfterOidcSignIn", "aDeactivatedMembershipIsRefused"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
