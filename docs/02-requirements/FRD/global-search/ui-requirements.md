# UI requirements — Global Search

## Screens
| Screen | Route | Roles | Wireframe |
|--------|-------|-------|-----------|
| Global search | `/search` | Public (signed in or not) | Not specified |

A single search box with three result sections (Products, Knowledge base, Your tickets), each hidden when empty. The current query is reflected in the URL (`?q=`) so a search is shareable/bookmarkable, same convention as the product catalog page's own debounced-search pattern.

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Query | Text | No (blank = everything) | - | - |

## States
- Empty: "Nothing matches your search yet." (`search.noResults`) — shown once a search has actually run and every section came back empty
- Loading: handled silently (results simply appear once loaded)
- Error: treated as "nothing found" rather than shown as an error — this is not critical-path data

## Accessibility and localization
- Each result section heading uses `component="h5"` to keep heading order valid under the page's `h4` title.
- Result rows are real links (`RouterLink`), not click handlers on a non-interactive element.
- All strings are in `frontend/src/i18n/locales/{en,es}.json` under `search.*`.
