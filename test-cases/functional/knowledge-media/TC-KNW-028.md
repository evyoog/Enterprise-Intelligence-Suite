# TC-KNW-028: An expired presigned URL fails

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-028 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Issue a download URL; wait past its expiry; open it.

## Expected Result
S3 answers 403 Request has expired.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeStorageSecurityTest.java` — `presignedUrlsExpireAndNeverCarryTheSecret`

## Manual / UAT
- needs a real bucket — UAT-KNW-003 step 5

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
