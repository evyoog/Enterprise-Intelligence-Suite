# TC-KNW-007: Existing articles move to the content model unchanged

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-007 |
| Requirement ID (required) | [REQ-KNW-001](../../../docs/02-requirements/FRD/knowledge-base/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/knowledge-base/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Have a published article (id, title, body, version 4). Start the backend (KnowledgeSeeder) or apply V021. Open it in the Knowledge Center and in Knowledge Management.

## Expected Result
Same id, title, body and version; type Article; live version "4.0"; Public; still returned by GET /knowledge-base/articles/{id}.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `existingArticlesKeepIdTitleBodyStatusAndVersion`

## Manual / UAT
- V021 applied twice to a copy of the old schema with a published and a draft article on 2026-10-05: both kept, the published one got live version 3.0; second run changed nothing

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
