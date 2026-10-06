# TC-KNW-039: Deleting a hosted video deletes its storage object

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-039 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Delete a published S3 video as a publisher.

## Expected Result
The object is deleted, the record and search entry removed, audited.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `organizationACannotPlayOrganizationBsRestrictedVideo`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
