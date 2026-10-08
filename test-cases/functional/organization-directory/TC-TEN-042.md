# TC-TEN-042: Organizations directory — organization detail and structure

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-042 |
| Requirement ID (required) | [REQ-TEN-007](../../../docs/02-requirements/FRD/organization-directory/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/organization-directory/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-007](TESTPLAN-TEN-007.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Open an organization; visit each tab; open Structure and a node's Details.

## Expected Result
Eight tabs; Overview shows completion and missing items; Structure is a read-only chart (no Add node or node menu); an organization that never opened its structure shows a message and no root is created.

## Automated coverage
- `backend/.../orgdirectory/service/OrgDirectoryServiceTest.java` — `theHierarchyIsReadOnlyForThePlatformAdministratorAndNeverCreatesTheRoot`, `overviewAndTabsAreReadable`
- `frontend/src/pages/admin/AdminOrganizationDetailPage.test.tsx` — `shows the overview…`, `shows the structure as a read-only org chart`, `tells the administrator when the organization has not set up a structure`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
