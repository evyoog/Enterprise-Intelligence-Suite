# TC-CAT-010: Plan Management — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-010 |
| Requirement ID (required) | [REQ-CAT-002](../../../docs/02-requirements/FRD/plan-management/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/plan-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-002](TESTPLAN-CAT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An existing product with a plain plan (no pricing fields).

## Steps
1. Arrange: create a product with a plan that has no pricing fields.
2. Act: update the product without touching the plan's pricing fields.
3. Observe the plan.

## Expected Result
The plan behaves exactly as it did before this feature (price/billing/sortOrder unchanged, pricing fields absent).

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `updateProductReplacesPlansEntirely` (pre-existing, still passing)

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
