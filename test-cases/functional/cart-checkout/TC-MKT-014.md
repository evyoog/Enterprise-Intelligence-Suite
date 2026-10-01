# TC-MKT-014: The cart shows each item and the summary, or "Tax calculated at payment" without billing details

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-014 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-7](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A cart with one item; the customer has no billing details.

## Steps
1. Open `/cart`.

## Expected Result
The item card shows image (or product icon), name, plan, billing-period chip, term and price; the summary shows the item count, subtotal, "Tax calculated at payment" and the total. AC-6 with a "GST (18 %)" line needs the tax engine (REQ-BIL-002), which is not built yet: with billing details the cart shows "Tax: none for this region".

## Automated coverage
- `frontend/src/pages/CartPage.test.tsx` — "shows each item, the summary with \"Tax calculated at payment\"…"
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `buyingAddsTheProductOnceAndASecondPlanReplacesTheFirst` (`taxCalculatedAtPayment`)

## Actual Result
AC-7 passed in the automated run on 2026-10-01. The GST line of AC-6 cannot run until REQ-BIL-002 tax rules are built.

## Status
Partly passed (AC-6 tax line blocked by REQ-BIL-002)

## Linked Defect (if failed)
None.
