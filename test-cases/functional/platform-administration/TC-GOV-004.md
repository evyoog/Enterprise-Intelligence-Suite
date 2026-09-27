# TC-GOV-004: Platform Administration — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-004 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A region assigned to an organization.

## Steps
1. Arrange: create a region and assign an organization to it.
2. Act: try to delete the region.
3. Observe the response.

## Expected Result
The request is refused (`IllegalArgumentException`); an unassigned region can still be deleted.

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `aRegionAssignedToAnOrganizationCannotBeDeleted`
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `anUnassignedRegionCanBeDeleted`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
