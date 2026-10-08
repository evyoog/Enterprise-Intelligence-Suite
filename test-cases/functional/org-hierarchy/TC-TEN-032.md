# TC-TEN-032: Organization hierarchy — root protection

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-032 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Try to delete, deactivate, move or retype the root.
2. Rename the root.

## Expected Result
The first four are refused; the rename succeeds.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `deactivatingAndDeletingAreGuardedByChildrenAndMembersAndTheRootIsProtected`, `movingWritesHistoryAndRefusesCyclesRootAndLevelViolations`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
