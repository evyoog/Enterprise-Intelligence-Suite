# TC-BIL-031: Switching the engine off is always allowed and warns when no rule is in force

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-031 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.5) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-1](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-5](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
External Tax Engine is On. Case A: no approved internal rule is in force. Case B: at least one approved rule is in force.

## Steps
1. Case A: switch Off and confirm the dialog.
2. Read the status chip and the messages in the engine card.
3. Case B: switch On, then Off again, and read the status.

## Expected Result
The switch-off is never refused. The mode becomes "EIS internal tax engine". In case A the status is "Blocking invoices" and a warning says that invoices will be blocked until a rule is approved. In case B the status is "Ready".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxEngineSwitchTest.java` — `switchingOffIsAlwaysAllowedAndWarnsWhenNoRuleIsInForce`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "switches back off with its own confirmation", "says clearly when invoices are blocked"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
