# TC-SUB-054: Grace reminders go out daily and stop at the end of the local grace day

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-054 |
| Requirement ID (required) | [REQ-SUB-004](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md) (C100) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/renewal-reminders/acceptance-criteria.md) |
| Business rule(s) | [BR-6, BR-15, BR-18](../../../docs/02-requirements/FRD/renewal-reminders/business-rules.md) |
| Decision | [C100](../../../docs/01-business/roadmap/open-decisions.md#c100) |
| Test Plan | [TESTPLAN-SUB-004](TESTPLAN-SUB-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A recipient in Asia/Kolkata (default 09:00); a subscription whose term ended 10 Jan 2030 at 10:00 IST with auto-renew off and a grace end date of 17 Jan 10:00 IST.

## Steps
1. Run the reminder job at 12 Jan 09:30 IST, then again an hour later.
2. Mark the subscription EXPIRED and run the job at 15:30 IST on 17 Jan (after the grace instant).
3. Run the job on 18 Jan 09:30 IST.

## Expected Result
Step 1: one reminder with the title "Your {product} subscription has ended" and no second one that day. Step 2: the reminder for 17 Jan is still sent (the last local grace day), although the grace instant and the expiry have passed. Step 3: nothing is sent. A renewed subscription has no grace end date and gets none.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SeatsGraceAndRecipientsTest.java` — `graceRemindersGoOutDailyAndStopAtTheEndOfTheLocalGraceDayNotAtTheInstantItEnds`

Not covered by an automated test: the wording of the email (template `NOTIF_RENEWAL_GRACE_REMINDER`, "It is read-only until {date}…" / "It has expired; renew to get access back."), a recipient with reminders switched off, and a recipient in another time zone.

## Actual Result
The automated tests passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
