# TC-INT-012: A due event is delivered once to each handler; an event with no handler is delivered

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-012 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two test handlers registered for test event types.

## Steps
1. Publish a test event.
2. Run the dispatcher.
3. Run it again.
4. Publish an event no handler declares; run the dispatcher.

## Expected Result
The event is DELIVERED with attempts 1 and one receipt per handler; the second run delivers nothing again. The unhandled event is DELIVERED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `aDueEventIsDeliveredToItsHandlersAndReceiptsAreRecorded`, `anEventWithNoHandlerIsDelivered`, `aHandlerThatAlreadySucceededIsNotCalledAgainOnRetry`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
