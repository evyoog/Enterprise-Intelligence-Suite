# TC-GOV-001: Platform Administration — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-GOV-001 |
| Requirement ID (required) | [REQ-GOV-001](../../../docs/02-requirements/FRD/platform-administration/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/platform-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-GOV-001](TESTPLAN-GOV-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A freshly started backend.

## Steps
1. Arrange: start the backend.
2. Act: list currencies.
3. Observe the result.

## Expected Result
USD, EUR, GBP and INR all exist and are enabled.

## Automated coverage
- `backend/.../administration/service/PlatformAdministrationServiceTest.java` — `currenciesAreSeededFromTheFixedCurrencyEnum`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
