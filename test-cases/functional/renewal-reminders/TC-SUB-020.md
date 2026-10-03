# TC-SUB-020: Users choose their own days and time within the allowed range

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-020 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C64) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-6](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md), [AC-10](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A user with 3 days; a user with a 04:00 time.

## Steps
1. Run the job 5 and 3 days before.
2. Save 31 days, then 45 on screen.

## Expected Result
Only from 3 days before for that user; the time is honoured in their zone. 31 is refused (400 / field error "Enter a number from 1 to 30."); a time like 25:00 is refused.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/RenewalAndRemindersTest.java` — `usersCanTurnRemindersOffOrChooseTheirOwnDays`, `theSameInstantIsADifferentLocalDayInAnotherTimeZone`
- `frontend/src/components/preferences/RenewalRemindersCard.test.tsx` — "saves own days and refuses an out-of-range value"

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
