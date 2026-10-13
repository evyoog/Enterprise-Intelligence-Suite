# TC-MKT-035: The product response says when the product was added

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-035 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active product.

## Steps
1. Read the product through the service.

## Expected Result
`createdAt` is set.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/wishlist/service/WishlistServiceTest.java` — `theProductListCarriesWhenEachProductWasAdded`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
