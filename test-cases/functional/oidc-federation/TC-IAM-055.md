# TC-IAM-055: Oidc Federation — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-055 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization administrator.

## Steps
1. Arrange: an organization administrator.
2. Act: they add an OIDC provider with a client secret.
3. Observe the response and the UI.

## Expected Result
It is listed with its redirect URI, the secret is stored encrypted and never returned, and editing with a blank secret keeps it.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "theClientSecretIsStoredEncryptedAndNeverReturned"
- `frontend/src/components/federation/OidcProvidersSection.test.tsx` — "adds a provider with a secret and shows the redirect URI to register", "edits without re-entering the secret"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
