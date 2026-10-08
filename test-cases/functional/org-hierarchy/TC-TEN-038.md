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
1. Open Organization → Structure; the org chart shows the root and the first two levels; expand and collapse a branch, search, open Details, add a node without selecting anything (it goes under the root), refuse a duplicate, open the root's ⋯ menu, open Import CSV and check the format and sample.
2. Switch the language to Spanish.

## Expected Result
The chart, search and dialogs work, the backend's reason is shown on refusal, the root offers no Move/Delete/Deactivate, there are no accessibility violations, and all text is translated.

## Automated coverage
- `frontend/src/pages/OrganizationStructurePage.test.tsx` (including `jest-axe`) and `frontend/src/api/orgHierarchyApi.test.ts` (a root whose null parent the server omits is still the root)
- `frontend/src/components/layout/appNavigation.test.ts` (sidebar entry for organization administrators)

## Status
Passed (2026-10-07). Spanish text checked by key parity of `en.json` and `es.json`.
