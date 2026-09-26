# TC-CAT-009: Plan Management — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-009 |
| Requirement ID (required) | [REQ-CAT-002](../../../docs/02-requirements/FRD/plan-management/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/plan-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-002](TESTPLAN-CAT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A new plan with all pricing fields set.

## Steps
1. Arrange: build a plan with currency, usage limit, included features, usage price, overage charge and tier-pricing text.
2. Act: create the product with this plan.
3. Observe the saved plan.

## Expected Result
All five fields are saved and returned unchanged.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `planPricingFieldsAreSavedAndReturned`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
