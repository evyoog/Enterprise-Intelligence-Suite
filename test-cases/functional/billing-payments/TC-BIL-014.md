# TC-BIL-014: Pay by invoice finalizes the invoice with route OFFLINE and shows the bank details

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-014 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-25](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Offline bank details saved by an admin; an OPEN invoice.

## Steps
1. Select Pay by invoice, tick the terms checkbox, click **Generate invoice**.

## Expected Result
Invoice stays OPEN with `payment_route = OFFLINE`; step 3 shows "Invoice generated", the invoice number, total, due date, the bank details with Copy buttons and "Quote invoice number <number> as the payment reference."; a notification email is sent to the billing email; no Razorpay order is created.

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "generates an offline invoice and shows the bank details to pay into"
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/CheckoutOfflinePaymentTest.java` — `payByInvoiceSetsTheOfflineRouteAndKeepsTheInvoiceOpen`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
