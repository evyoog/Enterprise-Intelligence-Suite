# TC-CAT-008: Plan Management — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-008 |
| Requirement ID (required) | [REQ-CAT-002](../../../docs/02-requirements/FRD/plan-management/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/plan-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-002](TESTPLAN-CAT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with a plan and no currency sent.

## Steps
1. Arrange: create a product with one plan, no currency field.
2. Act: read back the saved plan.
3. Observe its currency.

## Expected Result
The plan's currency is USD.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `planDefaultsToUsdCurrencyWhenNotSent`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
