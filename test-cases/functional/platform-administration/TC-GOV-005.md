# TC-GOV-005: Platform Administration — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-005 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Arrange: none.
2. Act: create a feature flag, disable it, then delete it.
3. Observe the flag's enabled state at each step.

## Expected Result
Each change is saved; after deletion, the flag reads as enabled again (fails open).

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `anAdminCanCreateUpdateAndDeleteAFeatureFlag`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
