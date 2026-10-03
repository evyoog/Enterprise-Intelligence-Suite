# TC-INT-016: Delivered events older than the retention period are removed

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-016 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P2 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A DELIVERED event delivered 31 days ago and one delivered 2 days ago.

## Steps
1. Run retention with 30 days.

## Expected Result
Only the 31-day-old event is removed. FAILED and PENDING events are never removed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `deliveredEventsOlderThanTheRetentionPeriodAreRemoved`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
