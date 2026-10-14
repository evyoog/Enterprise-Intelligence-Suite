# TC-MKT-047: Cart items never expire; a visitor's chosen item is added after sign-in

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-047 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-36](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
A signed-in customer with an item in the cart; a signed-out browser.

## Steps
1. Leave the item in the cart; change the item's `added_at` in the dev database to a date a year ago (or wait), sign out and in.
2. Signed out, click Buy on a paid plan, sign in.

## Expected Result
Step 1: the item is still in the cart, unchanged (no expiry, CA-3 C). Step 2: after sign-in the customer lands on the cart with the chosen product and plan added (CA-5 A).

## Automated coverage
None. This case is manual only.

Manual only for the first step: no automated test ages a cart item. The second step is covered by [TC-MKT-012](TC-MKT-012.md) (`ProductDetailPage.test.tsx`). **Not Run**.

## Actual Result
Not Run. The first step has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
