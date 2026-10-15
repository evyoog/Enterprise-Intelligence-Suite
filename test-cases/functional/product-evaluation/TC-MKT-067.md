# TC-MKT-067: The catalog Compare tick and tray

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-067 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-19](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-CAT-315](../../../docs/02-requirements/FRD/catalog-showcase/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The catalog with several products; a signed-in or signed-out visitor.

## Steps
1. Tick Compare on one card, then a second, then up to four.
2. Look at the other cards.
3. Remove a chip, then Clear.
4. Reload the tab.
5. Click Compare now with two chosen.

## Expected Result
A tray appears with "Compare ({n})", the chosen products as chips, Clear and Compare now (disabled and "Choose at least 2 products" with one). With four chosen the other ticks are disabled ("You can compare up to 4 products."). The choice survives a reload in the same tab (and a broken stored value is ignored). Compare now opens `/compare?items=…`. The form preview and wishlist cards have no tick.

## Automated coverage
- `frontend/src/components/catalog/CompareTray.test.tsx` — "shows nothing until a card is ticked, then the count, the chosen products and a link that needs 2", "unticks a card, removes a chip and clears the tray", "allows at most 4: the unticked cards are disabled once 4 are chosen", "remembers the choice for the browser tab (sessionStorage) and survives a broken value", "falls back to a generic label for an unknown product"
- `frontend/src/components/catalog/CatalogComponents.test.tsx` — "shows a Compare tick only when asked to, and reports it"

The tray tests run twice, with a `CompareProvider` and standalone. **Not covered:** the tick inside the full `ProductsPage` (the page is tested through its parts).

## Actual Result
The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
