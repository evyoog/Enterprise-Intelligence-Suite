# TC-KNW-048: Ask AI assistant says Coming soon

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-048 |
| Requirement ID (required) | [REQ-KNW-005](../../../docs/02-requirements/FRD/knowledge-center/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/knowledge-center/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | UI |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Click Ask AI assistant.

## Expected Result
A "Coming soon" dialog; no question is sent.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `knowledgeCenterIsPublicAndFeedbackNeedsSignIn`

## Manual / UAT
- UAT-KNW-004 step 6

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
