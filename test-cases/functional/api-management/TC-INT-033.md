# TC-INT-033: A legacy personal key keeps working as read-write but cannot be created any more

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-033 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A personal key created before the change (no organization).

## Steps
1. Call the API with a write request using the personal key.
2. List `/me/api-keys` and revoke the key.
3. `POST /me/api-keys`.

## Expected Result
Step 1: succeeds (read-write). Step 2: the key is listed as personal and can be revoked. Step 3: 403 "API keys belong to your organization…".

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `aLegacyPersonalKeyKeepsWorkingWithFullRightsButCannotBeCreatedAnymore`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
