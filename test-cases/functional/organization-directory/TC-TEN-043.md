# TC-TEN-043: Organizations directory — individual detail

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-043 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Open an individual; a member of an organization is not available as an individual.

## Expected Result
Five tabs with completion; opening a member as an individual answers not found.

## Automated coverage
- `backend/.../orgdirectory/service/OrgDirectoryServiceTest.java` — `anIndividualHasAProfileButAMemberDoesNot`
- `frontend/src/pages/admin/AdminOrganizationDetailPage.test.tsx` — `shows the profile with completion and its five tabs`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
