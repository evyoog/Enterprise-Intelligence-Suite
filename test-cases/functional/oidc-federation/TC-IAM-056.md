# TC-IAM-056: Oidc Federation — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-056 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A non-https issuer, scopes without openid, or no secret on create.

## Steps
1. Arrange: a non-https issuer, scopes without openid, or no secret on create.
2. Act: the administrator saves.
3. Observe the response and the UI.

## Expected Result
It is refused with the backend message.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "invalidConfigurationIsRefused"
- `frontend/src/components/federation/OidcProvidersSection.test.tsx` — "shows the backend refusal on save"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
