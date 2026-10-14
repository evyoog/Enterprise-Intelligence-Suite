# TC-SUP-007: Business-hours arithmetic: Monday to Friday 09:00 to 18:00, Asia/Kolkata

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-007 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-002, BR-SLA-003, BR-SLA-004](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Business hours Monday to Friday, 09:00 to 18:00, Asia/Kolkata (2026-10-12 is a Monday).

## Steps
1. Add 240 minutes to Monday 10:00.
2. Add 240 minutes to Friday 16:00.
3. Add 60 minutes to Saturday 12:00, Monday 07:00 and Monday 18:00.
4. Add 1440 minutes to Monday 09:00.
5. Add 60 minutes around the clock to Saturday 23:30; and 80% of 300 minutes to Monday 10:00.
6. Build a calculator with no day, and with a start after the end.

## Expected Result
Monday 14:00; Monday 11:00; Monday 10:00, Monday 10:00 and Tuesday 10:00; Wednesday 15:00; Sunday 00:30 and Monday 14:00 (and 10:48 for 80% of an hour 24x7); the invalid calculators are refused.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaCalculatorTest.java` — `withinTheSameBusinessDay`, `spillsOverTheEndOfTheDayAndTheWeekend`, `aTicketCreatedOutsideBusinessHoursStartsAtTheNextOpening`, `severalDays`, `aroundTheClockIgnoresBusinessHours`, `theWarningPointIsAPercentageOfTheTarget`, `businessHoursNeedADayAndAStartBeforeTheEnd`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
