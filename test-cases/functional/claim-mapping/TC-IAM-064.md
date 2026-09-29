# TC-IAM-064: Claim Mapping — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-064 |
| Requirement ID (required) | [REQ-IAM-007](../../../docs/02-requirements/FRD/claim-mapping/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/claim-mapping/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-007](TESTPLAN-IAM-007.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A SAML or OIDC provider.

## Steps
1. Arrange: a SAML or OIDC provider.
2. Act: the administrator saves a mapping.
3. Observe the response and the UI.

## Expected Result
The names are stored trimmed, blank ones mean default, reset clears all four, and another organization's provider is refused.

## Automated coverage
- `backend/…/federation/service/ClaimMappingTest.java` — "savingAMappingStoresTrimmedNamesAndBlankMeansDefault"
- `frontend/src/components/federation/ClaimMappingDialog.test.tsx` — "saves the entered names and shows the protocol defaults", "resets every field to the defaults"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
