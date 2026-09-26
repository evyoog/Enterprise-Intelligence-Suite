# TC-IAM-065: Claim Mapping — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-065 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SAML provider mapped to custom attributes.

## Steps
1. Arrange: a SAML provider mapped to custom attributes.
2. Act: a member signs in with both the custom and the default attributes.
3. Observe the response and the UI.

## Expected Result
The custom values are used.

## Automated coverage
- `backend/…/federation/service/ClaimMappingTest.java` — "samlUsesTheConfiguredAttributeFirstThenTheDefaults"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
