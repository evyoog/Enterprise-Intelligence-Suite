# TC-TEN-013: Member Lifecycle & Access Review — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-013 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A suspended member and no free seat left.

## Steps
1. Arrange: suspend a member, then lower the seat limit to exactly the remaining active count.
2. Act: the admin tries to reactivate them.
3. Observe the response.

## Expected Result
The request is refused with a seat-limit error.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `reactivatingAMemberRespectsTheSeatLimit`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
