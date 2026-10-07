# TC-TEN-031: Organization hierarchy — deactivate and delete guards

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-031 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Deactivate a node with children without confirmation, then with confirmation.
2. Delete a node with children, then one with a member, then an empty one.

## Expected Result
Unconfirmed deactivation is refused (409), confirmed succeeds; deletes with children or members are refused, the empty one is deleted.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `deactivatingAndDeletingAreGuardedByChildrenAndMembersAndTheRootIsProtected`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
