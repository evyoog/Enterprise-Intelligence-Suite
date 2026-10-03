# TC-INT-011: An event is written with its business change, and only if the change commits

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-011 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md), [AC-2](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md), [AC-3](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The backend test context; the scheduled dispatcher off.

## Steps
1. Publish an event inside a transaction that commits.
2. Publish another inside a transaction that is rolled back.
3. Read both aggregates' events.

## Expected Result
One PENDING event exists for the committed change, with a UUID event ID, type, aggregate, occurred-at, JSON payload (IDs and statuses only) and attempts 0. None exists for the rolled-back change.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `anEventIsWrittenOnlyWhenItsTransactionCommits`, `aDueEventIsDeliveredToItsHandlersAndReceiptsAreRecorded`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
