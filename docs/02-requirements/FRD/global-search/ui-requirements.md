# UI requirements — Global Search

## Screens
| Screen | Route | Roles | Spec |
|--------|-------|-------|------|
| Top-bar search (suggestions) | every signed-in screen | Signed in | [search.md](../../../05-ui/screen-requirements/search.md#top-bar-search) |
| Global search | `/search` | Public (signed in or not) | [search.md](../../../05-ui/screen-requirements/search.md#results-page) |
| Search administration | `/admin/search` | `MANAGE_SEARCH` | [search.md](../../../05-ui/screen-requirements/search.md#search-administration) |

## Fields and validation
| Field | Type | Required | Validation | Error message (i18n key) |
|-------|------|----------|------------|--------------------------|
| Query | Text | No (blank = everything) | First 200 characters used | - |
| Synonym terms (admin) | Comma-separated text | Yes | 2–10 different terms | `searchAdmin.synonymMinimum` |

## States
- Results page: loading (skeletons), results with count, "Did you mean", meaning search unavailable (info), no results (help with tips and links), load error (`search.loadError`).
- Top bar: searching, suggestions, recent searches (empty box), "See all results".
- Admin: basic engine warning (V020 not applied), pgvector missing, stub model warning, rebuild running.

## Accessibility and localization
- Top-bar box is an ARIA combobox with a listbox of options (`aria-activedescendant`, arrow keys, Enter, Escape).
- Ctrl+K / ⌘K is announced on the shortcut hint.
- Highlights use `<mark>`; result rows are links; the result count is `aria-live`.
- All strings in `frontend/src/i18n/locales/{en,es}.json` under `search.*` and `searchAdmin.*`.
