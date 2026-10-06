# TC-KNW-012: Restoring an old version creates a new draft and keeps history

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-012 |
| Requirement ID (required) | [REQ-KNW-002](../../../docs/02-requirements/FRD/knowledge-content/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/knowledge-content/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Restore version 1.0 of an item with versions 1.0, 1.1, 2.0.

## Expected Result
The working copy holds 1.0 content in Draft; three versions still listed.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `workflowRunsDraftReviewApprovedPublishedWithVersions`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
