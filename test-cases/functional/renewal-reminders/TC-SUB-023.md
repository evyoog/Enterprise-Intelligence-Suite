# TC-SUB-023: Renewal screens are accessible and show the next reminder

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-023 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P2 |
| Type | Accessibility |
| Automated | Yes |

## Preconditions
Reminder settings, renewal data and admin defaults loaded.

## Steps
1. Run axe on the Renewal reminders card, the admin tab and My subscriptions.
2. Check the next reminder for a 7-day, 08:30 setting ten days before renewal.

## Expected Result
No violations. The next reminder is 7 days before at 08:30 in the user's time zone (`2030-01-03T08:30+05:30` in the test).

## Automated coverage
- `frontend/src/components/preferences/RenewalRemindersCard.test.tsx`
- `frontend/src/components/settings/RenewalReminderDefaultsPanel.test.tsx`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `myRenewalsShowTheNextReminder`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
