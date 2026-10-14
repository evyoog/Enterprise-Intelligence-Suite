# TC-BIL-035: Rules are validated when saved: components, rates, dates, states and classes

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-035 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.10, .11, .19) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-10](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-16](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-24](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on the Rules tab.

## Steps
1. Open New rule and press Save with an empty form.
2. Enter a taxable rule with a rate of 100.5, then 18.12345, then a duplicate component code.
3. Set effective-to before effective-from.
4. Choose treatment Exempt and read the components section.
5. Save a valid India rule with state "Tamil Nadu" and tax class "saas".

## Expected Result
Steps 1 to 3: each error is shown as text (name, start date, rate 0 to 100 with up to 4 decimals, unique component codes, end date not before start date) and no API call is made for client-detectable errors; the server answers 400 `VALIDATION_ERROR` for the same cases. Step 4: no components are needed and none are stored. Step 5: the state is stored as its catalog key and the class code is stored upper case; a class that does not exist is refused. No rate is pre-filled.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `rulesAreValidatedWhenSaved`, `aStateIsStoredAsItsCatalogKeyAndANonTaxableRuleHasNoComponents`, `theTaxClassOfARuleMustExistAndIsStoredUpperCase`
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `aRuleIsCreatedAndRatesAreValidatedByTheApi`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "creates a rule with CGST and SGST components and sends no hard-coded rate of its own", "validates the rule form before calling the API"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
