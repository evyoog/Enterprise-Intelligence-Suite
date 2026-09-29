# TC-IAM-060: Oidc Federation — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-060 |
| Requirement ID (required) | [REQ-IAM-006](../../../docs/02-requirements/FRD/oidc-federation/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/oidc-federation/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-006](TESTPLAN-IAM-006.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A callback.

## Steps
1. Arrange: a callback.
2. Act: the nonce, audience, issuer, signature or expiry is wrong, or there is no email.
3. Observe the response and the UI.

## Expected Result
Sign-in is refused and the user sees the reason.

## Automated coverage
- `backend/…/federation/service/OidcFederationTest.java` — "tamperedOrMismatchedTokensAreRefused"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
