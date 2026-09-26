# TC-TEN-020: Group Management — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-020 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A group with a member.

## Steps
1. Arrange: a group with one member.
2. Act: remove that member.
3. Observe the group's members.

## Expected Result
The group has no members.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanCreateAGroupAndAddOrRemoveMembers`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
