# TC-TEN-038: Organization hierarchy — screen (AC-12)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-038 |
| Requirement ID (required) | [REQ-TEN-006](../../../docs/02-requirements/FRD/org-hierarchy/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/org-hierarchy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-006](TESTPLAN-TEN-006.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Open Organization → Structure; expand a node, search, add a child (with a backend refusal), open the root.
2. Switch the language to Spanish.

## Expected Result
The tree, search and dialogs work, the backend's reason is shown on refusal, the root offers no Move/Delete/Deactivate, there are no accessibility violations, and all text is translated.

## Automated coverage
- `frontend/src/pages/OrganizationStructurePage.test.tsx` (6 tests, including `jest-axe`)
- `frontend/src/components/layout/appNavigation.test.ts` (sidebar entry for organization administrators)

## Status
Passed (2026-10-07). Spanish text checked by key parity of `en.json` and `es.json`.
