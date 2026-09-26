# TC-IAM-066: Claim Mapping — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-066 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A mapping whose attribute is missing from the assertion.

## Steps
1. Arrange: a mapping whose attribute is missing from the assertion.
2. Act: a member signs in.
3. Observe the response and the UI.

## Expected Result
The defaults and then the fixed fallbacks are used.

## Automated coverage
- `backend/…/federation/service/ClaimMappingTest.java` — "samlFallsBackToTheDefaultsWhenTheConfiguredAttributeIsMissing"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
