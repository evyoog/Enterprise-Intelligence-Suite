# TC-TEN-022: Group Management — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-022 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An existing group.

## Steps
1. Arrange: create a group.
2. Act: delete it.
3. Observe the group list.

## Expected Result
The group no longer appears in the organization's group list.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `deletingAGroupRemovesItFromTheListing`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
