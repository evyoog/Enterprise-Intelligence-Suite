# TC-MKT-025: The cart, the Clear cart dialog and the badge pass the axe test

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-025 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None.

## Steps
1. Run axe on the cart with items, the empty cart and the Clear cart dialog.

## Expected Result
No axe violations; Remove, Undo, Change plan and Clear cart are keyboard operable; the badge has the accessible name "Cart, {n} items".

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — axe checks in "shows each item…", "clears the cart only after confirmation", "shows the empty state…"
- `frontend/src/components/cart/CartButton.test.tsx`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
