# TC-TEN-030: Organization hierarchy — move and history

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-030 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Move a Department from one Division to another.
2. Try to move a node under its descendant, under itself, the root, and with a level violation.
3. Open the node's history.

## Expected Result
The valid move succeeds and one history row (previous and new parent) exists; every invalid move is refused and adds no history.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `movingWritesHistoryAndRefusesCyclesRootAndLevelViolations`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
