# TC-TEN-025: Tenant Lifecycle — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-025 |
| Requirement ID (required) | [REQ-TEN-004](../../../docs/02-requirements/FRD/tenant-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/tenant-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-004](TESTPLAN-TEN-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization at its licensed seat limit, with seat overage allowed.

## Steps
1. Arrange: an org with licensedSeats=1, allowSeatOverage=true, and one active admin.
2. Act: add another member.
3. Observe whether it succeeds and whether the org reports over-limit.

## Expected Result
The member is added, and `isOverLimit` reports true.

## Automated coverage
- `backend/.../registration/service/OrganizationSelfServiceTest.java` — `allowSeatOverageBypassesTheSeatLimit`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
