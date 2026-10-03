# TC-SUB-021: Turning reminders off stops emails, not renewal

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-021 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A user with reminders off.

## Steps
1. Run the job inside the window.
2. Check the subscription; open Preferences and My subscriptions.

## Expected Result
No reminder is sent; auto-renew stays ON; the card says "Your subscriptions still renew; only the emails stop."; My subscriptions shows "Reminders are off." with a link to Preferences.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `usersCanTurnRemindersOffOrChooseTheirOwnDays`
- `frontend/src/components/preferences/RenewalRemindersCard.test.tsx`
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "shows auto-renew, the renewal date and the next reminder (REQ-SUB-004)"

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
