# TC-TEN-028: Organization hierarchy — level order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-028 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Create a Department under the root.
2. Try to add a Division, another Department and an unknown type under it.
3. Add a Team under it.

## Expected Result
Steps 2 are refused; step 3 succeeds.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `aChildMustBeOnALowerLevelThanItsParent`, `retypingMustKeepTheNodeAboveItsChildren`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
