# TC-INT-026: An event that fails for good alerts the listed people

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-026 |
| Requirement ID (required) | [REQ-INT-002](../../../docs/02-requirements/FRD/event-platform/requirement.md) (REQ-INT-002.11, BR-12 (EV-4 both)) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/event-platform/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-002](TESTPLAN-INT-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`EventReplayAndSettingsTest.anEventThatFailsForGoodAlertsTheListedPeopleInTheAppAndByEmail`, `alertRecipientsAreValidated`; UI: "warns about events that failed for good") |

## Steps
1. List one address with an account and one without (given with different case and twice); let an event use its last attempt.
2. Open the Platform events screen.

## Expected Result
The addresses are normalized. The person with an account gets an in-app notice "Event failed: …" and an email; the other gets the email. Invalid addresses and more than 20 are refused. The screen shows a banner with the failed count and Show failed filters the table.

## Status
Passed (automated, 2026-10-12).
