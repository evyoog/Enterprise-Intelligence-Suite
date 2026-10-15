# TC-MKT-068: The compare page: items, seats, plans, trial badge, features, tax and add to cart

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-068 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-022, BR-EVL-025, BR-EVL-026](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Published products with plans, one with a trial and one with no plans; a visitor and a signed-in customer.

## Steps
1. Open `/compare?items=A:1,B&seats=3`.
2. Change the seats (valid, 0 and 100001), change a plan, remove an item, add a product from the picker up to four.
3. Open `/compare` with one item and with an unknown product.
4. Click Add to cart for a paid plan, for a free plan, and as a visitor.

## Expected Result
The table shows each item with product, plan, billing period, price per seat, seats, subtotal, estimated tax and total, the included features with check marks, the "{n}-day free trial" badge and View product and Add to cart; the address follows every change; seats out of range show an error and ask nothing; fewer than 2 items shows the empty state with Browse the catalog; an unknown product shows the error with Retry; Add to cart uses the existing cart call, is disabled for a free plan and sends a visitor to sign in first; a skeleton shows while loading; the note "Estimates only." is shown; the page works without signing in.

## Automated coverage
- `frontend/src/pages/ComparePage.test.tsx` — "reads the items and seats from the URL, calls the API and shows price x seats and the tax estimate", "shows a free trial badge, the feature grid with check marks, and no-plan products at their flat price", "shows a skeleton while loading", "adds a product from the picker to the URL, up to 4, and offers another plan of a product already chosen", "changes a plan and removes an item, updating the URL and the request", "adds the chosen plan to the cart with the existing cart call, and disables it for a free plan", "sends a signed-out visitor to sign in before the cart add"

The validation and error tests of this file are under [TC-MKT-063](TC-MKT-063.md) and the tax statuses under [TC-MKT-062](TC-MKT-062.md). The API is mocked.

## Actual Result
The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
