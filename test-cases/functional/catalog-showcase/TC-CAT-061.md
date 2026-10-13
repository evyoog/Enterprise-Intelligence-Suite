# TC-CAT-061: The app form sends both images with the app and previews them

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-061 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C95) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
The Create App form.

## Steps
1. Enter a name and price. 2. Upload an app icon and a banner image. 3. Check the Showcase Preview. 4. Click Create App.

## Expected Result
The preview card shows the banner and the icon; the submitted app has `imageUrl` and `bannerImageUrl`.

## Automated coverage
- `frontend/src/components/admin/ShowcaseForms.test.tsx` — `uploads an app icon and a banner image and sends both with the app`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
