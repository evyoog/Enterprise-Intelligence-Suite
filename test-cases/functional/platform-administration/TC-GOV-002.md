# TC-GOV-002: Platform Administration — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-002 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An enabled currency.

## Steps
1. Arrange: an enabled currency (EUR).
2. Act: disable it, then re-enable it.
3. Observe both results.

## Expected Result
Both changes are saved and returned.

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `anAdminCanDisableAndReEnableACurrency`
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `updatingAnUnknownCurrencyIsRefused`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
