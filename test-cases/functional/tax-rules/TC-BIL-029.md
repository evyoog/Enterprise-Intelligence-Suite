# TC-BIL-029: The External Tax Engine switch shows the active mode and its status, and the mode persists

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-029 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.1, .2, .3) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-1](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator with `MANAGE_BILLING`. No tax setting has ever been changed (default), and no approved tax rule exists.

## Steps
1. Open Platform Admin → Billing → Tax configuration (`/admin/billing/tax`).
2. Read the External Tax Engine switch, the active calculation mode, the status chip and the messages.
3. Approve and activate one in-force rule (see TC-BIL-033), reload the screen and read the status again.
4. Switch the engine on (TC-BIL-030), sign out, sign in again as another administrator and reopen the screen.
5. Restart the backend and reopen the screen.

## Expected Result
Step 2: the switch is Off with the description "Enable or disable external tax calculation for billing transactions."; the active mode is "EIS internal tax engine"; the status is "Blocking invoices" with a message that no approved rule is in force. Step 3: the status becomes "Ready". Steps 4 and 5: the same mode (External tax provider) and the same configuration version, change time and administrator are shown for every administrator and server, because the setting is a database row.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `theDefaultIsTheInternalEngineAndTheScreenSaysItIsBlockingWhenNoRuleIsInForce`, `theStatusIsReadyOnceARuleIsInForce`, `aProviderThatPassesEveryCheckCanBeSwitchedOnAndTheSettingIsPersisted`
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `theStatusShowsTheModeAndNeverACredential`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "shows the External Tax Engine switch, the active mode and its status", "shows the external mode when the switch is on", "says clearly when invoices are blocked"

Not automated: Sign-out and application restart (steps 4 and 5) are checked by hand; the automated tests cover the persisted setting, not a real restart.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
