# TC-KNW-031: Uploads never completed are cleaned up

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-031 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Leave an upload pending past eis.knowledge.orphan-after; run the cleanup job.

## Expected Result
The object is deleted and the item marked Failed; audited.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `uploadsNeverCompletedAreRemovedByTheCleanupJob`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
