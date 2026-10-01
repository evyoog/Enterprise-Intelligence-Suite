# TC-MKT-022: An individual's checkout bills every item on one invoice, empties the cart and is idempotent

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-022 |
| Requirement ID (required) | [REQ-MKT-003](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) (C59) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md), [AC-21](../../../docs/02-requirements/FRD/cart-checkout/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-MKT-003](TESTPLAN-MKT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An individual with two paid items in the cart.

## Steps
1. Click Proceed to checkout.
2. Repeat the checkout call immediately.

## Expected Result
Two subscriptions (with the cart plans) and one OPEN invoice with one line per product are created; the cart is empty and the checkout opens at `/checkout?invoiceId=…&from=cart`. The repeated call returns the same invoice; no second invoice exists. (One invoice per cart is the engineering default for REQ-MKT-003 Open question 1.)

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/cart/service/CartServiceTest.java` — `anIndividualsCheckoutBillsEveryItemOnOneInvoiceEmptiesTheCartAndIsIdempotent`, `anEmptyCartCannotBeCheckedOut`
- `frontend/src/pages/CartPage.test.tsx` — "opens the checkout for the new invoice"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
