# TC-BIL-048: There is no silent fall-back: a failing provider blocks the invoice and internal rules are not used

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-048 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.36) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-3](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External mode On; approved internal rules exist (so a fall-back would succeed if it were possible); the provider keeps failing.

## Steps
1. Generate an invoice while the provider answers HTTP 503 on every attempt.
2. Read the invoice and the Draft invoices tab.
3. Let the provider fail validation later (for example capabilities no longer include the country) and generate another invoice.
4. Let the provider recover and press Retry tax.

## Expected Result
Step 1: three attempts, then the invoice is a Draft with `PROVIDER_UNAVAILABLE`, no tax lines and no number; the internal rules are never read. Step 3: blocked with `PROVIDER_NOT_VALIDATED`/`PROVIDER_UNSUPPORTED`, never taxed by the internal engine. Step 4: the retry is a new calculation with a new transaction reference and the invoice is finalized.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/ExternalTaxEngineTest.java` — `whenTheProviderKeepsFailingTheInvoiceIsBlockedAndNothingElseIsUsed`, `anAdapterThatCrashesBlocksTheInvoiceInsteadOfThrowing`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `whenTheProviderKeepsFailingTheInvoiceIsBlockedAndTheInternalRulesAreNeverUsed`, `aProviderThatLaterFailsValidationBlocksInvoicesInsteadOfFallingBackToInternalRules`, `afterTheProviderRecoversRetryTaxFinalizesTheDraftWithANewTransactionReference`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
