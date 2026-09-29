# TC-CAT-005: Product Lifecycle & Structure — AC-5

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-005 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two existing products.

## Steps
1. Arrange: create a parent product and another product.
2. Act: set the second product's parent and variant label.
3. Observe the saved product.

## Expected Result
Both fields are saved and returned.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `hierarchyAndVariantLabelAreSaved`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
