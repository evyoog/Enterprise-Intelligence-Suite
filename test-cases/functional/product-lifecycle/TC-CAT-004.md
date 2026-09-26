# TC-CAT-004: Product Lifecycle & Structure — AC-4

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-004 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A RETIRED product.

## Steps
1. Arrange: retire a product.
2. Act: call publish.
3. Observe its status.

## Expected Result
The product's status is ACTIVE again.

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `publishAndRetireChangeStatusAndAreReversible`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
