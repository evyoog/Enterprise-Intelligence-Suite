# TC-TEN-012: Member Lifecycle & Access Review — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-012 |
| Requirement ID (required) | [REQ-TEN-002](../../../docs/02-requirements/FRD/member-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/member-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-002](TESTPLAN-TEN-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A suspended member and a free seat.

## Steps
1. Arrange: suspend a member (seats are not full).
2. Act: the admin reactivates them.
3. Observe their status.

## Expected Result
The member's status is ACTIVE.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `orgAdminCanSuspendThenReactivateATeammate`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
