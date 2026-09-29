# TC-TEN-017: Group Management — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-017 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An org admin.

## Steps
1. Arrange: an org with an admin.
2. Act: create a group named "Engineering".
3. Observe the group list.

## Expected Result
The group appears with no members.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanCreateAGroupAndAddOrRemoveMembers`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
