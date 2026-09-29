# TC-MKT-010: Submitting for a nonexistent product is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-010 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Act: a customer submits a review against a product id that does not exist.
2. Observe the response.

## Expected Result
The request is refused with a generic 404 (`ResourceNotFoundException`).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `submittingForANonexistentProductIsRefused`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
