# TC-IAM-061: Oidc Federation — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-061 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A sign-in state.

## Steps
1. Arrange: a sign-in state.
2. Act: it is reused, used for another organization, unknown, or the provider returns an error.
3. Observe the response and the UI.

## Expected Result
It is refused.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "stateIsSingleUseAndBoundToTheOrganization"
- `backend/src/test/java/com/vyoog/eisplatform/config/OidcEndpointSecurityTest.java` — "signInNavigationIsPublicAndRedirectsBackWithAReason"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
