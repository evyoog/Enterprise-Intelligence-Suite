# TC-INT-035: Organization API key screens are accessible and create keys with a scope

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-035 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C92) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P1 |
| Type | Accessibility / UI |
| Automated | Yes |

## Preconditions
Signed in as an organization administrator.

## Steps
1. Open Organization → Sign-in security → API keys.
2. Create a key choosing *Read-write*; copy it from the dialog.
3. Revoke a key.
4. Run the accessibility check.

## Expected Result
The list shows scope and creator; the create call carries the chosen scope; the full key is shown once; a refusal (for example the limit of 10) is shown in the dialog; revoke asks for confirmation; no axe violations.

## Automated coverage
- `frontend/src/components/organization/OrganizationApiKeysSection.test.tsx`, `frontend/src/components/security/ApiKeysSection.test.tsx`, `frontend/src/pages/admin/AdminApiKeysPage.test.tsx`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
