# TC-TEN-010: Organization Lifecycle — AC-10

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-010 |
| Requirement ID (required) | [REQ-TEN-001](../../../docs/02-requirements/FRD/organization-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/organization-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-001](TESTPLAN-TEN-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The Organizations tab.

## Steps
1. Arrange: the Organizations tab.
2. Act: it is shown.
3. Observe the response and the UI.

## Expected Result
The new text exists in `en.json` and `es.json`, the controls are keyboard operable with accessible names, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/pages/admin/RegistrationsAdminPage.test.tsx` — "has no detectable a11y violations"

**Manual check:** Switch the UI to Spanish and check the new text; operate the dialogs with the keyboard only.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
