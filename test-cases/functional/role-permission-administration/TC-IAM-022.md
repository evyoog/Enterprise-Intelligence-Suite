# TC-IAM-022: Role and Permission Administration — AC-10

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-022 |
| Requirement ID (required) | [REQ-IAM-003](../../../docs/02-requirements/FRD/role-permission-administration/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/role-permission-administration/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-003](TESTPLAN-IAM-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The role and permission pages are shown.

## Steps
1. Arrange: the role and permission pages are shown.
2. Observe the response and the UI.

## Expected Result
Their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/pages/admin/RolesAdminPage.test.tsx` — "has no detectable a11y violations"
- `frontend/src/pages/admin/PermissionsAdminPage.test.tsx` — "has no detectable a11y violations"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
