# TC-KNW-030: No credentials in responses or the built bundle

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-030 |
| Requirement ID (required) | [REQ-KNW-003](../../../docs/02-requirements/FRD/knowledge-media/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/knowledge-media/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
Read storage information and media responses; search frontend/dist for AKIA, SECRET_ACCESS_KEY, X-Amz-Credential.

## Expected Result
No credentials or signing material anywhere.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `storageInformationNeverContainsCredentials`
- `backend/src/test/java/com/vyoog/eisplatform/modules/knowledgebase/service/KnowledgeStorageSecurityTest.java` — `presignedUrlsExpireAndNeverCarryTheSecret`

## Manual / UAT
- grep of frontend/dist on 2026-10-05: no match

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Automated part passed; manual part pending UAT
