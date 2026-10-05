# UI requirements — Semantic Search

No screen of its own; it changes the Global Search screens ([search.md](../../../05-ui/screen-requirements/search.md)):

| Where | What |
|---|---|
| Results page | "Similar meaning" label on records found by meaning; result count says "includes results with a similar meaning" when used; an info message when meaning search is unavailable and only keyword results are shown |
| Search administration → Index | Embedding model tile (model, dimension or why it is not available), passages embedded / total, pending count, stub-model warning, "Rebuild and re-embed", pgvector-missing notice, settings table (similarity threshold, chunk size, overlap, timeout, k) |
| Search administration → Insights | Share of searches that used meaning |

Strings: `search.*`, `searchAdmin.*` in `frontend/src/i18n/locales/{en,es}.json`.
