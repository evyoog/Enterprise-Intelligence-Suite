# TC-BIL-053: A Draft invoice is hidden from the customer, cannot be paid, and Retry tax restarts the calculation

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-053 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.42) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-40](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-41](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A Draft invoice blocked with `NO_MATCHING_RULE` (TC-BIL-043).

## Steps
1. As the customer open the invoice list (`/me/billing/invoices`), the invoice detail, its document and try to pay it; open checkout for it.
2. As an administrator open Draft invoices, press Retry tax while still no rule is approved.
3. Approve the missing rule, press Retry tax again.
4. Press Retry tax on an issued invoice.
5. Case: a Draft blocked because the customer has no billing details: the customer saves billing details.

## Expected Result
Step 1: the Draft is not listed; the document is 404 "This invoice is not issued yet"; payment is refused (only an OPEN invoice can be paid); checkout shows "We are preparing your invoice" with no payment options and `taxPending`; the customer is not told the block reason. Step 2: "Still blocked: <reason>", still a Draft. Step 3: the same invoice is finalized: status OPEN, number `EIS/YY-YY/NNNNNN`, issue date, due date 15 days later, event published, the Draft leaves the list and the engine card status refreshes. Step 4: refused (only a Draft can be retried). Case: the Draft is retried automatically and issued when the billing details are saved. A retry uses the engine in force at that moment.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `aDraftIsHiddenFromTheCustomerCannotBePaidAndShowsAsTaxPendingAtCheckout`, `whenTheRuleIsApprovedRetryTaxIssuesTheInvoiceAndNothingElseChanges`, `retryingAnInvoiceThatIsStillBlockedKeepsItADraft`, `withoutBillingDetailsTheInvoiceWaitsAndIsIssuedWhenTheCustomerSavesThem`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `retryingADraftRestartsWithTheEngineInForceAtThatMoment`, `anIssuedInvoiceCannotBeRetried`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/controller/InvoiceDocumentsTest.java` — `aDraftHasNoDocument`
- `frontend/src/pages/CheckoutPage.test.tsx` — "does not offer payment for a Draft invoice whose tax could not be determined (C96)"
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "lists Draft invoices with their reason and retries tax", "says when a retried invoice is still blocked", "shows an empty state when nothing is waiting"

Not automated: The "cannot be paid" API answer (409 `INVALID_STATE`) is not asserted by a dedicated automated test; check it by hand.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
