# TC-BIL-055: GSTIN format is validated for Indian customers; other countries accept free text

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-055 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.45) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-44](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer or organization on Billing → Billing details.

## Steps
1. Select country India and enter the tax ID `GSTIN123`, then Save.
2. Enter ` 33abcde1234f1z5 ` (lower case, spaces) and Save.
3. Clear the tax ID and Save.
4. Select Germany and save the tax ID `DE 123456789`.
5. Call `PUT /api/me/billing/details` with `GSTIN123` and country India.
6. Generate invoices for an Indian customer in the seller's state with and without a valid GSTIN.

## Expected Result
Step 1: a field error "Enter a valid 15-character GSTIN, for example 33ABCDE1234F1Z5." and Save is disabled; the API answers 400 with the message (step 5). Step 2: saved as `33ABCDE1234F1Z5`. Step 3: a blank tax ID is accepted. Step 4: free text up to 50 characters is accepted and no GSTIN hint is shown. Step 6: the same tax (TX-5: a valid GSTIN does not change the tax for Indian customers) and the GSTIN is printed on the invoice.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InvoiceTaxIntegrationTest.java` — `anIndianCustomerMustGiveAValidGstinOrNone`, `foreignCustomersMayGiveFreeTextTaxIdsAndAValidGstinDoesNotChangeTheTaxForIndianCustomers`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/support/TaxSupportTest.java` — `anIndianTaxIdMustBeAValidGstin`, `otherCountriesAcceptFreeTextTaxIds`
- `frontend/src/pages/BillingPage.test.tsx` — "checks the format of a GSTIN for an Indian customer before saving (TX-4)", "does not ask for a GSTIN format outside India"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
