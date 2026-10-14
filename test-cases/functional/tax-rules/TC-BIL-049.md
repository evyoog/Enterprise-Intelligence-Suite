# TC-BIL-049: An invalid provider response is rejected and the invoice is blocked

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-049 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.31 (step 6)) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-33](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External mode On with a validated provider. A provider (or stub) that can return each of the bad answers below.

## Steps
1. Return an answer with: status not success; another transaction reference; another currency; no provider reference; a changed taxable amount; a missing line; an unknown or repeated line; a negative tax; line tax not equal to the sum of components; total tax not equal to the sum of lines; tax larger than the taxable amount; a rate outside 0 to 100; an answer that is not JSON.
2. Generate an invoice for each.

## Expected Result
Each answer is rejected and the invoice is blocked with `PROVIDER_INVALID_RESPONSE` (not retried); no tax is stored from the bad answer.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/ExternalTaxEngineTest.java` — `anUnparseableAnswerBlocksAsInvalid`, `aFailureStatusIsRefused`, `anotherTransactionReferenceIsRefused`, `anotherCurrencyIsRefused`, `aMissingProviderReferenceIsRefused`, `aChangedTaxableAmountIsRefused`, `aMissingLineIsRefused`, `anUnknownOrRepeatedLineIsRefused`, `aNegativeTaxIsRefused`, `sumsThatDoNotAddUpAreRefused`, `aTaxLargerThanTheTaxableAmountIsRefused`, `aRateOutsideZeroToHundredIsRefused`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/provider/GenericRestTaxProviderTest.java` — `anAnswerThatIsNotJsonIsInvalid`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
