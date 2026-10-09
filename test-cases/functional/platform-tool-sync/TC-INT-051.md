# TC-INT-051: A subscription end is stored and sent as 23:59:00.000 +05:30, and V028 moves existing values safely

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-051 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A subscription whose last day is 31 Oct 2026 (also a month-end and a leap day); an existing row stored at UTC midnight; the V028 migration and its support scripts. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Create or renew a subscription whose last day is 2026-10-31.
2. Read `expires_at` and the message the tool receives.
3. Run `database/support/V028_dry_run.sql` on a copy holding old values, apply V028 twice, then run `V028_reverse.sql`.

## Expected Result
`expires_at` is `2026-10-31 18:29:00` UTC, which is 23:59:00 in India on the same date; the message carries `endsAt` = `2026-10-31T23:59:00.000+05:30`; a subscription is in force from `startedAt` to that instant, inclusive, and not a minute after. V028 lists the rows it will change, changes each to the same India date at 23:59:00, is idempotent, and the reverse script restores the copied values exactly for the rows it touched.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionClockTest.java` (7 tests: month ends, leap day, India date near UTC midnight, inclusive end, format)
- `.../registration/service/RenewalAndRemindersTest.java` (renewal dates use the end of day)
- `.../toolsync/ToolDeliveryTest.aSubscriptionEndIsSentAsEndOfDayInIndiaWhateverTimeWasStored`
- V028 dry run, apply twice and reverse were run by hand on PostgreSQL 16 on 2026-10-09 (see the plan's phase 7 notes)

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
