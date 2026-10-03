# TC-PRT-011: Preferences — Notification categories on the existing opt-out

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-011 |
| Requirement ID (required) | [C67](../../../docs/01-business/roadmap/open-decisions.md#c67) — [preferences screen](../../../docs/05-ui/screen-requirements/preferences.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Signed-in user with no email opt-outs.

## Steps
1. Check In-app notifications.
2. Turn off Billing.
3. Turn off Security.
4. Turn off Email notifications.

## Expected Result
In-app shows "Always on". Billing saves BILLING and SUBSCRIPTION as opted out; turning Security off shows the warning; turning email off saves every category and disables the category switches.

## Automated coverage
- `frontend/src/pages/PreferencesPage.test.tsx` — `maps notification switches onto the existing email opt-out categories`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
