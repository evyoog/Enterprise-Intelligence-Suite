# TC-KNW-019: Readers cannot create, upload, publish or delete

| Field | Value |
|---|---|
| Test Case ID (required) | TC-KNW-019 |
| Requirement ID (required) | [REQ-KNW-008](../../../docs/02-requirements/FRD/knowledge-permissions/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/knowledge-permissions/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-KNW-002](../knowledge-base/TESTPLAN-KNW-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
Seed data loaded (C74). Storage replaced by the in-memory store in automated tests.

## Steps
As a customer call create, upload-url (media and video), publish and delete.

## Expected Result
401 signed out, 403 signed in; nothing changes.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/KnowledgeAuthorizationTest.java` — `readersCannotCreateUploadPublishOrDelete`

## Actual Result
The automated tests above passed on 2026-10-05.

## Status
Passed (automated)
