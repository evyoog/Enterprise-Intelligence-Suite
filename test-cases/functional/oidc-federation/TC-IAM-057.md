# TC-IAM-057: Oidc Federation — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-057 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An enabled SAML provider.

## Steps
1. Arrange: an enabled SAML provider.
2. Act: the administrator enables an OIDC provider (or the reverse).
3. Observe the response and the UI.

## Expected Result
The other one is disabled and the SSO check reports the enabled protocol.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "onlyOneSamlOrOidcProviderIsEnabledPerOrganization"
- `frontend/src/components/federation/OidcProvidersSection.test.tsx` — "enables a provider, tells the page, and shows test results"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
