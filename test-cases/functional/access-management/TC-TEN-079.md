# TC-TEN-079: Access management — Access management screen

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-079 |
| Requirement ID (required) | [REQ-TEN-005](../../../docs/02-requirements/FRD/access-management/requirement.md) (REQ-TEN-005 UI, AC-21) |
| Acceptance Criterion | [AC-5, AC-13, AC-21](../../../docs/02-requirements/FRD/access-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-005](TESTPLAN-TEN-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessManagement.test.tsx`, 11 tests including axe on the list, the panel, the bulk dialog and the Activity tab) |

## Steps
1. Open Organization → Access management; open a member's panel; stage a product and a permission change; Save.
2. Open an administrator's panel as a delegate; open Role defaults; open Activity.

## Expected Result
Switches are real `role="switch"` controls with labels; locked switches show the reason next to a lock; changes are staged ("2 unsaved changes") and saved together; the read-only panel has no Save bar; the backend message appears on a refusal; every view passes the accessibility check. Spanish text is shown for the Spanish locale.

## Status
Passed (automated, 2026-10-12).
