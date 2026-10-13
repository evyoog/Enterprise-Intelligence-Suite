# TC-INT-021: Replay preview, and only delivered events

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-021 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.9, BR-9) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.aPreviewOnlyCountsAndPendingOrFailedEventsAreNotReplayed`) |

## Steps
1. Have one delivered, one pending and one failed event today.
2. Preview a replay of that type.

## Expected Result
The preview counts only the delivered event, calls nobody and writes no audit entry.

## Status
Passed (automated, 2026-10-12).
