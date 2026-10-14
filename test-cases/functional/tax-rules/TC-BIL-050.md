# TC-BIL-050: Provider credentials never appear in any API response, log message or screen

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-050 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.32) |
| Acceptance Criterion | [AC-22](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-30](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) and BR-SEC-001 |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
`TAX_PROVIDER_BASE_URL`, `TAX_PROVIDER_API_KEY` and `TAX_PROVIDER_API_SECRET` set in the backend environment (secrets file). An administrator with `MANAGE_BILLING`.

## Steps
1. Call every tax endpoint (status, provider, validate, calculations, blocked invoices) and read the responses and the External provider tab.
2. Make the provider fail with errors that echo the request, and read the messages stored on the invoice and in the logs.
3. Search the database for the key and secret values.

## Expected Result
Only "Set" / "Not set" per variable and the masked host are shown. No API key, secret or full path of the base URL appears in a response, a stored message, a log line or the database; the credentials are sent only as request headers to the provider. The screen has no field to type or reveal a secret.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/provider/GenericRestTaxProviderTest.java` — `noMessageEverContainsACredential`, `theProviderIsConfiguredOnlyWithAnHttpsUrlAndAKey`, `theStatusShowsOnlyWhetherEachValueIsSetAndTheHost`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `theStatusNeverExposesACredentialValue`
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `theStatusShowsTheModeAndNeverACredential`, `theProviderSettingsApiTakesNoCredentialsAndRejectsBadValues`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "shows the provider credentials only as set or not set and has no field for a key or secret"

Not automated: Log and database inspection (steps 2 and 3) is by hand.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
