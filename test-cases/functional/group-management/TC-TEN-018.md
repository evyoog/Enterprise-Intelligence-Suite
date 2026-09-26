# TC-TEN-018: Group Management — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-018 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An empty group and a member of the same organization.

## Steps
1. Arrange: create a group and a teammate.
2. Act: add the teammate to the group.
3. Observe the group's members.

## Expected Result
The group lists the teammate as a member.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanCreateAGroupAndAddOrRemoveMembers`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
