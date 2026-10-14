# TC-BIL-032: Editing provider settings while external mode is on must pass validation, and settings never carry credentials

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-032 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.4, .32, .33) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md), [AC-22](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-4](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-30](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External mode is On with a validated provider.

## Steps
1. On the External provider tab change the environment to PRODUCTION (or the company code) to a value for which the provider validation fails, then press Save settings.
2. Read the answer and reopen the tab.
3. Send `PUT /api/admin/billing/tax/provider` with an extra `apiKey` field and with `timeoutSeconds` 500.

## Expected Result
Step 1: the save is refused (409) and nothing is saved; the previous settings are shown again in step 2. Step 3: the extra `apiKey` is ignored and is not echoed; `timeoutSeconds` outside 2 to 60 is a 400. Credentials are never part of the settings request.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `editingTheProviderSettingsWhileExternalIsOnMustPassValidationOrNothingIsSaved`, `providerSettingsAreValidatedAndCredentialsAreNotPartOfThem`
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `theProviderSettingsApiTakesNoCredentialsAndRejectsBadValues`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "saves provider settings and tests the connection, listing the capabilities"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
