# TC-CAT-062: The compact card shows the banner and icon, falls back, and is not clickable in the form preview

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-062 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C95) |
| Acceptance Criterion | [AC-16](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
Cards with and without a banner image and an icon.

## Steps
1. Render a card with both images. 2. Render one with neither. 3. Render the form preview.

## Expected Result
Banner then icon images in that order; initials without an icon; no feature tags; in the preview Subscribe, Add to cart and the heart are disabled and the name is not a link.

## Automated coverage
- `frontend/src/components/catalog/CatalogComponents.test.tsx` — `shows the banner image behind the card…`, `shows the initials when there is no icon…`

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
