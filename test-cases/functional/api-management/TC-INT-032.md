# TC-INT-032: An organization key outlives its creator while an administrator remains

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-032 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization with two administrators; a key created by the first.

## Steps
1. Remove the creator from the organization (or suspend them).
2. Call the API with the key.
3. Remove the second administrator and call again.

## Expected Result
Step 2: 200, acting as the remaining administrator. Step 3: 401 and an audit entry `API_KEY_NO_ADMINISTRATOR`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ApiKeyAuthenticationTest.java` — `anOrganizationKeyOutlivesItsCreatorAsLongAsAnAdministratorRemains`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
