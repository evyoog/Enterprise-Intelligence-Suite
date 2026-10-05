# TC-KNW-010: A publisher approves and publishes; readers see version 1.0 and search finds it

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-010 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Approve and publish an item in review; open it signed out; search for its title.

## Expected Result
Version 1.0 is live and visible to its audience and found by search.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `workflowRunsDraftReviewApprovedPublishedWithVersions`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeAnalyticsAndSearchTest.java` — `publicKnowledgeOfEveryTypeIsSearchableButRestrictedIsNotInGlobalSearch`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `publishers see review and publish actions with minor or major versions`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
