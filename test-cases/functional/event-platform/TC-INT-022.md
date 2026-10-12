# TC-INT-022: A failing event does not stop the replay

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-022 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.9) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.aFailingEventDoesNotStopTheReplayAndIsReported`) |

## Steps
1. Replay two events where the receiver fails on the second.

## Expected Result
The first is replayed, the second is counted as failed and listed with the error; the replay finishes.

## Status
Passed (automated, 2026-10-12).
