# TC-BIL-046: External mode: the validated breakdown is stored and printed; an unsupported country is blocked

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-046 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.31, .34, .37) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-31](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-32](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-35](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External mode is On with a validated provider that supports India and INR. Products have a tax class and the supplier registrations are set.

## Steps
1. Generate an invoice for an Indian customer.
2. Read the invoice's tax lines, engine and the calculation attempts (`GET /api/admin/billing/tax/invoices/{id}/calculations`).
3. Inspect the request the provider received.
4. Generate an invoice for a customer in a country the provider did not report as supported (and, separately, a product with no tax class).

## Expected Result
Step 1: the invoice is finalized with the provider's components. Step 2: tax engine EXTERNAL, the provider reference, the configuration version and attempts are stored; the printed invoice shows the components. Step 3: the request carries the supplier entity and registrations, the customer's billing details and tax IDs, each line's tax class and taxable amount (excluding tax), the currency, the date, and the unique transaction reference also sent as `Idempotency-Key`. Step 4: no call is made; the invoice stays Draft with `PROVIDER_UNSUPPORTED` (or `MISSING_TAX_CLASS`).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/ExternalTaxEngineTest.java` — `aValidProviderAnswerBecomesTheStoredBreakdown`, `theRequestCarriesTheSupplierTheCustomerTheClassAndTheIdempotencyKey`, `anUnsupportedCountryCurrencyOrTypeBlocksWithoutCallingTheProvider`, `aMissingCountryOrTaxClassBlocks`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `anExternalInvoiceUsesTheProviderAndStoresItsReferenceAndAPinnedConfiguration`, `anUnsupportedCountryIsBlockedByTheExternalEngine`, `aProductWithoutATaxClassIsBlockedByTheExternalEngine`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/provider/GenericRestTaxProviderTest.java` — `aCalculationIsPostedWithAnIdempotencyKeyAndASignatureAndParsed`, `withoutASecretThereIsNoSignatureHeader`, `decimalAmountsAreNotAcceptedAsIntegers`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
