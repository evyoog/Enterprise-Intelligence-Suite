# TC-INT-013: A failing handler is retried with a doubling delay and the event fails after the maximum attempts

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-013 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (C62) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A handler that throws for the test aggregate.

## Steps
1. Publish an event; run the dispatcher.
2. Run it again before the next attempt time.
3. Set attempts to 9 and make it due; run the dispatcher.

## Expected Result
After the first run: PENDING, attempts 1, last error names the handler, next attempt about 30 s later. The early run changes nothing. After the tenth attempt: FAILED with no next attempt. Delays: 30 s, 1 min, 2 min … capped at 6 h.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/integration/service/OutboxDispatcherTest.java` — `aFailureIsRetriedLaterAndEventuallyFails`, `theRetryDelayDoublesAndIsCapped`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
