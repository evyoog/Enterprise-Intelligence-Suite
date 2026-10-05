# Search screens (C70)

[REQ-PRT-002](../../02-requirements/FRD/global-search/requirement.md) keyword search, [REQ-PRT-003](../../02-requirements/FRD/semantic-search/requirement.md) semantic search. Built 2026-10-05.

## Top-bar search
Every signed-in screen (desktop width; hidden on phones, where `/search` is used). Component `frontend/src/components/layout/TopBarSearch.tsx`.

| Element | Behaviour |
|---|---|
| Search box | Placeholder `search.topBarPlaceholder`. ARIA combobox. Ctrl+K (⌘K on Mac) focuses and selects it from anywhere; a `Ctrl K` / `⌘K` hint sits at the right |
| Empty box, focused | Up to 5 recent searches (Phase 17 history), headed "Recent searches" |
| Typing (2+ letters) | After 150 ms, up to 8 suggestions from `GET /search/suggest`: type icon, title with the typed text highlighted, type label |
| Last row | "See all results for "…"" → `/search?q=…` |
| Keys | ↓/↑ move through the list; Enter opens the highlighted item, or the results page; Escape closes |

## Results page
Route `/search?q=&type=` (public). Page `frontend/src/pages/GlobalSearchPage.tsx`.

| Element | Behaviour |
|---|---|
| Header | Title "Search" and a one-line explanation (`search.subtitle`) |
| Search box | Search icon, clear button. Typing searches after 300 ms (not saved). Enter runs a tracked search (saved to history, counted in insights). A query arriving in the URL is tracked |
| Filters | Chips: All, Products, Knowledge base, Your tickets (signed in only). Reflected in `?type=` |
| Recent searches | Signed in and empty box: chips of recent searches and **Clear history** |
| Did you mean | "Did you mean <corrected>?" — the link runs the corrected search |
| Meaning search unavailable | Info alert when `semanticStatus` is UNAVAILABLE |
| Result count | "N results", plus "· includes results with a similar meaning" when semantic search was used (`aria-live`) |
| Result row | Type icon, title (link, highlighted words), **match label** chip (Exact ID, Exact phrase, Keyword match, Partial word, Close spelling, Similar meaning — with a tooltip explaining it), snippet with highlighted words, type and `#ID` |
| No results | "No results for "…"", four tips (spelling or other language, fewer words, remove quotes, search an ID), buttons: Browse the product catalog, Open the knowledge base, Contact support (signed in) |
| Loading / error | Skeleton rows; error alert `search.loadError` |

## Search administration
Route `/admin/search`, navigation item **Search** (Administration), permission `MANAGE_SEARCH`. Page `frontend/src/pages/admin/AdminSearchPage.tsx`. Three tabs:

| Tab | Content |
|---|---|
| Index | Tiles: Search engine (Search index / Basic search), Indexed records (by type), Documentation passages (embedded / total, pending), Embedding model (name and dimension, or why not available). Alerts: V020 not applied, pgvector missing, stub model in use. **Rebuild index** and **Rebuild and re-embed** (each with a confirmation dialog); last build status; the page refreshes every 3 s while a build runs. Settings-in-use table |
| Synonyms | Add a group (comma-separated terms, 2–10) and the list of groups with Delete (confirmation) |
| Insights | Period 7 / 30 / 90 days. Tiles: searches, share with no results, share using meaning, p95 and average time. Tables: top searches, top searches with no results |

i18n: `search.*`, `searchAdmin.*`, `appShell.nav.searchAdmin` (en, es).
