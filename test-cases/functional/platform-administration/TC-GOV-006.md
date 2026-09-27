# TC-GOV-006: Platform Administration — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-006 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
No row exists for a given flag key.

## Steps
1. Arrange: pick a flag key with no row.
2. Act: check whether it is enabled.
3. Observe the result.

## Expected Result
It reads as enabled.

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `anUnknownFeatureFlagIsTreatedAsEnabled`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
