# TC-KNW-009: A contributor creates a draft and submits it for review

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-009 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
As a contributor create an item with blocks; submit it.

## Expected Result
Draft then In review; audited KNOWLEDGE_CONTENT_CREATED / _SUBMITTED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `workflowRunsDraftReviewApprovedPublishedWithVersions`
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `contributorDraftsAndSubmitsButCannotPublishOrDelete`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `lets a contributor draft and submit, but shows no publisher actions`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
