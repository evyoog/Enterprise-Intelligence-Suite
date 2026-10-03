# TC-CAT-015: Catalog Showcase — Platform form live preview and validation

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-015 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator on Create Platform.

## Steps
1. Submit the empty form.
2. Type a name, choose Custom colour → Green, turn off Show in catalog.
3. Submit.

## Expected Result
Step 1 shows "This field is required." and does not submit. Step 2 updates the preview name, marks Green pressed and shows "Hidden from catalog". Step 3 submits `primaryColor` #10B981, `showInCatalog` false, `displayOrder` 0 and shows "Saved".

## Automated coverage
- `frontend/src/components/admin/ShowcaseForms.test.tsx` — `updates the live preview and submits the showcase settings`, `shows validation only after the first submit and does not submit invalid data`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
