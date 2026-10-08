# TC-TEN-041: Organizations directory — profile completion

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-041 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Compare an empty and a fully filled organization and individual; read the summary.

## Expected Result
Percentages and missing items follow BR-DIR-003 to 005; the summary counts add up to the total.

## Automated coverage
- `backend/.../orgdirectory/service/OrgDirectoryServiceTest.java` — `directoryListsOrganizationsAndIndividualsTogetherWithProfileCompletion`, `billingAddressNeedsItsOwnFieldsWhenNotTheSameAsTheAddress`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
