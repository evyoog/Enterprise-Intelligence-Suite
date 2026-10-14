# TC-BIL-052: A calculation in progress keeps the engine it started with (pinned configuration)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-052 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.41) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-2](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Internal mode with approved rules and a validated provider available.

## Steps
1. Start a calculation (the mode is read once at its start).
2. While it is running (or between its start and end, including between provider retries) switch External On.
3. Let the calculation finish and read the invoice's engine and configuration version.
4. Start a new calculation.

## Expected Result
The first calculation finishes with the internal engine and does not call the provider; the invoice records engine INTERNAL and the configuration version it started with. The new calculation uses External. A retry of a Draft is a new calculation and uses the engine in force at that moment (TC-BIL-053).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `aCalculationInProgressKeepsTheEngineItStartedWith`, `anExternalInvoiceUsesTheProviderAndStoresItsReferenceAndAPinnedConfiguration`

Not automated: The race itself is simulated in the test by pinning the mode first and flipping the switch before the calculation runs; a true concurrent run is not automated.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
