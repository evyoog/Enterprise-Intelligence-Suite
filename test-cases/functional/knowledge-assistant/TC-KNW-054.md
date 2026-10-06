# TC-KNW-054: The ask endpoint answers not configured

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-054 |
| Requirement ID (required) | [REQ-KNW-007](../../../docs/02-requirements/FRD/knowledge-assistant/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-assistant/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | API |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
POST /knowledge/assistant/ask.

## Expected Result
501 ASSISTANT_NOT_CONFIGURED; GET /knowledge/assistant/status → configured false.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `knowledgeCenterIsPublicAndFeedbackNeedsSignIn`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
