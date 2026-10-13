# TC-MKT-031: Saving a product twice keeps one entry; the list is newest first

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-031 |
| Requirement ID (required) | [REQ-MKT-004](../../../docs/02-requirements/FRD/wishlist/requirement.md) (C94) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/wishlist/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-004](TESTPLAN-MKT-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two active products and a customer.

## Steps
1. Save product A twice. 2. Save product B. 3. Read the list.

## Expected Result
The list is B then A, with A once.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/wishlist/service/WishlistServiceTest.java` — `savingAProductIsIdempotentAndTheListIsNewestFirst`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
