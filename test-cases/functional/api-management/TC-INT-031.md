# TC-INT-031: A read-only key can only read; a read-write key can also write

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-031 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P0 |
| Type | Security |
| Automated | Yes |

## Preconditions
One READ key and one READ_WRITE key of the same organization.

## Steps
1. Call a `GET` endpoint with each key.
2. Call a `POST` (and `PUT`/`PATCH`/`DELETE`) endpoint with each key.

## Expected Result
Both keys get 200 on `GET`. The READ key gets 403 with code `API_KEY_READ_ONLY` on the write call and nothing changes; the READ_WRITE key succeeds.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `aReadOnlyKeyCanOnlyReadAndAReadWriteKeyCanAlsoWrite`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
