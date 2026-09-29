# TC-TEN-021: Group Management — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-021 |
| Requirement ID (required) | [REQ-TEN-003](../../../docs/02-requirements/FRD/group-management/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/group-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-003](TESTPLAN-TEN-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A group in one organization and a member of another.

## Steps
1. Arrange: an admin and group in org A; a member in org B.
2. Act: the admin of org A tries to add org B's member.
3. Observe the response.

## Expected Result
The request is refused with a 404.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `cannotAddAMemberOfAnotherOrganizationToAGroup`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
