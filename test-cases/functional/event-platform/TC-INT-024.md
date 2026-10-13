# TC-INT-024: Retention is 30 days by default and configurable

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-024 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.10, BR-11 (EV-3)) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.retentionStartsAtThirtyDaysIsConfigurableAndValidated`; UI: "shows the defaults and saves a new retention period") |

## Steps
1. Read the settings; save 90 days; try 0 and 3651.

## Expected Result
30 is the default; 90 is saved and audited (`EVENT_SETTINGS_CHANGED`); 0 and 3651 are refused and the value stays 90.

## Status
Passed (automated, 2026-10-12).
