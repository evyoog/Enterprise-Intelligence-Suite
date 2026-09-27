# TC-GOV-003: Platform Administration — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-003 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Arrange: none.
2. Act: create a region, then rename and disable it.
3. Observe the result.

## Expected Result
The region is created, then its name and enabled state are both updated.

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `anAdminCanCreateAndUpdateARegion`
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `creatingARegionWithADuplicateCodeIsRefused`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
