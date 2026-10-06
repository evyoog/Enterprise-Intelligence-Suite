# TC-KNW-011: A new draft of published content publishes as 1.1; 1.0 stays viewable and comparable

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-011 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Edit a published item, submit, approve, publish as minor; compare 1.0 with 1.1.

## Expected Result
Readers saw 1.0 while the draft was edited; 1.1 is then live; the comparison shows the changed block.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `workflowRunsDraftReviewApprovedPublishedWithVersions`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
