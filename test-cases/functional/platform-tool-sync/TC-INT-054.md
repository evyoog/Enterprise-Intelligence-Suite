# TC-INT-054: A stopped tool does not delay the others; failures are retried, then FAILED with the reason; Retry and Replay

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-054 |
| Requirement ID (required) | [REQ-INT-003](../../../docs/02-requirements/FRD/platform-tool-sync/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/platform-tool-sync/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-003](TESTPLAN-INT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two connected tools; one is stopped. The backend test context (H2, scheduled jobs off); the tool is a fake gateway unless real MCP is named.

## Steps
1. Change the organization; run the delivery.
2. Start the stopped tool; run again.
3. Make a tool answer `retry` three times (max attempts 3), and another call `rejected`.
4. Use Retry on the FAILED message and Replay on a delivered one.

## Expected Result
The running tool receives the message at once; the stopped tool's first message is tried once (one attempt, next try scheduled), its other messages are skipped for that run, and nothing waits for it. After it starts, everything is delivered. A message that keeps failing is FAILED after the last attempt with its reason and attempts; a rejection is FAILED at once with the tool's reason. Retry puts a FAILED message back to PENDING with attempts 0; Replay queues the same object again as a new message that carries the current state, and is coalesced when one already waits.

## Automated coverage
- `.../toolsync/ToolDeliveryTest.aStoppedToolDelaysNobodyElseAndItsMessagesWaitForTheirRetry`, `.aMessageThatKeepsFailingIsRetriedAndThenFailedWithItsReasonUntilAnAdminRetriesIt`, `.aRejectionIsFinalAtOnceAndKeepsTheToolsReason`, `.replayDeliversTheAggregateAgainAsANewMessage`
- `.../toolsync/AdminToolSyncTest.retryAndReplayAreAuditedAndRetryOnlyAppliesToAFailedDelivery`

## Actual Result
The automated tests passed on 2026-10-09 (`cd backend && mvn -B test`). Against a **real** Macro Planner (not the simulator) the same behaviour is checked in phase 8.

## Status
Passed (automated run 2026-10-09)

## Linked Defect (if failed)
None.
