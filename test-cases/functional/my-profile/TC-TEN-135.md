# TC-TEN-135: My profile — accessibility

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-135 |
| Requirement ID (required) | [REQ-TEN-011](../../../docs/02-requirements/FRD/my-profile/requirement.md) (BR-PRF-024) |
| Acceptance Criterion | [AC-18](../../../docs/02-requirements/FRD/my-profile/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-011](TESTPLAN-TEN-011.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The profile page, the global bar and the admin dialog.

## Steps
1. Run the automated accessibility check (jest-axe) on each tab, with errors showing, and on the bar.
2. Operate the tabs, the photo buttons, the missing-items disclosure and the dialog by keyboard only.
3. Check the names announced for the progress bars, the error badges and the photo buttons.

## Expected Result
No accessibility violations. Every input has a label; an invalid input has `aria-invalid` and its message in the helper text; arrow keys, Home and End move between tabs; focus is trapped in the dialog and returns on close; the progress bars announce a name and value; the error badge is announced with the tab name.

## Automated coverage
- `frontend/src/pages/ProfilePage.test.tsx` — `has no detectable a11y violations`
- `frontend/src/components/layout/AppShell.test.tsx` — `has no detectable a11y violations`

## Actual Result
Not run yet.

## Status
Not Run
