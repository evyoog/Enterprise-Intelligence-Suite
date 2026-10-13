# TC-INT-030: Only organization administrators create keys, and the key belongs to the organization

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-030 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P0 |
| Type | Security / Functional |
| Automated | Yes |

## Preconditions
An organization with an administrator and a plain member; a second organization with an administrator.

## Steps
1. The member tries to create a key.
2. The administrator creates a key named "CRM sync" with scope READ.
3. Both administrators of the organization list the keys; the other organization lists its own.
4. Use the new key to create or revoke a key.

## Expected Result
Step 1: 403. Step 2: 201 with the full key once. Step 3: the key is listed for every administrator of the organization and not for the other organization. Step 4: 403 (a key cannot create or revoke keys). Creation is audited with the creator and the organization.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `onlyOrganizationAdministratorsCreateKeysAndOnlyForTheirOwnOrganization`, `aKeyCannotCreateOrRevokeKeys`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
