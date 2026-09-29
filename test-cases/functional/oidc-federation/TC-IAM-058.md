# TC-IAM-058: Oidc Federation — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-058 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A provider.

## Steps
1. Arrange: a provider.
2. Act: the administrator tests it.
3. Observe the response and the UI.

## Expected Result
Discovery, issuer and required endpoints are each reported as a check or an error.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "testReportsDiscoveryIssuerAndEndpoints"
- `frontend/src/components/federation/OidcProvidersSection.test.tsx` — "enables a provider, tells the page, and shows test results"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
