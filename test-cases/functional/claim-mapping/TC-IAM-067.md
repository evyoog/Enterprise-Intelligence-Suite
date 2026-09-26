# TC-IAM-067: Claim Mapping — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-067 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OIDC provider mapped to custom claims.

## Steps
1. Arrange: an OIDC provider mapped to custom claims.
2. Act: a member signs in.
3. Observe the response and the UI.

## Expected Result
The custom claims are used, and unmapped details come from the OIDC defaults.

## Automated coverage
- `backend/…/federation/service/ClaimMappingTest.java` — "oidcUsesTheConfiguredClaimsThenTheOidcDefaults"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
