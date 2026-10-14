# TC-BIL-043: With no matching rule the invoice stays a Draft with a reason; no tax is assumed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-043 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.20, .42) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-18](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-40](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Internal mode. No rule matches the customer's country (for example no approved rule at all), or the customer's country, or the seller's country, is missing.

## Steps
1. Generate an invoice for a paid subscription of an Indian customer.
2. Open Admin → Billing → Draft invoices tab (or Resolve tax on the invoice row).
3. Repeat with a customer whose billing details have no country, and with a business profile that has no country.
4. Use the preview calculator for the same transaction.

## Expected Result
Step 1: the invoice is created as Draft: no invoice number, no due date, tax amount 0, no tax lines, `taxStatus` BLOCKED, `taxBlockCode` `NO_MATCHING_RULE` and a reason that names the country. Step 2: the Draft appears with its reason and an error badge counts it. Step 3: `MISSING_CUSTOMER_COUNTRY` and `MISSING_SUPPLIER_PROFILE`. Step 4: a warning "Blocked: ..." and never a zero. Zero tax only ever comes from a zero rate, an exemption or a non-taxable treatment.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `noMatchingRuleBlocksAndNeverAssumesZero`, `aMissingCustomerOrSupplierCountryBlocks`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `withoutAMatchingRuleTheInvoiceIsADraftWithNoNumberAndNoAssumedTax`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "shows why a preview is blocked instead of a zero", "lists Draft invoices with their reason and retries tax"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
