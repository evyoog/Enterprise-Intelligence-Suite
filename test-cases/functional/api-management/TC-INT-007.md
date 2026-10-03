# TC-INT-007: API key screens are accessible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-INT-007 |
| Requirement ID (required) | [REQ-INT-001](../../../docs/02-requirements/FRD/api-management/requirement.md) (C61) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/api-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-INT-001](TESTPLAN-INT-001.md) |
| Priority | P2 |
| Type | Accessibility |
| Automated | Yes |

## Preconditions
Key list with one key.

## Steps
1. Run axe on the section and on the admin page; open the revoke confirmation.

## Expected Result
No violations; dialogs are labelled by their titles; the one-time key is a labelled read-only field.

## Automated coverage
- `frontend/src/components/security/ApiKeysSection.test.tsx`
- `frontend/src/pages/admin/AdminApiKeysPage.test.tsx`

## Actual Result
The automated tests passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
