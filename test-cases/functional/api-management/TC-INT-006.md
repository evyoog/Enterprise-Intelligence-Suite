# TC-INT-006: Administrators see key usage; keys never reach platform administration

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-006 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional, Security |
| Automated | Yes |

## Preconditions
A platform administrator; a key created by an administrator.

## Steps
1. Open Admin → Integrations → API keys.
2. Call `/admin/events` with the administrator's key.

## Expected Result
Each key shows owner email, name, prefix, status, created, last used and requests; without `MANAGE_INTEGRATIONS` 403. The administrator's key gets 403 on `/admin/**` (ROLE_ADMIN is never copied to a key).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `administratorsSeeEveryKeysUsage`, `aKeyNeverReachesPlatformAdministrationEvenForAnAdmin`
- `frontend/src/pages/admin/AdminApiKeysPage.test.tsx`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
