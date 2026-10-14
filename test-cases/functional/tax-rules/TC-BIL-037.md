# TC-BIL-037: India GST: CGST + SGST within the seller's state, IGST between states, place of supply from a valid GSTIN

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-037 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.15) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-12](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-13](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-14](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seller (Billing settings → business profile) in Tamil Nadu. Approved rules: within state CGST 9 + SGST 9; between states IGST 18. Three customers: one in Tamil Nadu, one in Karnataka, one with a Tamil Nadu billing address and GSTIN `29ABCDE1234F1Z5`.

## Steps
1. Generate an invoice of 1000.00 for the Tamil Nadu customer.
2. Generate an invoice of 1000.00 for the Karnataka customer.
3. Generate an invoice of 1000.00 for the customer with the Karnataka GSTIN and the Tamil Nadu address.
4. Repeat step 3 with an invalid GSTIN (`NOT-A-GSTIN`).
5. Repeat with the customer's state missing.

## Expected Result
Step 1: tax lines CGST 90.00 and SGST 90.00, total 1180.00, place of supply "Tamil Nadu, India". Step 2: one line IGST 180.00, place of supply "Karnataka, India". Step 3: IGST 180.00, place of supply Karnataka (the GSTIN state wins). Step 4: the invalid GSTIN is ignored for the place of supply, so CGST + SGST. Step 5: only a rule whose supply type is Any can apply; otherwise the invoice is blocked with a reason that the place of supply could not be decided. The rates come only from the approved rules.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `withinTheSellersStateTheTaxIsCgstAndSgst`, `betweenStatesTheTaxIsIgst`, `aValidGstinSetsThePlaceOfSupplyOverTheBillingState`, `anInvalidGstinIsNotUsedForThePlaceOfSupply`, `whenTheStateIsMissingOnlyARuleThatDoesNotNeedItCanApply`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `anInvoiceWithinTheSellersStateShowsCgstAndSgstAndAddsThemToThePrice`, `anInvoiceToAnotherStateShowsIgst`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
