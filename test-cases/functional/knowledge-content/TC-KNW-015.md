# TC-KNW-015: Transitions outside the workflow are refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-015 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Publish a draft directly; change content in review as a contributor.

## Expected Result
409 INVALID_STATE / 403 PERMISSION_DENIED; nothing changes.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `workflowRunsDraftReviewApprovedPublishedWithVersions`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `contributorCannotChangeContentInReview`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
