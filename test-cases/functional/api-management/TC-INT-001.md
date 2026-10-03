# TC-INT-001: A key is shown once, stored only as a hash, and its creation is audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-001 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md), [AC-8](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Functional, Security |
| Automated | Yes |

## Preconditions
A signed-in customer with a Vyoog account.

## Steps
1. Account → Security → API keys → Create key, name "CRM sync".
2. Copy the key; Done.
3. Reload the list.

## Expected Result
The dialog shows the full key `eis_<8>_<40>` once with a warning; the list shows name, `prefix_…`, Active, created date — never the full key. The table stores the prefix and SHA-256 hash only. `API_KEY_CREATED` is audited with the prefix.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `theFullKeyIsShownOnceAndTheKeyActsAsItsOwner`
- `frontend/src/components/security/ApiKeysSection.test.tsx` — "creates a key and shows the full key once"

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
