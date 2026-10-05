# TC-KNW-018: Scheduled publishing happens when its time comes

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-018 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | REQ-KNW-002.5 |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Approve an item and schedule it for later.

## Expected Result
It stays Scheduled until the time, then version 1.0 is live.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `scheduledPublishingHappensWhenItsTimeComes`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
