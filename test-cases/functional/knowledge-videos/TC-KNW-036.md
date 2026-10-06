# TC-KNW-036: Organization B cannot play organization A's restricted video

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-036 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Publish an S3 video for organization 801; request play URLs as 801, 802 and signed out.

## Expected Result
801 gets a 1-hour URL; others 404.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `organizationACannotPlayOrganizationBsRestrictedVideo`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
