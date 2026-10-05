# TC-KNW-034: Hosted video upload with progress, verified before saving

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-034 |
| Requirement ID (required) | [REQ-KNW-004](../../../docs/02-requirements/FRD/knowledge-videos/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/knowledge-videos/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Upload an MP4 within the limit; watch progress; save the video.

## Expected Result
Progress, speed, remaining and Cancel shown; saved after verification.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `organizationACannotPlayOrganizationBsRestrictedVideo`
- `frontend/src/pages/knowledge-admin/KnowledgeManagement.test.tsx` — `shows upload progress with speed, remaining size and cancel`

## Manual / UAT
- browser → S3 with a real bucket — UAT-KNW-003

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
