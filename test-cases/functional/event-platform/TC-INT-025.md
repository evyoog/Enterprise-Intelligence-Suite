# TC-INT-025: The daily job uses the configured retention

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-025 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.10) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.theDailyRetentionJobUsesTheConfiguredPeriod`) |

## Steps
1. Set retention to 5 days; have delivered events 6 and 2 days old; run the retention job.

## Expected Result
The 6-day-old event is removed; the 2-day-old event stays.

## Status
Passed (automated, 2026-10-12).
