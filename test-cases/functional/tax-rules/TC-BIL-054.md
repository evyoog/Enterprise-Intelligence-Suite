# TC-BIL-054: The issued invoice shows subtotal, each tax component, total, place of supply, tax class and SAC/HSN code

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-054 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.43, .44) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-43](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An issued invoice for 1000.00 (tax-exclusive price) to a customer in the seller's state with GSTIN `33ABCDE1234F1Z5`; product class SAAS with SAC 998439.

## Steps
1. Open the invoice detail (customer and admin) and download the invoice document.
2. Read the checkout summary for the same invoice.
3. Open an invoice issued before tax was calculated (legacy).

## Expected Result
The invoice shows subtotal 1000.00, CGST 90.00 and SGST 90.00, total 1180.00 (the price is without tax, TX-1), the issuer legal name, address, GSTIN and PAN, the customer GSTIN, "Place of supply: Tamil Nadu, India", and per line the tax amount, tax class and SAC/HSN code. Admin views also show the block fields; customer views do not. A legacy invoice still prints a single tax line.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `anInvoiceWithinTheSellersStateShowsCgstAndSgstAndAddsThemToThePrice`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/controller/InvoiceDocumentsTest.java` — `anIssuedInvoicePrintsEveryLegalFieldAndEachTaxComponent`, `anInvoiceIssuedBeforeTaxWasCalculatedStillPrintsASingleTaxLine`
- `frontend/src/pages/CheckoutPage.test.tsx` — "shows the real tax components, rates and the place of supply from the invoice (C96)"
- `frontend/src/pages/BillingPage.test.tsx` — "shows the tax of each invoice and its credit notes after a refund (C96, TX-6)"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
