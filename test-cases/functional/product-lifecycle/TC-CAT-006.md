# TC-CAT-006: Product Lifecycle & Structure — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-006 |
| Requirement ID (required) | [REQ-CAT-001](../../../docs/02-requirements/FRD/product-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/product-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-001](TESTPLAN-CAT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product that is another's parent.

## Steps
1. Arrange: create a parent and a child product.
2. Act: try to delete the parent.
3. Observe the response.

## Expected Result
The request is refused with a 409 (`ProductInUseException`).

## Automated coverage
- `backend/.../product/service/ProductServiceTest.java` — `deletingAParentProductIsBlockedWhileAChildExists`

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
