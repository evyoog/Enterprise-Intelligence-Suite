# TC-BIL-030: External mode can be switched on only after the provider passes every check, and the change is audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-030 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.4, .6) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-4](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
External Tax Engine is Off. Case A: no provider credentials on the backend (`TAX_PROVIDER_BASE_URL`, `TAX_PROVIDER_API_KEY` unset) or no provider selected. Case B: a provider that supports the supplier country and the currencies in use, with credentials set.

## Steps
1. Case A: press the External Tax Engine switch and confirm the dialog.
2. Read the dialog "The external engine was not switched on", then close it and read the switch and the active mode.
3. Case B: select the provider on the External provider tab, press the switch and confirm.
4. Open the audit log and look for the mode change.

## Expected Result
Case A: the request is refused (HTTP 409, `changed=false`), the switch stays Off, the mode stays "EIS internal tax engine", and the dialog lists every check with its result (provider selected, credentials, connection, supplier entity, supplier registration, country support, currency support, transaction type). Case B: all checks pass in the same request, the mode becomes "External tax provider", the configuration version increases and an audit entry `TAX_ENGINE_MODE_CHANGED` records the previous and new mode and the administrator. A refused attempt is audited as `TAX_ENGINE_SWITCH_REFUSED`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `externalCannotBeSwitchedOnWithoutAProviderAndTheFailedChecksAreListed`, `externalCannotBeSwitchedOnWhenCredentialsAreMissingOrTheConnectionFails`, `externalCannotBeSwitchedOnWhenTheProviderDoesNotSupportTheSuppliersCountryOrTheCurrencies`, `aProviderThatPassesEveryCheckCanBeSwitchedOnAndTheSettingIsPersisted`
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `switchingOnWithoutAProviderIsAConflictThatListsTheFailedChecks`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "switches on after a confirmation and reports the change", "does nothing when the confirmation is cancelled", "lists every failed check when the external engine cannot be switched on and keeps the mode"

Not automated: No automated test reads the audit entry; check the audit log by hand (step 4).

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
