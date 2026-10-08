# TC-TEN-039: Organizations directory — one list

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-039 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. As a platform administrator open Administration → Organizations.

## Expected Result
Organizations and individuals appear together with a Type column; there is no Individuals or Pending provisioning tab; members are not repeated as individuals.

## Automated coverage
- `backend/.../orgdirectory/service/OrgDirectoryServiceTest.java` — `directoryListsOrganizationsAndIndividualsTogetherWithProfileCompletion`
- `frontend/src/pages/admin/AdminOrganizationsPage.test.tsx` — `lists organizations and individuals together…`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
