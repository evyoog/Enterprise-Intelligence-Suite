# TC-CAT-012: Catalog Showcase — Showcase fields are validated and stored

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-012 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `MANAGE_CATALOG`.

## Steps
1. Save a platform with colour `red`, status `PAUSED`, display order 10000.
2. Save an app with a tag containing a comma and a URL `ftp://x`.
3. Save valid values with a lower-case colour and duplicate tags.

## Expected Result
Steps 1–2 return 400. Step 3 stores the colour upper-case and the tags de-duplicated.

## Automated coverage
- `backend/.../platform/controller/CatalogControllerTest.java` — `showcaseFieldsAreValidatedAndStored`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
