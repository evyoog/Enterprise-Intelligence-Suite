# TC-BIL-045: Test connection and Validate capabilities show the result and no secret

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-045 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.30, .31, .33) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-30](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-31](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A configured provider (sandbox) selected on the External provider tab; credentials present on the backend.

## Steps
1. Press "Test connection and validate capabilities".
2. Read the checks, the validation status, and the supported countries, currencies and transaction types.
3. Inspect the API response of `POST /api/admin/billing/tax/provider/validate` and `GET /api/admin/billing/tax/status`.
4. Repeat with the provider returning an answer without the capability lists.

## Expected Result
Steps 1 and 2: a report with every check ticked or crossed, status Passed (or Failed with detail), the validation time, and the countries, currencies and transaction types the provider reported (stored with the result). Step 3: no API key, secret or full URL appears. Step 4: the validation fails.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/provider/GenericRestTaxProviderTest.java` — `capabilitiesAreReadAndTheCredentialsAreSentOnlyAsHeaders`, `aCapabilitiesAnswerWithoutTheListsIsInvalid`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `providerSettingsAreValidatedAndCredentialsAreNotPartOfThem`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "saves provider settings and tests the connection, listing the capabilities", "shows the provider credentials only as set or not set and has no field for a key or secret"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
