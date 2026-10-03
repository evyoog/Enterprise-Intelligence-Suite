# TC-SUB-019: Daily reminders at the send time in the recipient's time zone, once per day

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-019 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-4](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-8](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-11](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A customer in Asia/Kolkata with 7 days, 09:00; a renewal at 10:00 IST.

## Steps
1. Run the reminder job at 08:30 IST and 09:30 IST seven days before, later that day, the next day, eight days before and on the renewal day.
2. Check the email (manual).

## Expected Result
Nothing at 08:30; one reminder at 09:30; none again that day; one the next day; none outside 1–7 days; each logged once and published (`RenewalReminderSent`) and audited. For a New York customer the same instant falls on a different local date. Once renewed, the renewal date moves on and no reminders remain for the old date. Email (manual): product, plan, date, amount, "A renewal invoice will be issued…", and the three links.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `aReminderIsSentOncePerDayAtTheSendTimeInTheRecipientsTimeZone`, `theSameInstantIsADifferentLocalDayInAnotherTimeZone`

## Actual Result
The automated tests passed on 2026-10-03. The manual steps have not been run yet.

## Status
Automated part passed (2026-10-03); manual part not yet run

## Linked Defect (if failed)
None.
