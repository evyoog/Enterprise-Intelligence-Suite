# TC-TEN-015: Member Lifecycle & Access Review — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-015 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A removed (INACTIVE) member.

## Steps
1. Arrange: remove a member.
2. Act: try to reactivate them.
3. Observe the response.

## Expected Result
The request is refused.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `removedMemberCannotBeReactivated`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
