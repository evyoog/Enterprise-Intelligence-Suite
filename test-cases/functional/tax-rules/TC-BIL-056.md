# TC-BIL-056: A refund issues a credit note that reverses the original tax lines in proportion

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-056 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.46) |
| Acceptance Criterion | [AC-20](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-42](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An issued invoice of 1180.00 (1000.00 + CGST 90.00 + SGST 90.00).

## Steps
1. As an administrator refund 590.00 on the payment.
2. Open the invoice's credit notes (customer `/me/billing/invoices/{id}/credit-notes`, organization `/organization/me/billing/invoices/{id}/credit-notes`, admin `/admin/billing/invoices/{id}/credit-notes`) and download the credit note.
3. Change the engine and the rules, then refund more.
4. Refund the remainder of an invoice of 333.33 + tax in several parts.

## Expected Result
Step 2: a credit note with its own number series (`EIS-CN/YY-YY/NNNNNN`) of 500.00 + 90.00 tax, with the original components CGST 45.00 and SGST 45.00, referring to the original invoice number; the original invoice and its tax lines are unchanged. Step 3: the original rates are used, never the engine in force today. Step 4: rounding differences go to the largest remainders so the credit note tax equals the credited gross minus the credited net exactly, and the total credited never exceeds the invoice total.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `aPartialRefundIssuesACreditNoteThatReversesTheTaxInProportion`, `creditNotesNeverCreditMoreThanTheInvoiceAndTheLastOneIsExact`, `aCreditNoteUsesTheOriginalRatesEvenAfterTheEngineChanged`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/InvoiceNumberAndCreditNoteMathTest.java` — `aCreditNotesTaxIsSplitSoThePartsAddUpExactly`
- `frontend/src/pages/BillingPage.test.tsx` — "shows the tax of each invoice and its credit notes after a refund (C96, TX-6)"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
