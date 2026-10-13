# TC-CAT-052: The product card shows badges, rating, tags, From price and the heart

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-052 |
| Requirement ID (required) | [REQ-CAT-003](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md) (C94) |
| Acceptance Criterion | [AC-9, AC-11, AC-14](../../../docs/02-requirements/FRD/catalog-showcase/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-CAT-003](TESTPLAN-CAT-003.md) |
| Priority | P1 |
| Type | Functional / UI |
| Automated | Yes |

## Preconditions
A featured product added recently with reviews; one without reviews and with a free plan.

## Steps
1. Render both cards. 2. Click the heart and Subscribe; check Add to cart without a paid plan. 3. Run the accessibility check, in grid and list view.

## Expected Result
Featured and New badges, rating with count, category and platform, From price (no feature tags); no rating, no New and Free for the second; the heart announces saved or not; Add to cart disabled without a paid plan; no axe violations.

## Automated coverage
- `frontend/src/components/catalog/CatalogComponents.test.tsx` — `CatalogProductCard` tests

## Actual Result
The automated tests passed on 2026-10-13.

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
