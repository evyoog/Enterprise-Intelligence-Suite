# TC-KNW-020: A contributor may draft and submit but not publish or delete

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-020 |
| Requirement ID (required) | [REQ-KNW-008](../../../docs/02-requirements/FRD/knowledge-permissions/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/knowledge-permissions/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
As a KNOWLEDGE_CONTRIBUTE role create, submit, approve, publish, delete, edit taxonomy, open the search index.

## Expected Result
Create and submit work; the rest answer 403.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `contributorDraftsAndSubmitsButCannotPublishOrDelete`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `lets a contributor draft and submit, but shows no publisher actions`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
