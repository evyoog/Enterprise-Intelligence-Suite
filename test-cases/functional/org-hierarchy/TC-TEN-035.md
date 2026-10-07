# TC-TEN-035: Organization hierarchy — levels

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-035 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Remove a level used by a node.
2. Make the first level something other than Organization.
3. Add, rename and reorder levels.

## Expected Result
Steps 1 and 2 are refused; step 3 is saved and the new type can be used.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `levelsCanBeReorderedExtendedAndRenamedButNotRemovedWhileInUse`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
