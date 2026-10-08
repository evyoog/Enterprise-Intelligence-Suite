# TC-TEN-029: Organization hierarchy — sibling names

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-029 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. Add Sales under the root.
2. Add " sales " under the root.
3. Try to create a node with no parent.

## Expected Result
Step 2 answers 409; step 3 is refused.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `siblingNamesAreUniqueIgnoringCaseAndANodeCannotBeCreatedWithoutAParent`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
