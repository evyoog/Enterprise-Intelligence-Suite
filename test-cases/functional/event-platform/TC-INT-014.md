# TC-INT-014: Events of one aggregate are delivered in order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-014 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A handler that fails for the aggregate until told to succeed.

## Steps
1. Publish two events for the same aggregate.
2. Run the dispatcher while the first fails.
3. Let the handler succeed; make the first due; run again.

## Expected Result
While the first is undelivered, the second is not attempted. Then both are DELIVERED, first before second.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `aLaterEventOfTheSameAggregateWaitsForTheEarlierOne`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
