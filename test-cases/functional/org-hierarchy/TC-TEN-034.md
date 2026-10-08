# TC-TEN-034: Organization hierarchy — CSV import

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-034 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Import a CSV with good rows, a duplicate, an unknown parent, an unknown type and a level violation.
2. Import a file without the name/type columns and an empty file.

## Expected Result
Good rows are created, each bad row is reported with its row number, and the two bad files are refused.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `csvImportCreatesGoodRowsAndReportsBadOnes`, `csvParserHandlesQuotesAndBlankLines`
- `frontend/src/pages/OrganizationStructurePage.test.tsx` — `imports a CSV and lists failed rows`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
