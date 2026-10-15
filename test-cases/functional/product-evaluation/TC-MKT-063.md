# TC-MKT-063: The compare call refuses bad items and seats with the reason

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-063 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-022, BR-EVL-023, BR-EVL-024](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Published products A, B and C; a plan of C.

## Steps
1. Compare with one item, with no `items`, with five items, with the same item twice, with a part that is not a number, with `seats=0`, with a plan of another product, and with an unknown product id.

## Expected Result
400 for each of the first seven (messages "Choose 2 to 4 products or plans to compare.", "The same item is chosen twice.", "… is not a product or a product:plan pair.", "Seats must be between 1 and 100000.", "Plan {id} does not belong to {product}."); 404 for the unknown product. On the page an invalid seats value or a server refusal shows an error with Retry, and an unknown product shows the 404 message.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `theItemsAreValidated`
- `backend/src/test/java/com/vyoog/eisplatform/config/EvaluationAuthorizationTest.java` — `theComparePageIsOpenToEveryoneAndValidatesItsItems`
- `frontend/src/pages/ComparePage.test.tsx` — "shows the API validation message with Retry, and retries", "shows a 404 for an unknown product", "validates the seats: out of range shows an error and asks nothing; a valid number goes to the URL", "treats an invalid seats value in the URL as an error", "asks for at least 2 items when the URL has fewer, without calling the API"

The product that is not published (404) is not tested separately.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped. The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
