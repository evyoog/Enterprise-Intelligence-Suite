# TC-BIL-039: Several lines and components are rounded half up per line and per component; compound components

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-039 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.11) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-17](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
India rules CGST 9 + SGST 9 (INR). A rule with GST 5 and a compound PST 10 for Canada. A rule with VAT 10 for France.

## Steps
1. Calculate an invoice with two lines of 10.01 and 3.33 (minor units 1001 and 333) for a customer in the seller's state.
2. Calculate 0.05 and 0.04 (5 and 4 minor units) under the 10% rule.
3. Calculate 100.00 under the Canadian rule.
4. Generate a cart invoice with two subscriptions of 1000.00 and 500.00.

## Expected Result
Step 1: line 1 CGST 0.90 and SGST 0.90, line 2 CGST 0.30 and SGST 0.30; total tax 2.40 (rounding per line and per component, 90.09 becomes 90 and 29.97 becomes 30). Step 2: 0.5 rounds up to 1 minor unit; 0.4 gives an explicit 0. Step 3: GST 5.00 and PST 10.50 (10% of the base plus the earlier component). Step 4: line tax 180.00 and 90.00, four tax lines, invoice tax 270.00; the invoice tax is the sum of the line taxes.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `severalLinesAreTaxedAndRoundedPerLineAndPerComponent`, `halfARoundsUp`, `aCompoundComponentIsChargedOnTheBasePlusTheComponentsBeforeIt`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `aCartInvoiceCarriesSeveralLinesAndTheirOwnTaxAmounts`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
