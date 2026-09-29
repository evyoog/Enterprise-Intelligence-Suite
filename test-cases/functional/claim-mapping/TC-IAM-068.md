# TC-IAM-068: Claim Mapping — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-068 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Existing providers with no mapping.

## Steps
1. Arrange: existing providers with no mapping.
2. Act: members sign in.
3. Observe the response and the UI.

## Expected Result
Behaviour is unchanged.

## Automated coverage
- `backend/…/federation/service/SamlAuthenticationServiceTest.java` — all 16 tests
- `backend/…/federation/service/OidcFederationTest.java` — all 10 tests

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
