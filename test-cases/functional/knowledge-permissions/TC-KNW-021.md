# TC-KNW-021: A publisher runs the whole workflow, audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-021 |
| Requirement ID (required) | [REQ-KNW-008](../../../docs/02-requirements/FRD/knowledge-permissions/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-permissions/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
As ADMIN approve, publish, unpublish, archive, restore and delete.

## Expected Result
Each succeeds and is in the audit log.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `contributorDraftsAndSubmitsButCannotPublishOrDelete`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeContentServiceTest.java` — `deprecatedStaysVisibleWithBannerAndArchivedIsHidden`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
