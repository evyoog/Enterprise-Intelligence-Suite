# TC-TEN-023: Tenant Lifecycle — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-023 |
| Requirement ID (required) | [REQ-TEN-004](../../../docs/02-requirements/FRD/tenant-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/tenant-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-004](TESTPLAN-TEN-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization and an existing region.

## Steps
1. Arrange: create a region.
2. Act: update the organization with that region id and allowSeatOverage=true.
3. Observe the result.

## Expected Result
Both fields are saved, and the response includes the region's name.

## Automated coverage
- `backend/.../registration/service/AdminOrganizationLifecycleTest.java` — `updateCanAssignARegionAndSetTheSeatOveragePolicy`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
