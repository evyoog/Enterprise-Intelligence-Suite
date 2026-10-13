# TC-INT-020: Replay re-sends delivered events of a range to one receiver

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-020 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.9, BR-9, BR-10 (EV-1 B)) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.replayResendsTheDeliveredEventsOfARangeToOneReceiverOnly`; UI: `EventReplayAndSettings.test.tsx` "previews the size of a replay and then sends it") |

## Steps
1. Deliver three events: two today (types A and B) and one ten days old.
2. Replay today to receiver 1; then replay only type B.

## Expected Result
Only the two events of the range reach receiver 1, oldest first; receiver 2 is not called; the events stay DELIVERED; one audit entry `EVENTS_REPLAYED` names the receiver and the counts. The type filter narrows it to one event.

## Status
Passed (automated, 2026-10-12).
