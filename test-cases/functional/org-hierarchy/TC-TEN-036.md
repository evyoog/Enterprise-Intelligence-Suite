# TC-TEN-036: Organization hierarchy — permission and isolation

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-036 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. As a plain member open the structure.
2. As an administrator request another organization's node (read, move, delete).

## Expected Result
The member gets 403; the other organization's node answers 404.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `anotherOrganizationsNodesAreNotFoundAndNonAdminsAreForbidden`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
