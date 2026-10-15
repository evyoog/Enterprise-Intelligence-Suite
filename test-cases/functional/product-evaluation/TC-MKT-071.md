# TC-MKT-071: The evaluation screens are accessible and translated

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-071 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-23](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | Not applicable (accessibility and translation) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Accessibility |
| Automated | Yes |

## Preconditions
The product page with the trial panel and the demo dialog open; My subscriptions with a trial; the catalog with a ticked card and the tray; the compare page, empty and filled.

## Steps
1. Run axe on each screen.
2. Operate the panel buttons, the dialog, the Compare tick, the tray and the compare controls with the keyboard only.
3. Switch the language to Spanish and look for untranslated text.

## Expected Result
No axe violations. Every control has a visible label or accessible name (the Compare tick names the product, the tray is a labelled region with a polite live count, the compare table has a caption, row and column headers and a focusable scroll region, the feature marks have hidden text). All text exists in `en.json` and `es.json`.

## Automated coverage
- `frontend/src/pages/ProductDetailPage.test.tsx` — "has no detectable a11y violations with the trial panel and the demo dialog"
- `frontend/src/pages/MySubscriptionsPage.test.tsx` — "has no detectable a11y violations"
- `frontend/src/components/catalog/CompareTray.test.tsx` — "has no a11y violations"
- `frontend/src/pages/ComparePage.test.tsx` — "has no a11y violations, empty or filled"

The keyboard and Spanish steps are manual. The `compare.*` (61 keys) and `evaluation.*` (41 keys) trees have the same keys in `en.json` and `es.json` (checked by a one-off script on 2026-10-15; no repository test checks key parity). **Not covered by an axe test:** the admin product form field, the organization subscriptions trial row.

## Actual Result
The axe tests passed in the automated run of 2026-10-15. The manual steps were not run.

## Status
Partly passed: axe automated run 2026-10-15; keyboard and Spanish checks Not Run

## Linked Defect (if failed)
None.
