# TC-MKT-032: Removing a product keeps the others; removing an unsaved one is harmless

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-032 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with two saved products.

## Steps
1. Remove A. 2. Remove A again.

## Expected Result
Only B remains both times; no error.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/wishlist/service/WishlistServiceTest.java` — `removingAProductLeavesTheOthers…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
