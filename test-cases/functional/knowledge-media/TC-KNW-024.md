# TC-KNW-024: Upload URLs are presigned PUTs for new generated keys

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-024 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Request an upload URL for "../../Q3 manual.pdf".

## Expected Result
Key documents/manuals/{product}/{module}/{uuid}.pdf (never the file name); URL expires in 15 minutes.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeMediaAndVideoServiceTest.java` — `uploadUrlUsesAGeneratedKeyAndShortExpiryAndChecksTypeAndSize`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeStorageSecurityTest.java` — `presignedUrlsExpireAndNeverCarryTheSecret`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
