# TC-PRT-025: Search UI: suggestions, Ctrl/Cmd+K, results page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-025 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Frontend with the search API mocked.

## Steps
1. Type in the top bar; use ↓ and Enter; click a suggestion; press Enter with no highlighted item; focus the empty box; press Ctrl+K.
2. On /search: open with ?q=, type, press Enter, change the filter, follow "Did you mean", search with no results, clear history, sign out.

## Expected Result
Suggestions with highlighted text open their record; Enter opens results; recent searches show; Ctrl+K focuses the box. The results page shows match labels and highlights, tracks only searches the user ran, filters by type, offers help and Clear history, hides tickets when signed out, and has no axe violations.

## Automated coverage
- `frontend/src/components/layout/TopBarSearch.test.tsx` — 7 tests
- `frontend/src/pages/GlobalSearchPage.test.tsx` — 11 tests

## Actual Result
The automated tests above passed on 2026-10-05 (PostgreSQL 16 with pg_trgm and pgvector 0.6.0; embeddings from the stub test model).

## Status
Passed (automated run 2026-10-05)

## Linked Defect (if failed)
None.
