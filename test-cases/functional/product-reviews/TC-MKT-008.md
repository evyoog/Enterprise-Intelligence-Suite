# TC-MKT-008: Moderating an already-decided review is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-008 |
| Requirement ID (required) | [REQ-MKT-002](../../../docs/02-requirements/FRD/product-reviews/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/product-reviews/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-002](TESTPLAN-MKT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved review.

## Steps
1. Arrange: a customer submits a review; an admin approves it.
2. Act: the admin attempts to approve it again.
3. Act: the admin attempts to reject it.
4. Observe both responses.

## Expected Result
Both attempts are refused (`IllegalArgumentException` / 400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/reviews/service/ProductReviewServiceTest.java` — `moderatingAnAlreadyDecidedReviewIsRefused`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
