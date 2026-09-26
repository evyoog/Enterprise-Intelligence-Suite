# TC-CAT-001: Product Lifecycle & Structure — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-001 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product at version 1.

## Steps
1. Arrange: create a product.
2. Act: update it twice.
3. Observe the returned version.

## Expected Result
The product's version is 3 after the two updates.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `everyUpdateAfterCreationIncrementsVersion`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
