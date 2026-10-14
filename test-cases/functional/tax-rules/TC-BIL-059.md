# TC-BIL-059: The Tax configuration screen: states, keyboard and screen-reader access

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-059 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.1, .2) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-6](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator. Cases: normal data; the status or reference API failing; an empty system (no rules, classes, exemptions, registrations, Draft invoices).

## Steps
1. Open `/admin/billing/tax` and watch the loading state.
2. Make the status request fail and read the error.
3. Open each tab in an empty system.
4. Operate the switch, the tabs, a dialog and a row action with the keyboard only, and with a screen reader.
5. Open `/admin/billing/tax?tab=blocked` directly and use the browser Back button after switching tabs.

## Expected Result
Step 1: a skeleton, then the engine card and tabs. Step 2: an error alert with the server message (or "Tax configuration could not be loaded.") and a Try again button. Step 3: each tab shows its own empty message. Step 4: the switch has the accessible name "External Tax Engine" and a visible text state; tabs have labels, `aria-controls` and a tab panel; dialogs have titles and errors use `role="alert"`; status is never colour only. Step 5: the tab is kept in the URL. The page has no axe violations.

## Automated coverage
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "shows the External Tax Engine switch, the active mode and its status", "shows an error with a retry when the configuration cannot be loaded", "shows an empty state when nothing is waiting"

Not automated: The axe check is in the first test above (`axe(container)`). Keyboard-only, screen-reader and URL/back-button checks are by hand.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
