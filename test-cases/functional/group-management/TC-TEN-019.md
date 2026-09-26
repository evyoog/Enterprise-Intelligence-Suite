# TC-TEN-019: Group Management — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-019 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A member already in a group.

## Steps
1. Arrange: add a teammate to a group.
2. Act: add the same teammate again.
3. Observe the group's members.

## Expected Result
The group still lists that member exactly once.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanCreateAGroupAndAddOrRemoveMembers`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
