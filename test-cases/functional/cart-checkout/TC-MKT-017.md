# TC-MKT-017: Clear cart needs confirmation; no quantity, coupon, fee or shipping field exists

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-017 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-11](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with items.

## Steps
1. Click Clear cart, then Cancel.
2. Click Clear cart, then confirm.

## Expected Result
Cancel changes nothing; confirming empties the cart. No quantity stepper, coupon, fee or shipping line is shown anywhere in the cart.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "clears the cart only after confirmation", "shows each item…" (no quantity/coupon/shipping)

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
