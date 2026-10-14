# TC-BIL-124: The discount and the code are shown on the checkout, in Billing and on the invoice document

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-124 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (C100) |
| Acceptance Criterion | [AC-53](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Business rule(s) | [BR-27](../../../docs/02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
An invoice with a coupon (10.00 off 100.00) and one without; the invoice document downloaded (manual).

## Steps
1. Open the checkout for each invoice.
2. Open Billing → Invoices as the customer and Billing admin → Invoices as the administrator.
3. Download the invoice document for the invoice with the coupon.

## Expected Result
Step 1: "Discount (SAVE10)" with a minus amount between the subtotal and the tax for the first; no discount line for the second. Step 2: under the total, "Discount (SAVE10): −10.00" for the first and nothing for the second, in both screens. Step 3: the document prints "Discount (SAVE10): -10.0 {currency}".

## Automated coverage
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the coupon discount between the gross subtotal and the tax, and the net total (C100)", "shows no discount line when the invoice has no coupon (C100)"
- `frontend/src/pages/BillingPage.test.tsx` — "shows the coupon discount of an invoice only when there is one (C100)"
- `frontend/src/pages/admin/AdminBillingPage.test.tsx` — "shows the coupon discount of an invoice (C100)"

Not covered by an automated test: the invoice document (`InvoiceDocuments`) printing the discount line; check a downloaded invoice manually. That step is **Not Run**.

## Actual Result
The automated tests listed above passed on 2026-10-14. The document step has not been run.

## Status
Passed for the automated part (2026-10-14); the not-covered part is Not Run

## Linked Defect (if failed)
None.
