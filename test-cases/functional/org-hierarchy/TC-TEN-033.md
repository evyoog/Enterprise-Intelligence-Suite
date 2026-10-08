# TC-TEN-033: Organization hierarchy — place members

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-033 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Place a member on a node, then remove the placement.
2. Try to place a member of another organization.

## Expected Result
Placement shows in the node's members and count; the other organization's member is 'not found'.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `membersCanBePlacedOnlyWithinTheirOwnOrganization`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
