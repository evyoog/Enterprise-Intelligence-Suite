# TC-INT-023: Replay checks the receiver, the range and the size

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-023 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.9, BR-9) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.replayValidatesTheReceiverTheRangeAndTheSize`; UI: "does not allow a replay that is too large", "refuses a range that ends before it starts") |

## Steps
1. Replay to an unknown or empty receiver, with a missing day, or with the end before the start.
2. Match more events than the limit (3 in the test, 1000 by default); preview and replay.

## Expected Result
Unknown receiver is 404, the others are 400. The preview reports the count and the limit; the real replay is refused ("limited to") and nothing is sent.

## Status
Passed (automated, 2026-10-12).
