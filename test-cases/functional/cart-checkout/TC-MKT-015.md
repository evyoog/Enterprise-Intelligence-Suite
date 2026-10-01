# TC-MKT-015: Remove shows a toast with Undo that restores the item

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-015 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A cart with one item.

## Steps
1. Click Remove on the item.
2. Click Undo in the toast.

## Expected Result
The item disappears and "Removed {product}" with Undo is shown for about 5 seconds; Undo puts the item back on the same plan.

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "removes an item with Undo that restores it with the same plan"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
