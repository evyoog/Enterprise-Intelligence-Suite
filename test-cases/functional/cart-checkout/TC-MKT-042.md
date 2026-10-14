# TC-MKT-042: A changed price is confirmed once for the whole cart

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-042 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-31](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Business rule(s) | [BR-7](../../../docs/02-requirements/FRD/cart-checkout/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with two items whose catalog prices changed after they were added.

## Steps
1. Open the cart and click Proceed to checkout.
2. Click **Confirm new prices** once.
3. Proceed again.

## Expected Result
Step 1: each item shows "The price changed from {old} to {new}." and one notice above the items says the prices of 2 items changed, with one **Confirm new prices** button; checkout is blocked. Step 2: both item messages disappear (one confirmation request per changed item is sent; each item keeps its new price). Step 3: validation passes and checkout proceeds.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "confirms every changed price once for the whole cart (C100 CA-6)"
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `confirmingTheNewPriceClearsThePriceIssue`

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
