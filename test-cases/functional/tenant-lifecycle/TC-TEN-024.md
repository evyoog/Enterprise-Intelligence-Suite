# TC-TEN-024: Tenant Lifecycle — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-024 |
| Requirement ID (required) | [REQ-TEN-004](../../../docs/02-requirements/FRD/tenant-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/tenant-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-004](TESTPLAN-TEN-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization.

## Steps
1. Arrange: an existing organization.
2. Act: update it with a region id that does not exist.
3. Observe the response.

## Expected Result
The request is refused (`ResourceNotFoundException`).

## Automated coverage
- `backend/.../registration/service/AdminOrganizationLifecycleTest.java` — `assigningAnUnknownRegionIsRefused`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
