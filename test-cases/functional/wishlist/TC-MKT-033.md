# TC-MKT-033: Each customer sees only their own list; only active products can be saved

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-033 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two customers; an active, a retired and an unknown product.

## Steps
1. Customer one saves the active product. 2. Customer two reads their list. 3. Save the retired and the unknown product.

## Expected Result
Customer two has an empty list; saving the retired or unknown product is refused as not found.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/wishlist/service/WishlistServiceTest.java` — `aCustomerOnlySeesTheirOwnListAndOnlyActiveProductsCanBeSaved`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
