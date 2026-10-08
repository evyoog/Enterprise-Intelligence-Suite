# TC-TEN-027: Organization hierarchy — first open

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-027 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator (MANAGE_ORGANIZATION) and a member.

## Steps
1. As the administrator open the structure for the first time.

## Expected Result
A root named after the organization and the seven default levels exist; a second open creates nothing new.

## Automated coverage
- `backend/.../orghierarchy/service/OrgHierarchyServiceTest.java` — `firstOpenCreatesTheRootAndTheSevenDefaultLevels`

## Actual Result
The automated tests passed on 2026-10-07.

## Status
Passed
