# TC-TEN-061: Projects — assign users (members)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-061 |
| Requirement ID (required) | [REQ-TEN-009](../../../docs/02-requirements/FRD/projects/requirement.md) (REQ-TEN-009.2, BR-PRJ-002) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/projects/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-009](TESTPLAN-TEN-009.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`ProjectServiceTest.onlyActiveMembersOfTheSameOrganizationCanBeAdded`) |

## Steps
1. Add an active member to a project, then add the same member again.
2. Try to add a member of another organization and a removed member.
3. Remove the member twice.

## Expected Result
The member is added once. The other organization's member is "not found"; the removed member is refused (409). Removing twice is not an error.

## Status
Passed (automated, 2026-10-12).
