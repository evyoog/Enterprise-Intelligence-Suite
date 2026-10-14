# TC-MKT-048: The coupon field, its messages and the purchase-request dialog are accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-048 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C100) |
| Acceptance Criterion | [AC-37](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P2 |
| Type | Accessibility |
| Automated | Yes |

## Preconditions
A cart with the coupon field; a coupon refusal; an unusable applied coupon; the purchase-request dialog.

## Steps
1. Run axe on the cart after a refused coupon.
2. Run axe with the "coupon can no longer be used" warning.
3. Open the purchase-request dialog and run axe on it; tab through it.

## Expected Result
No axe violations. The coupon field has a visible label and the refusal is helper text; the warning is an alert; the dialog is labelled by its title, the reason field is required with a visible counter, and every control is keyboard operable.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "shows the reason a coupon is refused and keeps the field", "warns when the applied coupon can no longer be used, and lets the customer remove it", "submits an organization member's cart as one purchase request with a required reason (C100 CA-2)" (each runs axe)

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
