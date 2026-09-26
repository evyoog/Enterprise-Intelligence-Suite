# TC-CAT-007: Product Lifecycle & Structure — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-007 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An existing product.

## Steps
1. Arrange: create a product.
2. Act: try to set it as its own dependency.
3. Observe the response.

## Expected Result
The request is refused with a 400 (`IllegalArgumentException`).

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `aProductCannotDependOnItself`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
