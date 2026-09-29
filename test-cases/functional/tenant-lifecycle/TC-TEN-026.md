# TC-TEN-026: Tenant Lifecycle — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-026 |
| Requirement ID (required) | [REQ-TEN-004](../../../docs/02-requirements/FRD/tenant-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/tenant-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-004](TESTPLAN-TEN-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization at its licensed seat limit, with seat overage not allowed.

## Steps
1. Arrange: an org with licensedSeats=1, allowSeatOverage=false, and one active admin.
2. Act: add another member.
3. Observe the response.

## Expected Result
The request is refused (`SeatLimitExceededException`).

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `seatOverageIsRefusedWhenTheOrganizationDoesNotAllowIt`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
